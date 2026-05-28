package app.pawclock.locale

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import app.pawclock.domain.locale.LocaleApplier

/**
 * Тонкая Android-обёртка над [AppCompatDelegate.setApplicationLocales] для применения
 * in-app выбора языка из Settings → Language.
 *
 * Реализует port [LocaleApplier] (объявлен в `:core:domain`), что позволяет
 * SettingsViewModel'и не зависеть напрямую от AppCompat и легко мокаться в unit-тестах.
 *
 * Поведение:
 *   • `tag = null/blank` → следовать системному языку (LocaleListCompat.getEmptyLocaleList())
 *   • `tag = "ru"` / `"en"` → установить язык приложения, отвязав от системы
 *
 * Side-effect: AppCompatDelegate триггерит Activity recreate, чтобы Composables
 * с stringResource'ами перечитали ресурсы под новой локалью. Это стандартный паттерн
 * Android per-app language preferences (API 33+ native; на API 24-32 AppCompat
 * бэкпортит через сохранение в SharedPreferences и применение при старте).
 *
 * Ссылка: https://developer.android.com/guide/topics/resources/app-languages
 */
object LocaleHelper : LocaleApplier {
    override fun applyLanguageTag(tag: String?) {
        val locales =
            if (tag.isNullOrBlank()) {
                LocaleListCompat.getEmptyLocaleList()
            } else {
                LocaleListCompat.forLanguageTags(tag)
            }
        AppCompatDelegate.setApplicationLocales(locales)
    }
}
