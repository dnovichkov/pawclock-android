package app.pawclock.domain.usecase

import app.pawclock.calculator.CatAgeCalculator
import app.pawclock.calculator.CatLifeStageCalculator
import app.pawclock.calculator.DogAgeCalculator
import app.pawclock.calculator.DogLifeStageCalculator
import app.pawclock.domain.pet.CalculatedAge
import app.pawclock.domain.pet.UnsupportedSpeciesException
import app.pawclock.domain.settings.SettingsReader
import app.pawclock.model.CalculationMethod
import app.pawclock.model.CatType
import app.pawclock.model.DogSize
import app.pawclock.model.Pet
import app.pawclock.model.Species
import java.time.Clock
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.first

/**
 * Рассчитывает возраст питомца и текущую стадию жизни.
 *
 * Алгоритм:
 *  1. Берёт календарный возраст в годах через `ChronoUnit.DAYS.between(birthDate, today) / 365.25`
 *     (с учётом високосных лет). [Clock] — параметр конструктора, чтобы тесты могли
 *     зафиксировать время через `Clock.fixed(...)`.
 *  2. Для собак выбирает метод: либо явно переданный [methodOverride], либо
 *     дефолтный из [SettingsReader]. Стадия жизни считается всегда через
 *     [DogLifeStageCalculator] и зависит от размера.
 *  3. Для кошек метод фиксирован — AAFP/AAHA 2021, метод сохраняется как `EPIGENETIC`
 *     в [CalculatedAge.method] (см. KDoc CalculatedAge).
 *  4. Если [Species] не реализован — бросает [UnsupportedSpeciesException].
 *
 * Дефолты subcategory, если [Pet.subcategory] не задан:
 *  - Dog → [DogSize.Medium] (срединная категория, минимизирует ошибку оценки)
 *  - Cat → [CatType.IndoorShortHair] (домашняя короткошёрстная — медиана)
 *
 * @param dogAgeCalculator калькулятор возраста собак (Wang / SIZE_BASED)
 * @param dogLifeStageCalculator калькулятор стадии жизни собак (AAHA 2019)
 * @param catAgeCalculator калькулятор возраста кошек (AAFP 2021)
 * @param catLifeStageCalculator калькулятор стадии жизни кошек (AAFP 2021)
 * @param settingsReader источник дефолтного метода (если [methodOverride] не задан)
 * @param clock источник времени; в production — `Clock.systemDefaultZone()`, в тестах — `Clock.fixed`
 */
class CalculatePetAgeUseCase(
    private val dogAgeCalculator: DogAgeCalculator,
    private val dogLifeStageCalculator: DogLifeStageCalculator,
    private val catAgeCalculator: CatAgeCalculator,
    private val catLifeStageCalculator: CatLifeStageCalculator,
    private val settingsReader: SettingsReader,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    /**
     * Рассчитывает возраст и стадию жизни для заданного питомца.
     *
     * @param pet питомец (должен пройти валидацию через [SavePetUseCase] перед сохранением)
     * @param methodOverride если задан — переопределяет дефолтный метод (для Quick Calculator).
     *   Для кошек игнорируется (всегда AAFP).
     * @throws UnsupportedSpeciesException если `pet.species.isImplemented == false`
     * @throws IllegalArgumentException если `pet.birthDate` в будущем (`ageInYears <= 0`)
     */
    suspend operator fun invoke(
        pet: Pet,
        methodOverride: CalculationMethod? = null,
    ): CalculatedAge {
        if (!pet.species.isImplemented) {
            throw UnsupportedSpeciesException(pet.species)
        }
        val today = LocalDate.now(clock)
        val ageInYears = calendarAgeInYears(pet.birthDate, today)
        require(ageInYears > 0) {
            "Pet's birthDate (${pet.birthDate}) must be in the past relative to $today"
        }
        return when (pet.species) {
            Species.Dog -> calculateDog(pet, ageInYears, methodOverride)
            Species.Cat -> calculateCat(pet, ageInYears)
            else -> throw UnsupportedSpeciesException(pet.species)
        }
    }

    private suspend fun calculateDog(
        pet: Pet,
        ageInYears: Double,
        methodOverride: CalculationMethod?,
    ): CalculatedAge {
        val method = methodOverride ?: settingsReader.observeDefaultCalculationMethod().first()
        val size = resolveDogSize(pet)
        val humanYears = dogAgeCalculator.toHumanYears(ageInYears, method, size)
        val lifeStage = dogLifeStageCalculator.determine(ageInYears, size)
        return CalculatedAge(
            ageInYears = ageInYears,
            humanYears = humanYears,
            lifeStage = lifeStage,
            method = method,
        )
    }

    private fun calculateCat(
        pet: Pet,
        ageInYears: Double,
    ): CalculatedAge {
        val type = resolveCatType(pet)
        val humanYears = catAgeCalculator.toHumanYears(ageInYears, type)
        val lifeStage = catLifeStageCalculator.determine(ageInYears, type)
        return CalculatedAge(
            ageInYears = ageInYears,
            humanYears = humanYears,
            lifeStage = lifeStage,
            method = CalculationMethod.EPIGENETIC,
        )
    }

    private fun resolveDogSize(pet: Pet): DogSize = pet.subcategory?.let(DogSize::fromId) ?: DogSize.Medium

    private fun resolveCatType(pet: Pet): CatType = pet.subcategory?.let(CatType::fromId) ?: CatType.IndoorShortHair

    private fun calendarAgeInYears(
        birthDate: LocalDate,
        today: LocalDate,
    ): Double = ChronoUnit.DAYS.between(birthDate, today).toDouble() / DAYS_PER_YEAR

    private companion object {
        /**
         * Среднее число дней в году с учётом високосных (юлианский год).
         * Точность достаточна для отображения возраста с одним знаком после запятой.
         */
        const val DAYS_PER_YEAR: Double = 365.25
    }
}
