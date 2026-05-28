package app.pawclock.feature.quickcalc.ui.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.pawclock.feature.quickcalc.QuickCalcSubcategoryOption
import app.pawclock.feature.quickcalc.R

/**
 * Селектор подкатегории (DogSize / CatType) для Quick Calculator (Task 20).
 *
 * Аналогичен PetEditor SubcategorySelector'у (см. :feature:editor), но дублирован
 * локально — feature-модули не должны зависеть друг от друга (clean architecture rule).
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun QuickCalcSubcategorySelector(
    options: List<QuickCalcSubcategoryOption>,
    selectedId: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CHIP_LABEL_GAP_DP.dp),
    ) {
        Text(
            text = stringResource(R.string.quick_calc_subcategory_label),
            style = MaterialTheme.typography.labelLarge,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CHIP_SPACING_DP.dp),
        ) {
            options.forEach { option ->
                val labelRes = subcategoryLabelRes(option.id)
                FilterChip(
                    selected = selectedId == option.id,
                    onClick = {
                        if (selectedId == option.id) onSelect(null) else onSelect(option.id)
                    },
                    label = {
                        Text(
                            text = if (labelRes != 0) stringResource(labelRes) else option.label,
                        )
                    },
                    border =
                        FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedId == option.id,
                        ),
                )
            }
        }
    }
}

@androidx.annotation.StringRes
private fun subcategoryLabelRes(id: String): Int =
    when (id) {
        "toy" -> R.string.quick_calc_subcategory_toy
        "small" -> R.string.quick_calc_subcategory_small
        "medium" -> R.string.quick_calc_subcategory_medium
        "large" -> R.string.quick_calc_subcategory_large
        "giant" -> R.string.quick_calc_subcategory_giant
        "indoor_short_hair" -> R.string.quick_calc_subcategory_indoor_short_hair
        "indoor_long_hair" -> R.string.quick_calc_subcategory_indoor_long_hair
        "outdoor" -> R.string.quick_calc_subcategory_outdoor
        "large_breed" -> R.string.quick_calc_subcategory_large_breed
        else -> 0
    }

private const val CHIP_LABEL_GAP_DP: Int = 8
private const val CHIP_SPACING_DP: Int = 8
