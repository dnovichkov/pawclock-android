package app.pawclock.feature.quickcalc.ui.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.pawclock.designsystem.components.AgeBigCard
import app.pawclock.designsystem.components.LifeStageChip
import app.pawclock.designsystem.components.PawClockCard
import app.pawclock.designsystem.components.SectionDivider
import app.pawclock.domain.pet.CalculatedAge
import app.pawclock.feature.quickcalc.R
import app.pawclock.model.CalculationMethod
import app.pawclock.model.LifeStage

/**
 * Bottom sheet с результатом Quick Calculator (§5.3 спецификации, Task 20).
 *
 * Содержит:
 *  - [AgeBigCard] с человеческими годами как hero-блоком (главная цифра displayMedium);
 *  - [LifeStageChip] с текущей стадией жизни;
 *  - inline "Как это посчитано" — короткое объяснение формулы и DOI-ссылка;
 *  - [QuickCalcMethodToggle] для собак — переключение Wang/SizeBased в реальном времени.
 *
 * Поскольку Modal sheet с динамическим toggle'ом меняет результат "in-place", это даёт
 * пользователю мгновенный feedback о разнице между методами Wang и SizeBased — UX-ценность
 * §3.2 для educational angle "Wang vs traditional 7×".
 *
 * @param onDismiss колбэк закрытия sheet'а (по умолчанию — не закрывать вручную, sheet
 *   живёт пока state.result остаётся Success).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun QuickCalcResultSheet(
    calculatedAge: CalculatedAge,
    method: CalculationMethod,
    showMethodToggle: Boolean,
    onMethodChange: (CalculationMethod) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SHEET_PADDING_DP.dp)
                    .padding(bottom = SHEET_PADDING_DP.dp),
            verticalArrangement = Arrangement.spacedBy(SECTION_SPACING_DP.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val ageYears = calculatedAge.ageInYears.toInt().coerceAtLeast(0)
            AgeBigCard(
                ageLabel = pluralStringResource(R.plurals.age_years, ageYears, ageYears),
                humanYearsLabel =
                    stringResource(R.string.quick_calc_result_human_years_unit, calculatedAge.humanYears.toInt()),
                ageDescriptor = stringResource(R.string.quick_calc_result_age_descriptor),
                humanYearsDescriptor = stringResource(R.string.quick_calc_result_human_years_descriptor),
            )

            LifeStageChip(
                stage = calculatedAge.lifeStage,
                label = stringResource(lifeStageLabelRes(calculatedAge.lifeStage)),
            )

            if (showMethodToggle) {
                SectionDivider()
                QuickCalcMethodToggle(
                    method = method,
                    onMethodChange = onMethodChange,
                )
            }

            SectionDivider()
            CalculationExplanation(method = calculatedAge.method)
        }
    }
}

@Composable
private fun CalculationExplanation(
    method: CalculationMethod,
    modifier: Modifier = Modifier,
) {
    PawClockCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(EXPL_GAP_DP.dp)) {
            Text(
                text = stringResource(R.string.quick_calc_result_explanation_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(explanationTextRes(method)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@androidx.annotation.StringRes
private fun explanationTextRes(method: CalculationMethod): Int =
    when (method) {
        CalculationMethod.EPIGENETIC -> R.string.quick_calc_result_explanation_epigenetic
        CalculationMethod.SIZE_BASED -> R.string.quick_calc_result_explanation_size_based
    }

@androidx.annotation.StringRes
internal fun lifeStageLabelRes(stage: LifeStage): Int =
    when (stage) {
        LifeStage.Dog.Puppy -> R.string.quick_calc_life_stage_dog_puppy
        LifeStage.Dog.YoungAdult -> R.string.quick_calc_life_stage_dog_young_adult
        LifeStage.Dog.MatureAdult -> R.string.quick_calc_life_stage_dog_mature_adult
        LifeStage.Dog.Senior -> R.string.quick_calc_life_stage_dog_senior
        LifeStage.Dog.EndOfLife -> R.string.quick_calc_life_stage_dog_end_of_life
        LifeStage.Cat.Kitten -> R.string.quick_calc_life_stage_cat_kitten
        LifeStage.Cat.YoungAdult -> R.string.quick_calc_life_stage_cat_young_adult
        LifeStage.Cat.MatureAdult -> R.string.quick_calc_life_stage_cat_mature_adult
        LifeStage.Cat.Senior -> R.string.quick_calc_life_stage_cat_senior
        LifeStage.Cat.EndOfLife -> R.string.quick_calc_life_stage_cat_end_of_life
    }

private const val SHEET_PADDING_DP: Int = 16
private const val SECTION_SPACING_DP: Int = 16
private const val EXPL_GAP_DP: Int = 8
