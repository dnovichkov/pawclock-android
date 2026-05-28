package app.pawclock.feature.editor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pawclock.feature.editor.PetEditorEvent
import app.pawclock.feature.editor.PetEditorSaveResult
import app.pawclock.feature.editor.PetEditorState
import app.pawclock.feature.editor.PetEditorViewModel
import app.pawclock.feature.editor.R
import app.pawclock.feature.editor.ui.section.BirthDateField
import app.pawclock.feature.editor.ui.section.GenderSelector
import app.pawclock.feature.editor.ui.section.SpeciesSelector
import app.pawclock.feature.editor.ui.section.SubcategorySelector
import app.pawclock.feature.editor.ui.section.ValidationErrorsBanner

/**
 * Экран создания/редактирования питомца (§5.3 спецификации, Task 19 / Plan 1).
 *
 * Структура: один длинный scrollable Column с секциями (name → species → subcategory →
 * birthDate → gender → weight → notes), Save FAB по §5.3 и Back-кнопкой в TopAppBar.
 *
 * Photo picker отложен на Plan 2 (PhotoPicker через ACTION_PICK_IMAGES не требует runtime
 * разрешения, но требует Activity API — лучше вместе с editor-полноценным фото-experience'ом).
 *
 * Локализация: hardcoded русские строки на этом этапе; полная локализация через
 * `stringResource(R.string.xxx)` — Task 22.
 *
 * @param onSaved колбэк после успешного сохранения; UI вызывает после навигации.
 * @param onBack колбэк для toolbar-back / системной кнопки.
 * @param viewModel опционально-передаваемый ViewModel (по умолчанию — через Hilt).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetEditorScreen(
    onSaved: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PetEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PetEditorContent(
        state = state,
        onEvent = viewModel::handleEvent,
        onBack = onBack,
        onSaved = onSaved,
        modifier = modifier,
    )
}

/**
 * Stateless вариант экрана — отделён от ViewModel'и для testability и preview'ев.
 * Compose-тесты могут передать любое [PetEditorState] напрямую без Hilt-setup'а.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PetEditorContent(
    state: PetEditorState,
    onEvent: (PetEditorEvent) -> Unit,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    // Реакция на результат save:
    //  - Success → уведомить навигатор + очистить saveResult;
    //  - Error → показать snackbar с локализованным сообщением и очистить saveResult,
    //    иначе ConsumeSaveResult никогда не отправляется и форма залипает в Error-состоянии.
    val saveErrorMessage =
        (state.saveResult as? PetEditorSaveResult.Error)?.let {
            stringResource(formErrorMessageRes(it.messageKey))
        }
    LaunchedEffect(state.saveResult) {
        when (val result = state.saveResult) {
            is PetEditorSaveResult.Success -> {
                onSaved(result.petId)
                onEvent(PetEditorEvent.ConsumeSaveResult)
            }
            is PetEditorSaveResult.Error -> {
                if (saveErrorMessage != null) {
                    snackbarHostState.showSnackbar(saveErrorMessage)
                }
                onEvent(PetEditorEvent.ConsumeSaveResult)
            }
            null -> Unit
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text =
                            if (state.editingPetId == null) {
                                stringResource(R.string.pet_editor_title_new)
                            } else {
                                stringResource(R.string.pet_editor_title_edit)
                            },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.pet_editor_back),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                modifier = Modifier.testTag(SAVE_FAB_TEST_TAG),
                onClick = { onEvent(PetEditorEvent.Save) },
                icon = { Icon(Icons.Filled.Done, contentDescription = null) },
                text = { Text(text = stringResource(R.string.pet_editor_save)) },
            )
        },
    ) { padding ->
        when {
            state.isLoading -> LoadingContent(padding)
            else -> FormContent(state, onEvent, padding)
        }
    }
}

@Composable
private fun LoadingContent(padding: PaddingValues) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(padding),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun FormContent(
    state: PetEditorState,
    onEvent: (PetEditorEvent) -> Unit,
    padding: PaddingValues,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = HORIZONTAL_PADDING_DP.dp)
                .verticalScroll(rememberScrollState())
                .semantics { },
        verticalArrangement = Arrangement.spacedBy(SECTION_SPACING_DP.dp),
    ) {
        Box(Modifier.padding(top = TOP_GAP_DP.dp))

        OutlinedTextField(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .testTag(NAME_FIELD_TEST_TAG),
            value = state.name,
            onValueChange = { onEvent(PetEditorEvent.SetName(it)) },
            label = { Text(text = stringResource(R.string.pet_editor_name_label)) },
            singleLine = true,
            isError = state.validationErrors.any { it.name == "NameBlank" },
        )

        SpeciesSelector(
            selected = state.species,
            onSelect = { onEvent(PetEditorEvent.SelectSpecies(it)) },
        )

        if (state.availableSubcategories.isNotEmpty()) {
            SubcategorySelector(
                options = state.availableSubcategories,
                selectedId = state.subcategory,
                onSelect = { onEvent(PetEditorEvent.SetSubcategory(it)) },
            )
        }

        BirthDateField(
            value = state.birthDate,
            onChange = { onEvent(PetEditorEvent.SetBirthDate(it)) },
            isError = state.validationErrors.any { it.name.startsWith("BirthDate") },
        )

        GenderSelector(
            selected = state.gender,
            onSelect = { onEvent(PetEditorEvent.SetGender(it)) },
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.weightKg,
            onValueChange = { onEvent(PetEditorEvent.SetWeight(it)) },
            label = { Text(text = stringResource(R.string.pet_editor_weight_label)) },
            singleLine = true,
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.notes,
            onValueChange = { onEvent(PetEditorEvent.SetNotes(it)) },
            label = { Text(text = stringResource(R.string.pet_editor_notes_label)) },
            minLines = NOTES_MIN_LINES,
        )

        if (state.formErrorMessageKey != null) {
            Text(
                text = stringResource(formErrorMessageRes(state.formErrorMessageKey)),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        if (state.validationErrors.isNotEmpty()) {
            ValidationErrorsBanner(state.validationErrors)
        }

        Box(Modifier.padding(bottom = BOTTOM_GAP_DP.dp))
    }
}

/**
 * Маппит доменный messageKey (см. PetValidationError / SavePetUseCase) на string-resource id.
 */
@androidx.annotation.StringRes
private fun formErrorMessageRes(messageKey: String): Int =
    when (messageKey) {
        "pet_editor_error_species_required" -> R.string.pet_editor_error_species_required
        "pet_editor_error_birth_date_required" -> R.string.pet_editor_error_birth_date_required
        "pet_editor_error_pet_not_found" -> R.string.pet_editor_error_pet_not_found
        "pet_editor_error_unsupported_species" -> R.string.pet_editor_error_unsupported_species
        "pet_editor_error_load_failed" -> R.string.pet_editor_error_load_failed
        "pet_editor_error_save_failed" -> R.string.pet_editor_error_save_failed
        // Unknown key — fallback на нейтральное load_failed-сообщение чтобы не вводить
        // пользователя в заблуждение (раньше показывали species_required даже для IO-ошибок).
        else -> R.string.pet_editor_error_load_failed
    }

internal const val NAME_FIELD_TEST_TAG: String = "pet_editor_name_field"
internal const val SAVE_FAB_TEST_TAG: String = "pet_editor_save_fab"

private const val HORIZONTAL_PADDING_DP: Int = 16
private const val TOP_GAP_DP: Int = 8
private const val BOTTOM_GAP_DP: Int = 96
private const val SECTION_SPACING_DP: Int = 16
private const val NOTES_MIN_LINES: Int = 3
