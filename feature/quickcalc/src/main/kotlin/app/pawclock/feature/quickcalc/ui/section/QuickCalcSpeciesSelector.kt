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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import app.pawclock.model.Species

/**
 * Селектор вида питомца для Quick Calculator (Task 20).
 *
 * В Plan 1 показывает только Dog/Cat (фильтр `Species.implemented()`); остальные виды
 * появятся в Plan 2.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun QuickCalcSpeciesSelector(
    selected: Species?,
    onSelect: (Species) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CHIP_LABEL_GAP_DP.dp),
    ) {
        Text(
            text = "Вид *",
            style = MaterialTheme.typography.labelLarge,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CHIP_SPACING_DP.dp),
        ) {
            Species.implemented().forEach { species ->
                FilterChip(
                    modifier = Modifier.testTag(quickCalcSpeciesChipTag(species)),
                    selected = selected == species,
                    onClick = { onSelect(species) },
                    label = { Text(text = speciesLabel(species)) },
                    border = FilterChipDefaults.filterChipBorder(enabled = true, selected = selected == species),
                )
            }
        }
    }
}

private fun speciesLabel(species: Species): String =
    when (species) {
        Species.Dog -> "Собака"
        Species.Cat -> "Кошка"
        else -> species.id
    }

internal fun quickCalcSpeciesChipTag(species: Species): String = "quick_calc_species_chip_${species.id}"

private const val CHIP_LABEL_GAP_DP: Int = 8
private const val CHIP_SPACING_DP: Int = 8
