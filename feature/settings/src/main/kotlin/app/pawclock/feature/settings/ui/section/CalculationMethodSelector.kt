package app.pawclock.feature.settings.ui.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import app.pawclock.feature.settings.R
import app.pawclock.model.CalculationMethod

/**
 * Селектор дефолтного метода расчёта возраста для собак.
 *
 * EPIGENETIC (Wang 2020) — научно более точный для среднего возраста;
 * SIZE_BASED (AKC/AAHA 2019) — табличный, учитывает размер собаки.
 *
 * Применяется только к собакам (для кошек — единый AAFP-метод, см. ADR-0006).
 * UI это объясняет через supporting-описания каждой опции.
 */
@Composable
internal fun CalculationMethodSelector(
    selected: CalculationMethod,
    onSelect: (CalculationMethod) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().selectableGroup(),
    ) {
        CalculationMethod.entries.forEach { option ->
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selected == option,
                            onClick = { onSelect(option) },
                            role = Role.RadioButton,
                        ).padding(horizontal = ROW_HORIZONTAL_PADDING_DP.dp, vertical = ROW_VERTICAL_PADDING_DP.dp)
                        .testTag(calculationMethodOptionTag(option)),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(ROW_GAP_DP.dp),
            ) {
                RadioButton(
                    selected = selected == option,
                    onClick = null,
                )
                Column(verticalArrangement = Arrangement.spacedBy(LABEL_GAP_DP.dp)) {
                    Text(
                        text = stringResource(methodLabelRes(option)),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = stringResource(methodSupportingTextRes(option)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@androidx.annotation.StringRes
private fun methodLabelRes(method: CalculationMethod): Int =
    when (method) {
        CalculationMethod.EPIGENETIC -> R.string.settings_method_epigenetic_title
        CalculationMethod.SIZE_BASED -> R.string.settings_method_size_based_title
    }

@androidx.annotation.StringRes
private fun methodSupportingTextRes(method: CalculationMethod): Int =
    when (method) {
        CalculationMethod.EPIGENETIC -> R.string.settings_method_epigenetic_supporting
        CalculationMethod.SIZE_BASED -> R.string.settings_method_size_based_supporting
    }

internal fun calculationMethodOptionTag(method: CalculationMethod): String = "settings_method_${method.name}"

private const val ROW_HORIZONTAL_PADDING_DP: Int = 16
private const val ROW_VERTICAL_PADDING_DP: Int = 12
private const val ROW_GAP_DP: Int = 12
private const val LABEL_GAP_DP: Int = 4
