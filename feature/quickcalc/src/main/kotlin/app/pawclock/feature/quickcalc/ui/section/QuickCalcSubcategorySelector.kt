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
import androidx.compose.ui.unit.dp
import app.pawclock.feature.quickcalc.QuickCalcSubcategoryOption

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
            text = "Подкатегория",
            style = MaterialTheme.typography.labelLarge,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CHIP_SPACING_DP.dp),
        ) {
            options.forEach { option ->
                FilterChip(
                    selected = selectedId == option.id,
                    onClick = {
                        // Повторный тап — снять выбор.
                        if (selectedId == option.id) onSelect(null) else onSelect(option.id)
                    },
                    label = { Text(text = subcategoryLabel(option)) },
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
 * Возвращает человекочитаемое имя подкатегории по её id.
 * В Task 22 заменим на stringResource(R.string.dog_size_${id}) / cat_type_${id}.
 */
private fun subcategoryLabel(option: QuickCalcSubcategoryOption): String =
    when (option.id) {
        "toy" -> "Той"
        "small" -> "Маленькая"
        "medium" -> "Средняя"
        "large" -> "Большая"
        "giant" -> "Гигантская"
        "indoor_short_hair" -> "Домашняя короткошёрстная"
        "indoor_long_hair" -> "Домашняя длинношёрстная"
        "outdoor" -> "Уличная"
        "large_breed" -> "Крупная порода"
        else -> option.label
    }

private const val CHIP_LABEL_GAP_DP: Int = 8
private const val CHIP_SPACING_DP: Int = 8
