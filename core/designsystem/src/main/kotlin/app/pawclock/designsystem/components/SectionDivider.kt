package app.pawclock.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Разделитель секций с опциональным заголовком — для PetDetail (care recommendations
 * сворачиваемые секции) и Settings (группировка).
 *
 * Если [title] передан, отображается секционный заголовок (`titleSmall` стиль) над
 * тонкой `HorizontalDivider`. Если null — просто разделитель.
 */
@Composable
fun SectionDivider(
    title: String? = null,
    modifier: Modifier = Modifier,
) {
    if (title != null) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
        )
    }
    HorizontalDivider(
        modifier = if (title == null) modifier.fillMaxWidth() else Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}
