package app.pawclock.domain.pet

import app.pawclock.model.Species

/**
 * Бросается доменными UseCase'ами (см. [CalculatePetAgeUseCase]) при попытке рассчитать
 * возраст для вида с `Species.isImplemented = false`.
 *
 * В Plan 1 поддерживаются только [Species.Dog] и [Species.Cat]. Остальные 10 видов из §4
 * спецификации объявлены как stubs до Plan 2 и должны быть обработаны UI как "coming soon",
 * **не должно быть возможности сохранить такого питомца** в Plan 1.
 *
 * UI ловит это исключение и показывает диалог «Этот вид пока не поддерживается».
 *
 * @property species вид, который не имеет реализации формулы расчёта
 */
class UnsupportedSpeciesException(
    val species: Species,
) : IllegalStateException(
        "Species '${species.id}' is not implemented yet (isImplemented=${species.isImplemented})",
    )
