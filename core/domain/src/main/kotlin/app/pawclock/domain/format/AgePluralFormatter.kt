package app.pawclock.domain.format

/**
 * Форматтер возраста с учётом CLDR-правил множественного числа.
 *
 * Pure-Kotlin реализация: не зависит от Android Resources, поэтому может быть
 * использована в любом модуле (домен/feature) и легко юнит-тестируется без
 * Robolectric или AndroidContext.
 *
 * В Composables предпочтительнее `pluralStringResource(R.plurals.age_years, n, n)` —
 * Android knows CLDR rules для всех локалей "из коробки". Этот форматтер нужен
 * для не-UI кода (логирование, экспорт CSV/JSON в Plan 2, тестируемые формирователи
 * сообщений) и для случаев, где Resources недоступен.
 *
 * Поддерживаемые языки (Plan 1):
 *   • ru — три формы (one/few/many)
 *   • en — две формы (one/other)
 *
 * Незнакомые языки fall-back'ятся на английский (минимально консистентное поведение
 * вместо crash'а — UX-важно при добавлении новых локалей в Plan 2/3).
 *
 * Ссылка на CLDR Plural Rules:
 * https://unicode-org.github.io/cldr/ldml/tr35-numbers.html#Language_Plural_Rules
 */
class AgePluralFormatter {
    /**
     * Возвращает строку вида "5 лет", "1 год", "2 years" — число + правильная форма
     * слова "год/year".
     *
     * @param years возраст в полных годах; ≥ 0
     * @param localeTag BCP 47 tag ("ru", "en", "ru-RU", "en-US"); region игнорируется
     * @throws IllegalArgumentException если years < 0
     */
    fun formatYears(
        years: Int,
        localeTag: String,
    ): String {
        require(years >= 0) { "years must be non-negative, was $years" }
        val language = normalizeLanguage(localeTag)
        val word =
            when (language) {
                "ru" -> russianForm(years)
                else -> englishForm(years)
            }
        return "$years $word"
    }

    private fun normalizeLanguage(localeTag: String): String =
        localeTag
            .substringBefore('-')
            .substringBefore('_')
            .lowercase()

    /**
     * CLDR ru rule:
     *   one: v=0 и i mod 10 = 1 и i mod 100 != 11
     *   few: v=0 и i mod 10 ∈ 2..4 и i mod 100 ∉ 12..14
     *   many: иначе
     * Для целочисленных возрастов v=0 всегда выполнено.
     */
    private fun russianForm(years: Int): String {
        val mod10 = years % MOD_DECIMAL
        val mod100 = years % MOD_CENTURY
        return when {
            mod10 == 1 && mod100 != TEEN_EXCEPTION_ONE -> "год"
            mod10 in FEW_RANGE && mod100 !in TEEN_EXCEPTION_FEW -> "года"
            else -> "лет"
        }
    }

    private fun englishForm(years: Int): String = if (years == 1) "year" else "years"

    private companion object {
        const val MOD_DECIMAL = 10
        const val MOD_CENTURY = 100
        const val TEEN_EXCEPTION_ONE = 11
        val FEW_RANGE = 2..4
        val TEEN_EXCEPTION_FEW = 12..14
    }
}
