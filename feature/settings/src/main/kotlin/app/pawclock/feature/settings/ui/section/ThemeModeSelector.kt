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
import app.pawclock.model.ThemeMode

/**
 * Селектор режима темы (Light / Dark / System) — radio-group из 3 опций.
 *
 * Material 3 рекомендует именно RadioButton (а не SegmentedButton) для full-width
 * preference-rows: каждый item получает свой `selectable` + Role.RadioButton ради
 * правильной семантики доступности (TalkBack читает "Светлая, отмечено, 1 из 3").
 *
 * @param selected текущий выбор.
 * @param onSelect callback при клике на любую опцию.
 */
@Composable
internal fun ThemeModeSelector(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().selectableGroup(),
    ) {
        ThemeMode.entries.forEach { option ->
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selected == option,
                            onClick = { onSelect(option) },
                            role = Role.RadioButton,
                        ).padding(horizontal = ROW_HORIZONTAL_PADDING_DP.dp, vertical = ROW_VERTICAL_PADDING_DP.dp)
                        .testTag(themeModeOptionTag(option)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ROW_GAP_DP.dp),
            ) {
                RadioButton(
                    selected = selected == option,
                    // Внутренний click перехвачен через Row.selectable, чтобы tap-area был всю строку.
                    onClick = null,
                )
                Text(
                    text = stringResource(themeModeLabelRes(option)),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@androidx.annotation.StringRes
private fun themeModeLabelRes(themeMode: ThemeMode): Int =
    when (themeMode) {
        ThemeMode.Light -> R.string.settings_theme_light
        ThemeMode.Dark -> R.string.settings_theme_dark
        ThemeMode.System -> R.string.settings_theme_system
    }

internal fun themeModeOptionTag(themeMode: ThemeMode): String = "settings_theme_${themeMode.id}"

private const val ROW_HORIZONTAL_PADDING_DP: Int = 16
private const val ROW_VERTICAL_PADDING_DP: Int = 12
private const val ROW_GAP_DP: Int = 12
