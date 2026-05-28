package app.pawclock.feature.quickcalc.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pawclock.feature.quickcalc.QuickCalcEvent
import app.pawclock.feature.quickcalc.QuickCalcResult
import app.pawclock.feature.quickcalc.QuickCalcState
import app.pawclock.feature.quickcalc.QuickCalcViewModel
import app.pawclock.feature.quickcalc.R
import app.pawclock.feature.quickcalc.ui.section.QuickCalcBirthDateField
import app.pawclock.feature.quickcalc.ui.section.QuickCalcMethodToggle
import app.pawclock.feature.quickcalc.ui.section.QuickCalcResultSheet
import app.pawclock.feature.quickcalc.ui.section.QuickCalcSpeciesSelector
import app.pawclock.feature.quickcalc.ui.section.QuickCalcSubcategorySelector
import app.pawclock.feature.quickcalc.ui.section.QuickCalcValidationErrorsBanner
import app.pawclock.model.Species

/**
 * Экран Quick Calculator (§5.3, Task 20 / Plan 1).
 *
 * Главная цель — расчёт возраста без сохранения, поэтому экран короче PetEditor'а:
 * только species + subcategory + birthDate + method toggle (для собак). Результат —
 * в bottom sheet'е (QuickCalcResultSheet) поверх формы.
 *
 * Локализация: hardcoded русские строки на этом этапе; полный stringResource — Task 22.
 *
 * @param onBack колбэк toolbar-back / системной кнопки.
 * @param viewModel опционально-передаваемый ViewModel (по умолчанию — через Hilt).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickCalcScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QuickCalcViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    QuickCalcContent(
        state = state,
        onEvent = viewModel::handleEvent,
        onBack = onBack,
        modifier = modifier,
    )
}

/**
 * Stateless вариант экрана — отделён от ViewModel'и для testability и preview'ев.
 * Compose-тесты могут передавать произвольный [QuickCalcState] напрямую без Hilt-setup'а.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun QuickCalcContent(
    state: QuickCalcState,
    onEvent: (QuickCalcEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.quick_calc_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.quick_calc_back),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                modifier = Modifier.testTag(CALCULATE_FAB_TEST_TAG),
                onClick = { onEvent(QuickCalcEvent.Calculate) },
                text = { Text(text = stringResource(R.string.quick_calc_calculate)) },
                icon = { /* без иконки — текстовая FAB достаточно выразительна */ },
            )
        },
    ) { padding ->
        QuickCalcForm(state = state, onEvent = onEvent, padding = padding)
        // Result sheet поверх формы — показывается только при Success.
        if (state.result is QuickCalcResult.Success) {
            QuickCalcResultSheet(
                calculatedAge = state.result.calculatedAge,
                method = state.method,
                showMethodToggle = state.species == Species.Dog,
                onMethodChange = { onEvent(QuickCalcEvent.SetMethod(it)) },
                onDismiss = { /* sheet не закрывается явно — пересчёт через Calculate FAB */ },
            )
        }
    }
}

@Composable
private fun QuickCalcForm(
    state: QuickCalcState,
    onEvent: (QuickCalcEvent) -> Unit,
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
        Box(Modifier.padding(top = TOP_GAP_DP.dp))

        Text(
            text = stringResource(R.string.quick_calc_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        QuickCalcSpeciesSelector(
            selected = state.species,
            onSelect = { onEvent(QuickCalcEvent.SelectSpecies(it)) },
        )

        if (state.availableSubcategories.isNotEmpty()) {
            QuickCalcSubcategorySelector(
                options = state.availableSubcategories,
                selectedId = state.subcategory,
                onSelect = { onEvent(QuickCalcEvent.SetSubcategory(it)) },
            )
        }

        QuickCalcBirthDateField(
            value = state.birthDate,
            onChange = { onEvent(QuickCalcEvent.SetBirthDate(it)) },
            isError =
                (state.result as? QuickCalcResult.ValidationError)?.errors?.any {
                    it.name.startsWith("BirthDate")
                } == true,
        )

        // Method toggle для собак отображается в форме до Calculate; после Success — в bottom sheet.
        // Это упрощает UX: при первом расчёте уже видно, какой метод будет применён.
        if (state.species == Species.Dog && state.result !is QuickCalcResult.Success) {
            QuickCalcMethodToggle(
                method = state.method,
                onMethodChange = { onEvent(QuickCalcEvent.SetMethod(it)) },
            )
        }

        if (state.result is QuickCalcResult.ValidationError) {
            QuickCalcValidationErrorsBanner(state.result.errors)
        }

        Box(Modifier.padding(bottom = BOTTOM_GAP_DP.dp))
    }
}

internal const val CALCULATE_FAB_TEST_TAG: String = "quick_calc_calculate_fab"

private const val HORIZONTAL_PADDING_DP: Int = 16
private const val TOP_GAP_DP: Int = 8
private const val BOTTOM_GAP_DP: Int = 96
private const val SECTION_SPACING_DP: Int = 16
