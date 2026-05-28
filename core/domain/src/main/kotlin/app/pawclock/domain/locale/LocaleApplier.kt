package app.pawclock.domain.locale

/**
 * Узкий port для применения локали приложения. Position в domain-слое — чтобы
 * feature-модули (например, SettingsViewModel) могли вызывать смену языка через
 * абстракцию, не зная про Android-specific AppCompatDelegate.
 *
 * Adapter реализация: `:app/locale/LocaleHelper` — оборачивает
 * `AppCompatDelegate.setApplicationLocales` и регистрируется как @Binds в Hilt-модуле
 * `:app/data/locale/di/LocaleModule`.
 *
 * Тесты подкладывают `FakeLocaleApplier` чтобы verify, что ViewModel вызывает applier
 * с правильным language tag'ом.
 */
fun interface LocaleApplier {
    /**
     * Применить язык приложения.
     *
     * @param tag BCP 47 language tag ("ru", "en", "ru-RU") или `null` — "следовать системе".
     */
    fun applyLanguageTag(tag: String?)
}
