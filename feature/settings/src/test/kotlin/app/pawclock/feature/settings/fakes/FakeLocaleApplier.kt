package app.pawclock.feature.settings.fakes

import app.pawclock.domain.locale.LocaleApplier

/**
 * In-memory fake [LocaleApplier] для unit-тестов [app.pawclock.feature.settings.SettingsViewModel].
 *
 * Журналирует все вызовы `applyLanguageTag()` в список [appliedTags], что позволяет
 * проверить:
 *   • что ViewModel вызывает applier (fire-and-forget семантика отдельно от persistence);
 *   • с правильным language tag'ом ("ru", "en", null);
 *   • в правильном порядке (synchronous до launch'a corutine'ы для persistence).
 */
class FakeLocaleApplier : LocaleApplier {
    private val _appliedTags = mutableListOf<String?>()
    val appliedTags: List<String?> get() = _appliedTags.toList()

    val applyCount: Int get() = _appliedTags.size

    /**
     * Возвращает последний применённый language tag или `null`, если applier ещё не вызывался.
     *
     * Использует [List.lastOrNull] вместо [List.last], чтобы тест-кейсы, проверяющие
     * "applier не был вызван", могли безопасно обратиться к [lastTag] без NoSuchElementException.
     */
    val lastTag: String? get() = _appliedTags.lastOrNull()

    override fun applyLanguageTag(tag: String?) {
        _appliedTags += tag
    }
}
