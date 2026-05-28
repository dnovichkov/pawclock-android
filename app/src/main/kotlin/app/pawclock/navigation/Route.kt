package app.pawclock.navigation

import kotlinx.serialization.Serializable

/**
 * Typesafe routes для Navigation Compose 2.8+ (Task 17 / Plan 1).
 *
 * Navigation Compose 2.8 поддерживает `composable<Route> { ... }` с автоматической
 * сериализацией параметров через kotlinx.serialization — это устраняет stringly-typed
 * paths (`"pet/{petId}"`) и compile-time валидирует имена и типы аргументов.
 *
 * Использование:
 * ```
 * NavHost(navController, startDestination = Route.PetsList) {
 *     composable<Route.PetsList> { PetsListScreen(...) }
 *     composable<Route.PetDetail> { entry ->
 *         val args = entry.toRoute<Route.PetDetail>()
 *         PetDetailScreen(args.petId)
 *     }
 * }
 *
 * navController.navigate(Route.PetDetail(petId = 42L))
 * ```
 *
 * Каждый объект/класс должен быть `@Serializable` — это требование Navigation 2.8+ API.
 * Sealed-иерархия не обязательна для самого framework'а, но используется для
 * exhaustiveness-чеков в `when`-выражениях.
 *
 * Соответствие экранам:
 *  - [PetsList] — главный экран, список питомцев (Task 18)
 *  - [PetDetail] — детальный экран питомца с care-рекомендациями (Task 18)
 *  - [PetEditor] — создание/редактирование. `petId == null` = новый питомец (Task 19)
 *  - [QuickCalculator] — одноразовый расчёт без сохранения (Task 20)
 *  - [Settings] — настройки приложения (Task 21)
 *  - [About] — About-экран с лицензией и источниками (Task 21)
 */
sealed interface Route {
    @Serializable
    data object PetsList : Route

    @Serializable
    data class PetDetail(
        val petId: Long,
    ) : Route

    @Serializable
    data class PetEditor(
        val petId: Long? = null,
    ) : Route

    @Serializable
    data object QuickCalculator : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data object About : Route
}
