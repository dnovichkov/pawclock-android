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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.pawclock.feature.editor.R
import app.pawclock.model.Species

/**
 * Селектор вида питомца (Task 19).
 *
 * В Plan 1 показывает только Dog/Cat (фильтр `Species.implemented()`); остальные виды
 * появятся в Plan 2 после реализации соответствующих калькуляторов.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun SpeciesSelector(
    selected: Species?,
    onSelect: (Species) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CHIP_LABEL_GAP_DP.dp),
    ) {
        Text(
            text = stringResource(R.string.pet_editor_species_label),
            style = MaterialTheme.typography.labelLarge,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CHIP_SPACING_DP.dp),
        ) {
            Species.implemented().forEach { species ->
                FilterChip(
                    modifier = Modifier.testTag(speciesChipTag(species)),
                    selected = selected == species,
                    onClick = { onSelect(species) },
                    label = { Text(text = stringResource(speciesLabelRes(species))) },
                    border = FilterChipDefaults.filterChipBorder(enabled = true, selected = selected == species),
                )
            }
        }
    }
}

@androidx.annotation.StringRes
private fun speciesLabelRes(species: Species): Int =
    when (species) {
        Species.Dog -> R.string.pet_editor_species_dog
        Species.Cat -> R.string.pet_editor_species_cat
        // Not-yet-implemented виды используют свой id как fallback-resource-name.
        // Plan 2 добавит реальные строки и расширит список implemented(). До тех пор —
        // делаем читаемый fallback на species_dog (нейтральный плейсхолдер, не падает).
        else -> R.string.pet_editor_species_dog
    }

internal fun speciesChipTag(species: Species): String = "species_chip_${species.id}"

private const val CHIP_LABEL_GAP_DP: Int = 8
private const val CHIP_SPACING_DP: Int = 8
