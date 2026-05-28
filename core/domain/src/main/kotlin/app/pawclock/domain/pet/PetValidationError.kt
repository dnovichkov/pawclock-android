package app.pawclock.domain.pet

/**
 * Типизированные ошибки валидации [app.pawclock.model.Pet] на уровне домена.
 *
 * Каждая ошибка несёт стабильный [messageKey] — он используется UI-слоем для
 * lookup локализованной строки в `strings.xml` (см. Task 22). НИ В КОЕМ СЛУЧАЕ
 * не нужно показывать пользователю английский [name] enum-константы или текст
 * исключения напрямую — UI должен мапить ключ → строку через `stringResource(...)`.
 *
 * Локализационные ключи (резервируются для Task 22):
 *  - `pet_validation_name_blank` → "Введите имя питомца"
 *  - `pet_validation_birthdate_in_future` → "Дата рождения не может быть в будущем"
 *  - `pet_validation_birthdate_unrealistic` → "Дата рождения слишком давно"
 */
enum class PetValidationError(
    val messageKey: String,
) {
    NameBlank(messageKey = "pet_validation_name_blank"),
    BirthDateInFuture(messageKey = "pet_validation_birthdate_in_future"),
    BirthDateUnrealistic(messageKey = "pet_validation_birthdate_unrealistic"),
}
