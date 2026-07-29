package app.pawclock.feature.quickcalc.ui.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.pawclock.designsystem.components.SpeciesIcon
import app.pawclock.feature.quickcalc.R
import app.pawclock.model.Species

/**
 * Селектор вида питомца для Quick Calculator (Task 20 / Plan 1; расширен в Task 16 / Plan 2).
 *
 * Итерирует `Species.implemented()` — после Plan 2 это все 12 видов. Иконка слева, метка
 * вида локализуется через [speciesLabelRes].
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
            text = stringResource(R.string.quick_calc_species_label),
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
                    label = { Text(text = stringResource(speciesLabelRes(species))) },
                    leadingIcon = {
                        SpeciesIcon(
                            species = species,
                            modifier = Modifier.size(CHIP_ICON_DP.dp),
                        )
                    },
                    border = FilterChipDefaults.filterChipBorder(enabled = true, selected = selected == species),
                )
            }
        }
    }
}

@androidx.annotation.StringRes
private fun speciesLabelRes(species: Species): Int =
    when (species) {
        Species.Dog -> R.string.quick_calc_species_dog
        Species.Cat -> R.string.quick_calc_species_cat
        Species.Rabbit -> R.string.quick_calc_species_rabbit
        Species.Hamster -> R.string.quick_calc_species_hamster
        Species.GuineaPig -> R.string.quick_calc_species_guinea_pig
        Species.Rat -> R.string.quick_calc_species_rat
        Species.Mouse -> R.string.quick_calc_species_mouse
        Species.Ferret -> R.string.quick_calc_species_ferret
        Species.Bird -> R.string.quick_calc_species_bird
        Species.Reptile -> R.string.quick_calc_species_reptile
        Species.Horse -> R.string.quick_calc_species_horse
        Species.Fish -> R.string.quick_calc_species_fish
    }

internal fun quickCalcSpeciesChipTag(species: Species): String = "quick_calc_species_chip_${species.id}"

private const val CHIP_LABEL_GAP_DP: Int = 8
private const val CHIP_SPACING_DP: Int = 8
private const val CHIP_ICON_DP: Int = 18
