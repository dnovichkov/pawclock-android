package app.pawclock.domain.pet

/**
 * Бросается [SavePetUseCase] при неудачной валидации [app.pawclock.model.Pet].
 *
 * Несёт одну или несколько [PetValidationError] — UI-слой мапит каждую ошибку
 * на локализованную строку через `stringResource(error.messageKey)`. Список
 * непустой по построению (см. `init`).
 *
 * @property errors список ошибок валидации (≥ 1)
 */
class PetValidationException(
    val errors: List<PetValidationError>,
) : IllegalArgumentException(
        "Pet validation failed: ${errors.joinToString { it.name }}",
    ) {
    init {
        require(errors.isNotEmpty()) {
            "PetValidationException must carry at least one error"
        }
    }
}
