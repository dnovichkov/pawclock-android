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

/**
 * Селектор языка приложения: System (null), Русский ("ru"), English ("en").
 *
 * Применение реального переключения локали через `AppCompatDelegate.setApplicationLocales`
 * откладывается на Task 22 (Localization). В Task 21 SettingsScreen уже корректно
 * сохраняет/читает выбор пользователя из DataStore — wiring к локали добавится next task.
 */
@Composable
internal fun LanguageSelector(
    selected: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().selectableGroup(),
    ) {
        LanguageOption.entries.forEach { option ->
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selected == option.tag,
                            onClick = { onSelect(option.tag) },
                            role = Role.RadioButton,
                        ).padding(horizontal = ROW_HORIZONTAL_PADDING_DP.dp, vertical = ROW_VERTICAL_PADDING_DP.dp)
                        .testTag(languageOptionTag(option)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ROW_GAP_DP.dp),
            ) {
                RadioButton(
                    selected = selected == option.tag,
                    onClick = null,
                )
                Text(
                    text = stringResource(option.labelRes),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

/**
 * Опция языка: маппит UI-label на BCP 47 tag (null = "как в системе").
 *
 * @property tag null означает следование системной локали; ненулевое значение —
 *   BCP 47 tag, который передаётся в `AppCompatDelegate.setApplicationLocales`
 *   через [app.pawclock.domain.locale.LocaleApplier] (см. SettingsViewModel).
 * @property labelRes string-resource id для отображения в UI (локализованная метка).
 * @property id стабильный id для testTag и detekt-friendly enum-имени.
 */
internal enum class LanguageOption(
    val tag: String?,
    @androidx.annotation.StringRes val labelRes: Int,
    val id: String,
) {
    System(tag = null, labelRes = R.string.settings_language_system, id = "system"),
    Russian(tag = "ru", labelRes = R.string.settings_language_russian, id = "ru"),
    English(tag = "en", labelRes = R.string.settings_language_english, id = "en"),
}

internal fun languageOptionTag(option: LanguageOption): String = "settings_language_${option.id}"

private const val ROW_HORIZONTAL_PADDING_DP: Int = 16
private const val ROW_VERTICAL_PADDING_DP: Int = 12
private const val ROW_GAP_DP: Int = 12
