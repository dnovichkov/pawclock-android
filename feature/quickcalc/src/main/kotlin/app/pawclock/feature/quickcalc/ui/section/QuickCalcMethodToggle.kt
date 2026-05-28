package app.pawclock.feature.quickcalc.ui.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import app.pawclock.model.CalculationMethod

/**
 * Переключатель метода расчёта для собак (Wang / SizeBased) — только для [app.pawclock.model.Species.Dog].
 *
 * Wang (EPIGENETIC) — эпигенетическая формула Wang et al. 2020 (по умолчанию, ADR-0006).
 * SizeBased (SIZE_BASED) — AKC/AAHA 2019 размер-табличный метод.
 *
 * Использует Material 3 SegmentedButton по §5.3 спецификации.
 */
@Composable
internal fun QuickCalcMethodToggle(
    method: CalculationMethod,
    onMethodChange: (CalculationMethod) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(LABEL_GAP_DP.dp),
    ) {
        Text(
            text = "Метод расчёта",
            style = MaterialTheme.typography.labelLarge,
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            val methods = CalculationMethod.entries
            methods.forEachIndexed { index, item ->
                SegmentedButton(
                    modifier = Modifier.testTag(quickCalcMethodTag(item)),
                    selected = method == item,
                    onClick = { onMethodChange(item) },
                    shape =
                        SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = methods.size,
                        ),
                ) {
                    Text(text = methodLabel(item))
                }
            }
        }
    }
}

private fun methodLabel(method: CalculationMethod): String =
    when (method) {
        CalculationMethod.EPIGENETIC -> "Wang (эпигенетика)"
        CalculationMethod.SIZE_BASED -> "По размеру"
    }

internal fun quickCalcMethodTag(method: CalculationMethod): String = "quick_calc_method_${method.name}"

private const val LABEL_GAP_DP: Int = 8
