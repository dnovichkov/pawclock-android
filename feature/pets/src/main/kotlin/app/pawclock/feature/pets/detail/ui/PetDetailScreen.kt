package app.pawclock.feature.pets.detail.ui

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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pawclock.designsystem.components.AgeBigCard
import app.pawclock.designsystem.components.LifeStageChip
import app.pawclock.designsystem.components.PawClockCard
import app.pawclock.designsystem.components.SectionDivider
import app.pawclock.domain.pet.CalculatedAge
import app.pawclock.feature.pets.detail.PetDetailState
import app.pawclock.feature.pets.detail.PetDetailViewModel
import app.pawclock.model.CareRecommendation
import app.pawclock.model.LifeStage
import app.pawclock.model.Pet
import app.pawclock.model.Species

/**
 * Экран детального просмотра питомца (Task 18 / Plan 1, §5.3 спецификации).
 *
 * Layout (вертикальный, scrollable):
 *  1. LargeTopAppBar с именем питомца + IconButton back / Edit FAB.
 *  2. AgeBigCard — главный hero-блок (возраст в годах + ЧГ).
 *  3. LifeStageChip — стадия жизни (с цветовой семантикой Puppy/Senior/etc.)
 *  4. SectionDivider("Рекомендации по уходу") + содержимое care-рекомендации
 *     (если присутствует) или пустой стейт с дисклеймером.
 *  5. SectionDivider("Как это посчитано") + краткое объяснение метода (DOI/AAFP).
 *
 * Локализация: hardcoded русские строки. Полная локализация — Task 22.
 *
 * @param onBackClick колбэк навигации «назад».
 * @param onEditClick колбэк навигации в PetEditor для редактирования (передаёт petId).
 * @param viewModel опционально-передаваемый ViewModel (по умолчанию — Hilt).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetDetailScreen(
    onBackClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PetDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PetDetailContent(
        state = state,
        onBackClick = onBackClick,
        onEditClick = onEditClick,
        modifier = modifier,
    )
}

/**
 * Stateless вариант экрана — отделён от ViewModel'и для testability и preview'ев.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PetDetailContent(
    state: PetDetailState,
    onBackClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(text = topBarTitleFor(state)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            (state as? PetDetailState.Success)?.let { success ->
                FloatingActionButton(onClick = { onEditClick(success.pet.id) }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Редактировать")
                }
            }
        },
    ) { padding ->
        when (state) {
            is PetDetailState.Loading -> CenteredProgress(padding)
            is PetDetailState.NotFound -> NotFoundContent(padding)
            is PetDetailState.Success -> SuccessContent(state, padding)
            is PetDetailState.Error -> ErrorContent(state.messageKey, padding)
        }
    }
}

private fun topBarTitleFor(state: PetDetailState): String =
    when (state) {
        is PetDetailState.Success -> state.pet.name
        is PetDetailState.Loading -> "Загрузка…"
        is PetDetailState.NotFound -> "Питомец"
        is PetDetailState.Error -> "Ошибка"
    }

@Composable
private fun CenteredProgress(padding: PaddingValues) {
    Box(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun NotFoundContent(padding: PaddingValues) {
    Box(
        modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = HORIZONTAL_PADDING_DP.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Питомец не найден.\nВозможно, он был удалён.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ErrorContent(
    messageKey: String,
    padding: PaddingValues,
) {
    Box(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Ошибка: $messageKey",
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun SuccessContent(
    state: PetDetailState.Success,
    padding: PaddingValues,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = HORIZONTAL_PADDING_DP.dp)
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(SECTION_SPACING_DP.dp),
    ) {
        HeroBlock(state.pet, state.calculatedAge)

        SectionDivider(title = "Рекомендации по уходу")
        CareRecommendationsBlock(state.careRecommendation)

        SectionDivider(title = "Как это посчитано")
        CalculationDetailsBlock(state.pet.species, state.calculatedAge)
    }
}

@Composable
private fun HeroBlock(
    pet: Pet,
    calculated: CalculatedAge,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AgeBigCard(
            ageLabel = formatYears(calculated.ageInYears),
            humanYearsLabel = "${calculated.humanYears.toInt()} ЧГ",
            ageDescriptor = "Возраст",
            humanYearsDescriptor = "В человеческих годах",
        )
        LifeStageChip(
            stage = calculated.lifeStage,
            label = lifeStageLabel(calculated.lifeStage),
        )
        if (pet.weightKg != null) {
            Text(
                text = "Вес: ${pet.weightKg} кг",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CareRecommendationsBlock(recommendation: CareRecommendation?) {
    if (recommendation == null) {
        Text(
            text = "Для этой стадии пока нет рекомендаций.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    PawClockCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CareSection(label = "Стадия", body = recommendation.stageDescription)
            CareSection(label = "Питание", body = recommendation.nutrition)
            CareSection(label = "Активность", body = recommendation.activity)
            CareSection(label = "Визиты к врачу", body = recommendation.veterinaryCheckFrequency)
            recommendation.dentalCare?.let { CareSection(label = "Зубы", body = it) }
            CareSection(label = "Тревожные сигналы", body = recommendation.warningSigns)
            Text(
                text = recommendation.disclaimer,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
            Text(
                text = "Источник: ${recommendation.sourceName}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Composable
private fun CareSection(
    label: String,
    body: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun CalculationDetailsBlock(
    species: Species,
    calculated: CalculatedAge,
) {
    val explanation =
        when (species) {
            Species.Dog ->
                "Метод: ${calculated.method.name}. " +
                    "Wang et al. (Cell Systems 2020) или AKC/AAHA 2019 size-table. " +
                    "Возраст ${calculated.ageInYears.format1()} лет → ${calculated.humanYears.format1()} ЧГ."
            Species.Cat ->
                "Метод: AAHA/AAFP 2021. Кусочная формула: " +
                    "1й год = 15, 2й = 24, далее +4/год. Поправки на outdoor (+15%) и large breed (+1/год)."
            else -> "Метод неприменим для этого вида."
        }
    Text(
        text = explanation,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private fun lifeStageLabel(stage: LifeStage): String =
    when (stage) {
        LifeStage.Dog.Puppy -> "Щенок"
        LifeStage.Dog.YoungAdult -> "Молодой взрослый"
        LifeStage.Dog.MatureAdult -> "Зрелый взрослый"
        LifeStage.Dog.Senior -> "Старший"
        LifeStage.Dog.EndOfLife -> "Поздний возраст"
        LifeStage.Cat.Kitten -> "Котёнок"
        LifeStage.Cat.YoungAdult -> "Молодой взрослый"
        LifeStage.Cat.MatureAdult -> "Зрелый взрослый"
        LifeStage.Cat.Senior -> "Старший"
        LifeStage.Cat.EndOfLife -> "Поздний возраст"
    }

private fun formatYears(years: Double): String {
    val whole = years.toInt()
    return "$whole лет"
}

private fun Double.format1(): String = "%.1f".format(this)

private const val HORIZONTAL_PADDING_DP: Int = 16
private const val SECTION_SPACING_DP: Int = 16
