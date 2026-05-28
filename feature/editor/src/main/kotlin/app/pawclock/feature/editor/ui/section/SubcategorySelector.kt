package app.pawclock.feature.editor.ui.section

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
import app.pawclock.feature.editor.R
import app.pawclock.feature.editor.SubcategoryOption

/**
 * Селектор подкатегории (DogSize / CatType) — зависит от выбранного вида.
 *
 * Если опций нет (species не выбран или not implemented) — composable не рендерится
 * на уровне родителя; здесь предполагается options.isNotEmpty().
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun SubcategorySelector(
    options: List<SubcategoryOption>,
    selectedId: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CHIP_LABEL_GAP_DP.dp),
    ) {
        Text(
            text = stringResource(R.string.pet_editor_subcategory_label),
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
                        // Повторный тап — снять выбор.
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

/**
 * Маппит id подкатегории на string-resource id. Возвращает `0` для неизвестных id,
 * чтобы caller мог упасть на `option.label` без NPE.
 */
@androidx.annotation.StringRes
private fun subcategoryLabelRes(id: String): Int =
    when (id) {
        "toy" -> R.string.dog_size_toy
        "small" -> R.string.dog_size_small
        "medium" -> R.string.dog_size_medium
        "large" -> R.string.dog_size_large
        "giant" -> R.string.dog_size_giant
        "indoor_short_hair" -> R.string.cat_type_indoor_short_hair
        "indoor_long_hair" -> R.string.cat_type_indoor_long_hair
        "outdoor" -> R.string.cat_type_outdoor
        "large_breed" -> R.string.cat_type_large_breed
        else -> 0
    }

private const val CHIP_LABEL_GAP_DP: Int = 8
private const val CHIP_SPACING_DP: Int = 8
