package app.pawclock.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.pawclock.datastore.AppSettings
import app.pawclock.datastore.SettingsRepository
import app.pawclock.domain.export.ExportFormat
import app.pawclock.domain.export.ExportPetsUseCase
import app.pawclock.domain.import_.ImportException
import app.pawclock.domain.import_.ImportPetsUseCase
import app.pawclock.domain.import_.PetsCsvDeserializer
import app.pawclock.domain.import_.PetsImportResult
import app.pawclock.domain.import_.PetsJsonDeserializer
import app.pawclock.domain.locale.LocaleApplier
import app.pawclock.feature.settings.backup.BackupFileReader
import app.pawclock.feature.settings.backup.BackupFileWriter
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel экрана настроек (Task 21 / Plan 1 + Plan 2 Task 21).
 *
 * Plan 1: read-through proxy для [SettingsRepository] — [state] derived из `repository.observe()`;
 * каждое настроечное событие делегирует в suspend setter (fire-and-forget).
 *
 * Plan 2 (§3.5): экспорт/импорт данных питомцев. Поскольку выбор файла идёт через системный
 * SAF-диалог (асинхронный Activity Result), операции двухфазны и общаются с UI через one-time
 * [effects] (Channel → Flow), а не через [state]:
 *  - [SettingsEvent.ExportRequested] → запоминает формат, эмитит [SettingsEffect.RequestSaveLocation];
 *  - [SettingsEvent.ExportLocationSelected] → сериализует и пишет файл, эмитит результат;
 *  - [SettingsEvent.ImportRequested] → эмитит [SettingsEffect.RequestOpenLocation];
 *  - [SettingsEvent.ImportLocationSelected] → читает файл и импортирует, эмитит результат.
 *
 * [BackupFileWriter]/[BackupFileReader] — порты SAF I/O, изолирующие ViewModel от Android `Uri`.
 */
