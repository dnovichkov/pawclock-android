package app.pawclock.locale

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import app.pawclock.domain.locale.LocaleApplier

/**
 * Применение in-app выбора языка из Settings → Language.
 *
 * Реализует port [LocaleApplier] (объявлен в `:core:domain`), что позволяет
 * SettingsViewModel'и не зависеть напрямую от Android API и легко мокаться в unit-тестах.
 *
 * Поведение:
 *   • `tag = null/blank` → следовать системному языку (пустой locale list)
 *   • `tag = "ru"` / `"en"` → установить язык приложения, отвязав от системы
 *
 * Почему на API 33+ используется framework [LocaleManager], а не AppCompatDelegate:
 * `AppCompatDelegate.setApplicationLocales` на 33+ делегирует в LocaleManager через
 * внутренний application context (`sAppContext`), который AppCompat записывает только
 * при attach'е AppCompatActivity. MainActivity у нас ComponentActivity, поэтому
 * sAppContext остаётся null и вызов молча превращается в no-op — язык не переключался
 * вовсе (регрессия обнаружена вручную на API 36). Прямой вызов LocaleManager не зависит
 * от AppCompat: система сама персистит выбор и пересоздаёт активности.
 *
 * Известное ограничение API 24-32: AppCompat-бэкпорт применяет локаль через
 * AppCompatActivity.attachBaseContext, которого у ComponentActivity нет, поэтому
 * live-переключение там не работает (выбор сохраняется только в DataStore-radio).
 * Полноценный фикс для <33 — ручной attachBaseContext-wrapper в MainActivity;
 * отложено, см. план v1.0.1.
 *
 * Ссылка: https://developer.android.com/guide/topics/resources/app-languages
 */
class LocaleHelper(
    private val context: Context,
) : LocaleApplier {
    override fun applyLanguageTag(tag: String?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(LocaleManager::class.java)
            localeManager.applicationLocales =
                if (tag.isNullOrBlank()) {
                    LocaleList.getEmptyLocaleList()
                } else {
                    LocaleList.forLanguageTags(tag)
                }
        } else {
            val locales =
                if (tag.isNullOrBlank()) {
                    LocaleListCompat.getEmptyLocaleList()
                } else {
                    LocaleListCompat.forLanguageTags(tag)
                }
            AppCompatDelegate.setApplicationLocales(locales)
        }
    }
}
