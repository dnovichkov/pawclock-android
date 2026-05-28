package app.pawclock.feature.settings.ui.section

import android.os.Build
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

/**
 * Переключатель Material You dynamic color.
 *
 * На устройствах ниже Android 12 (API < 31) dynamic color невозможен — switch
 * остаётся видимым, но с дополнительной supporting line, объясняющей почему он
 * disabled. Это соответствует §5 спецификации: "fallback палитра используется на API < 31".
 *
 * @param enabled текущее значение из DataStore.
 * @param onToggle callback при пользовательском изменении (только когда switch активен).
 */
@Composable
internal fun DynamicColorSwitch(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val deviceSupportsDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    ListItem(
        modifier = modifier.testTag(DYNAMIC_COLOR_SWITCH_TEST_TAG),
        headlineContent = {
            Text(text = "Динамические цвета")
        },
        supportingContent = {
            Text(
                text =
                    if (deviceSupportsDynamic) {
                        "Цвета приложения подстраиваются под обои Android"
                    } else {
                        "Доступно с Android 12. На вашем устройстве используется фирменная палитра."
                    },
                style = MaterialTheme.typography.bodySmall,
            )
        },
        trailingContent = {
            Switch(
                checked = enabled,
                onCheckedChange = if (deviceSupportsDynamic) onToggle else null,
                enabled = deviceSupportsDynamic,
                modifier = Modifier.testTag(DYNAMIC_COLOR_SWITCH_CONTROL_TAG),
            )
        },
    )
}

internal const val DYNAMIC_COLOR_SWITCH_TEST_TAG: String = "settings_dynamic_color_row"
internal const val DYNAMIC_COLOR_SWITCH_CONTROL_TAG: String = "settings_dynamic_color_switch"