@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val repository: SettingsRepository,
        private val localeApplier: LocaleApplier,
        private val exportPets: ExportPetsUseCase,
        private val importPets: ImportPetsUseCase,
        private val backupFileWriter: BackupFileWriter,
        private val backupFileReader: BackupFileReader,
    ) : ViewModel() {
        val state: StateFlow<SettingsState> =
            repository
                .observe()
                .map { it.toSettingsState() }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
                    initialValue = SettingsState.Default,
                )

        private val effectChannel = Channel<SettingsEffect>(Channel.BUFFERED)

        /** Поток one-time эффектов (SAF-запросы, результаты). UI коллектит ровно один раз. */
        val effects: Flow<SettingsEffect> = effectChannel.receiveAsFlow()

        /**
         * Формат, выбранный в [SettingsEvent.ExportRequested], — нужен на втором шаге
         * ([SettingsEvent.ExportLocationSelected]), т.к. SAF-диалог между шагами теряет контекст.
         */
        private var pendingExportFormat: ExportFormat = ExportFormat.JSON

        fun handleEvent(event: SettingsEvent) {
            when (event) {
                is SettingsEvent.SetThemeMode ->
                    viewModelScope.launch { repository.setThemeMode(event.themeMode) }
                is SettingsEvent.SetLanguageTag -> {
                    // Сначала persistим в DataStore, ПОТОМ триггерим Activity recreate.
                    // AppCompatDelegate.setApplicationLocales() инициирует recreate,
                    // что отменяет viewModelScope; если persist запустить ДО ожидания записи —
                    // DataStore.edit { } может быть отменён до commit на диск, и
                    // в следующий cold start языковая настройка SettingsRepository окажется
                    // на старом значении (хотя AppCompat metadata-service всё равно сохранит
                    // локаль сам — UI отрисуется правильно, но Settings-radio покажет
                    // старый выбор). Sequence запуска внутри одной корутины гарантирует,
                    // что setLanguageTag завершён до applyLanguageTag.
                    viewModelScope.launch {
                        repository.setLanguageTag(event.languageTag)
                        localeApplier.applyLanguageTag(event.languageTag)
                    }
                }
                is SettingsEvent.SetDynamicColor ->
                    viewModelScope.launch { repository.setDynamicColor(event.enabled) }
                is SettingsEvent.SetDefaultCalculationMethod ->
                    viewModelScope.launch { repository.setDefaultCalculationMethod(event.method) }
                is SettingsEvent.ExportRequested -> onExportRequested(event)
                is SettingsEvent.ExportLocationSelected -> onExportLocationSelected(event)
                SettingsEvent.ImportRequested ->
                    viewModelScope.launch {
                        effectChannel.send(SettingsEffect.RequestOpenLocation(IMPORT_MIME_TYPES))
                    }
                is SettingsEvent.ImportLocationSelected -> onImportLocationSelected(event)
            }
        }

        private fun onExportRequested(event: SettingsEvent.ExportRequested) {
            pendingExportFormat = event.format
            val mime =
                when (event.format) {
                    ExportFormat.JSON -> MIME_JSON
                    ExportFormat.CSV -> MIME_CSV
                }
            val extension =
                when (event.format) {
                    ExportFormat.JSON -> "json"
                    ExportFormat.CSV -> "csv"
                }
            viewModelScope.launch {
                effectChannel.send(
                    SettingsEffect.RequestSaveLocation(
                        suggestedFilename = "$BACKUP_FILE_BASENAME.$extension",
                        mimeType = mime,
                    ),
                )
            }
        }

        private fun onExportLocationSelected(event: SettingsEvent.ExportLocationSelected) {
            viewModelScope.launch {
                val effect =
                    try {
                        val content = exportPets(pendingExportFormat)
                        backupFileWriter.write(event.uriString, content)
                        SettingsEffect.ExportComplete(petCount = countPets(content, pendingExportFormat))
                    } catch (_: IOException) {
                        SettingsEffect.ExportError(SettingsMessages.EXPORT_ERROR_WRITE_FAILED)
                    } catch (_: RuntimeException) {
                        // Защита от непредвиденных ошибок сериализации/репозитория — иначе
                        // необработанное исключение в viewModelScope свалит процесс.
                        SettingsEffect.ExportError(SettingsMessages.EXPORT_ERROR_WRITE_FAILED)
                    }
                effectChannel.send(effect)
            }
        }

        private fun onImportLocationSelected(event: SettingsEvent.ImportLocationSelected) {
            viewModelScope.launch {
                val effect =
                    try {
                        val content = backupFileReader.read(event.uriString)
                        val summary = importPets(content = content, strategy = event.strategy)
                        SettingsEffect.ImportComplete(
                            petCount = summary.importedCount,
                            warnings = summary.warnings,
                        )
                    } catch (e: ImportException) {
                        SettingsEffect.ImportError(e.toMessageKey())
                    } catch (_: IOException) {
                        SettingsEffect.ImportError(SettingsMessages.IMPORT_ERROR_READ_FAILED)
                    } catch (_: RuntimeException) {
                        SettingsEffect.ImportError(SettingsMessages.IMPORT_ERROR_READ_FAILED)
                    }
                effectChannel.send(effect)
            }
        }

        /**
         * Считает число только что экспортированных питомцев, разбирая собственный вывод тем же
         * форматным десериализатором. Данные бэкапа малы (десятки питомцев), повторный разбор
         * дёшев и избавляет от лишней зависимости на репозиторий ради одного числа в сообщении.
         */
        private fun countPets(
            content: String,
            format: ExportFormat,
        ): Int {
            val result =
                when (format) {
                    ExportFormat.JSON -> PetsJsonDeserializer.decode(content)
                    ExportFormat.CSV -> PetsCsvDeserializer.decode(content)
                }
            return (result as? PetsImportResult.Success)?.entries?.size ?: 0
        }

        /** Маппит типизированную доменную ошибку импорта на стабильный ключ сообщения для UI. */
        private fun ImportException.toMessageKey(): String =
            when (this) {
                is ImportException.MalformedData -> SettingsMessages.IMPORT_ERROR_MALFORMED
                is ImportException.MissingRequiredField -> SettingsMessages.IMPORT_ERROR_MISSING_FIELD
                is ImportException.UnknownSpecies -> SettingsMessages.IMPORT_ERROR_UNKNOWN_SPECIES
                is ImportException.UnsupportedSchemaVersion ->
                    SettingsMessages.IMPORT_ERROR_UNSUPPORTED_VERSION
            }

        private fun AppSettings.toSettingsState(): SettingsState =
            SettingsState(
                themeMode = themeMode,
                languageTag = languageTag,
                dynamicColor = dynamicColor,
                defaultCalculationMethod = defaultCalculationMethod,
            )

        private companion object {
            /**
             * Таймаут перед отменой upstream-Flow когда последний subscriber отписался.
             * 5 секунд — стандартное значение для WhileSubscribed: переживает rotation
             * без перезаписи DataStore-state, но останавливает collection при background.
             */
            const val SUBSCRIPTION_TIMEOUT_MS: Long = 5_000L

            const val MIME_JSON: String = "application/json"
            const val MIME_CSV: String = "text/csv"

            /** Базовое имя файла бэкапа; пользователь может переименовать в SAF-диалоге. */
            const val BACKUP_FILE_BASENAME: String = "pawclock-backup"

            /** MIME-фильтр для `ACTION_OPEN_DOCUMENT` при импорте. */
            val IMPORT_MIME_TYPES: List<String> = listOf(MIME_JSON, MIME_CSV)
        }
    }
