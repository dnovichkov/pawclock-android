package app.pawclock.feature.settings.ui.section

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Заголовок секции настроек — крупный subtle-текст в стиле Material 3 preferences-categories.
 *
 * Используется для группировки родственных настроек (Тема, Язык, Расчёт).
 */
@Composable
internal fun SettingsSectionHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier =
            modifier
                .padding(
                    start = HORIZONTAL_PADDING_DP.dp,
                    end = HORIZONTAL_PADDING_DP.dp,
                    top = TOP_PADDING_DP.dp,
                    bottom = BOTTOM_PADDING_DP.dp,
                ),
    )
}

private const val HORIZONTAL_PADDING_DP: Int = 16
private const val TOP_PADDING_DP: Int = 24
private const val BOTTOM_PADDING_DP: Int = 8
