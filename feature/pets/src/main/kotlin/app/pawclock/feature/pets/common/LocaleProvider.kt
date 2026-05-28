package app.pawclock.feature.pets.common

import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Источник текущей BCP 47 locale tag (например, `"ru"`, `"en"`).
 *
 * Inject'ится в ViewModel'и (например, [app.pawclock.feature.pets.detail.PetDetailViewModel]),
 * которым нужна locale для загрузки care-рекомендаций.
 *
 * Не возвращается `Flow` — на этапе Plan 1 in-app смена локали (Task 22)
 * рекомендуется через `AppCompatDelegate.setApplicationLocales`, что пересоздаёт
 * Activity и вместе с ней ViewModel. Поэтому достаточно snapshot'а на момент создания.
 *
 * Тесты подменяют через прямую инстанциацию (см. PetDetailViewModelTest).
 */
@Singleton
open class LocaleProvider
    @Inject
    constructor() {
        open fun current(): String = Locale.getDefault().language
    }
