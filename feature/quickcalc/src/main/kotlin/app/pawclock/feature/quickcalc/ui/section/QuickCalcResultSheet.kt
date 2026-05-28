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
import androidx.compose.ui.unit.dp
import app.pawclock.designsystem.components.AgeBigCard
import app.pawclock.designsystem.components.LifeStageChip
import app.pawclock.designsystem.components.PawClockCard
import app.pawclock.designsystem.components.SectionDivider
import app.pawclock.domain.pet.CalculatedAge
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
            AgeBigCard(
                ageLabel = formatYears(calculatedAge.ageInYears),
                humanYearsLabel = "${calculatedAge.humanYears.toInt()} ЧГ",
                ageDescriptor = "Календарный возраст",
                humanYearsDescriptor = "В человеческих годах",
            )

            LifeStageChip(
                stage = calculatedAge.lifeStage,
                label = lifeStageLabel(calculatedAge.lifeStage),
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
                text = "Как это посчитано",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = explanationText(method),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun explanationText(method: CalculationMethod): String =
    when (method) {
        CalculationMethod.EPIGENETIC ->
            "Формула Wang et al. (Cell Systems 2020, DOI 10.1016/j.cels.2020.06.006): " +
                "ЧГ = 16 · ln(age) + 31. Основана на метилировании ДНК — более точна, " +
                "чем традиционная формула «1 год = 7 лет»."
        CalculationMethod.SIZE_BASED ->
            "Табличный метод AKC/AAHA 2019: возраст в человеческих годах берётся из " +
                "опубликованной таблицы и зависит от размерного класса (Toy / Small / " +
                "Medium / Large / Giant)."
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

private const val SHEET_PADDING_DP: Int = 16
private const val SECTION_SPACING_DP: Int = 16
private const val EXPL_GAP_DP: Int = 8
