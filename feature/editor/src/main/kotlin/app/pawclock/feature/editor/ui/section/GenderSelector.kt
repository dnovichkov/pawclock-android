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
import app.pawclock.model.Gender

/**
 * Селектор пола (Male / Female / Unknown).
 *
 * Повторный клик на выбранный chip снимает выбор (`onSelect(null)`) — даёт пользователю
 * возможность вернуться в null-состояние, если он передумал.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun GenderSelector(
    selected: Gender?,
    onSelect: (Gender?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CHIP_LABEL_GAP_DP.dp),
    ) {
        Text(
            text = stringResource(R.string.pet_editor_gender_label),
            style = MaterialTheme.typography.labelLarge,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CHIP_SPACING_DP.dp),
        ) {
            Gender.entries.forEach { gender ->
                FilterChip(
                    selected = selected == gender,
                    onClick = {
                        if (selected == gender) onSelect(null) else onSelect(gender)
                    },
                    label = { Text(text = stringResource(genderLabelRes(gender))) },
                    border =
                        FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected == gender,
                        ),
                )
            }
        }
    }
}

@androidx.annotation.StringRes
private fun genderLabelRes(gender: Gender): Int =
    when (gender) {
        Gender.Male -> R.string.pet_editor_gender_male
        Gender.Female -> R.string.pet_editor_gender_female
        Gender.Unknown -> R.string.pet_editor_gender_unknown
    }

private const val CHIP_LABEL_GAP_DP: Int = 8
private const val CHIP_SPACING_DP: Int = 8
