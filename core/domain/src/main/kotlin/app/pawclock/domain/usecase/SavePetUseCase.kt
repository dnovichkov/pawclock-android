package app.pawclock.domain.usecase

import app.pawclock.domain.pet.PetRepository
import app.pawclock.domain.pet.PetValidationError
import app.pawclock.domain.pet.PetValidationException
import app.pawclock.domain.pet.UnsupportedSpeciesException
import app.pawclock.model.Pet
import java.time.Clock
import java.time.LocalDate

/**
 * Сохраняет (insert или update) питомца после валидации.
 *
 * Правила валидации (см. [PetValidationError]):
 *  - имя должно быть непустым после `trim()`; `Pet`-конструктор уже отбрасывает blank-имена
 *    через `require(name.isNotBlank())`, но UseCase делает явную проверку, чтобы вернуть
 *    типизированную [PetValidationError.NameBlank] вместо `IllegalArgumentException`;
 *  - `birthDate` должна быть строго в прошлом (запрещены как future-даты, так и `today`);
 *    причина запрета `today`: [CalculatePetAgeUseCase] требует `ageInYears > 0`, иначе
 *    бросает IAE — без этой симметрии Save принимал бы newborn'а, но Detail-экран сразу
 *    падал бы в Error;
 *  - `birthDate` не может быть нереально давно (раньше, чем [EARLIEST_REALISTIC_BIRTH_YEAR]) —
 *    защита от случайного выбора 1900-х годов через старый API DatePicker.
 *
 * Дополнительно бросает [UnsupportedSpeciesException] для нереализованных видов
 * (только Dog/Cat в Plan 1, остальные — stubs).
 *
 * Различение insert / update делается по `pet.id`:
 *  - `id == 0L` → [PetRepository.insert] (возвращает авто-id)
 *  - `id != 0L` → [PetRepository.update] (возвращает Unit)
 *
 * @return id сохранённого питомца (для insert — новый; для update — тот же `pet.id`)
 * @throws PetValidationException если валидация не прошла; список ошибок несёт ВСЕ нарушения,
 *   а не первое — UI отображает все одновременно (например, и пустое имя, и плохую дату)
 * @throws UnsupportedSpeciesException если `pet.species.isImplemented == false`
 */
class SavePetUseCase(
    private val petRepository: PetRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    suspend operator fun invoke(pet: Pet): Long {
        if (!pet.species.isImplemented) {
            throw UnsupportedSpeciesException(pet.species)
        }
        val errors = validate(pet)
        if (errors.isNotEmpty()) {
            throw PetValidationException(errors)
        }
        return if (pet.id == 0L) {
            petRepository.insert(pet)
        } else {
            petRepository.update(pet)
            pet.id
        }
    }

    private fun validate(pet: Pet): List<PetValidationError> {
        val today = LocalDate.now(clock)
        return buildList {
            if (pet.name.trim().isEmpty()) add(PetValidationError.NameBlank)
            if (!pet.birthDate.isBefore(today)) add(PetValidationError.BirthDateInFuture)
            if (pet.birthDate.year < EARLIEST_REALISTIC_BIRTH_YEAR) {
                add(PetValidationError.BirthDateUnrealistic)
            }
        }
    }

    private companion object {
        /**
         * Граница «нереалистичной» даты рождения. Любая дата раньше 1990 года —
         * почти наверняка ошибка пользователя (максимальная зафиксированная продолжительность
         * жизни домашних животных не превышает 30 лет; запас в ~6 лет от 2026 → 1996,
         * округлено до 1990 для запаса на лошадей).
         */
        const val EARLIEST_REALISTIC_BIRTH_YEAR: Int = 1990
    }
}
