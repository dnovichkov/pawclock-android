package app.pawclock.domain.usecase

import app.pawclock.calculator.AgeCalculator
import app.pawclock.calculator.LifeStageCalculator
import app.pawclock.calculator.SpeciesParams
import app.pawclock.domain.pet.CalculatedAge
import app.pawclock.domain.pet.UnsupportedSpeciesException
import app.pawclock.domain.settings.SettingsReader
import app.pawclock.model.BirdType
import app.pawclock.model.CalculationMethod
import app.pawclock.model.CatType
import app.pawclock.model.DogSize
import app.pawclock.model.HamsterType
import app.pawclock.model.HorseType
import app.pawclock.model.Pet
import app.pawclock.model.RabbitSize
import app.pawclock.model.ReptileType
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
 *  2. Диспатчит по виду через [AgeCalculator.forSpecies] / [LifeStageCalculator.forSpecies]
 *     (Strategy pattern). Если для вида нет калькулятора (`forSpecies == null`), бросает
 *     [UnsupportedSpeciesException] — единая проверка реализованности вместо `pet.species.isImplemented`.
 *  3. Параметры вида ([SpeciesParams]) и итоговый [CalculatedAge.method] резолвятся в
 *     [resolveParams]: для собак выбирается метод (override либо дефолт из [SettingsReader]),
 *     для кошек метод фиксирован — AAFP 2021, сохраняется как `EPIGENETIC` (см. KDoc CalculatedAge).
 *
 * Дефолты subcategory, если [Pet.subcategory] не задан:
 *  - Dog → [DogSize.Medium] (срединная категория, минимизирует ошибку оценки)
 *  - Cat → [CatType.IndoorShortHair] (домашняя короткошёрстная — медиана)
 *  - Rabbit → [RabbitSize.Medium] (срединная категория)
 *  - Hamster → [HamsterType.Syrian] (самый распространённый вид)
 *  - Bird → [BirdType.Budgerigar] (волнистый попугай — самый распространённый вид)
 *  - Reptile → [ReptileType.BeardedDragon] (бородатая агама — самая распространённая «стартовая» рептилия)
 *  - Horse → [HorseType.LightHorse] (верховая лошадь — медианная категория по ЧЖ)
 *
 * Калькуляторы — `data object`-синглтоны (stateless), поэтому больше не инжектируются:
 * UseCase обращается к ним напрямую через фабрики. Это убирает разрастающийся
 * конструктор (12 видов × 2 калькулятора) и держит граф DI минимальным.
 *
 * @param settingsReader источник дефолтного метода (если `methodOverride` не задан)
 * @param clock источник времени; в production — `Clock.systemDefaultZone()`, в тестах — `Clock.fixed`
 */
class CalculatePetAgeUseCase(
    private val settingsReader: SettingsReader,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    /**
     * Рассчитывает возраст и стадию жизни для заданного питомца.
     *
     * @param pet питомец (должен пройти валидацию через [SavePetUseCase] перед сохранением)
     * @param methodOverride если задан — переопределяет дефолтный метод (для Quick Calculator).
     *   Для кошек игнорируется (всегда AAFP).
     * @throws UnsupportedSpeciesException если для `pet.species` нет калькулятора (вид не реализован)
     * @throws IllegalArgumentException если `pet.birthDate` в будущем (`ageInYears <= 0`)
     */
    suspend operator fun invoke(
        pet: Pet,
        methodOverride: CalculationMethod? = null,
    ): CalculatedAge {
        val ageCalculator =
            AgeCalculator.forSpecies(pet.species) ?: throw UnsupportedSpeciesException(pet.species)
        val lifeStageCalculator =
            LifeStageCalculator.forSpecies(pet.species) ?: throw UnsupportedSpeciesException(pet.species)

        val today = LocalDate.now(clock)
        val ageInYears = calendarAgeInYears(pet.birthDate, today)
        require(ageInYears > 0) {
            "Pet's birthDate (${pet.birthDate}) must be in the past relative to $today"
        }

        val (params, method) = resolveParams(pet, methodOverride)
        return CalculatedAge(
            ageInYears = ageInYears,
            humanYears = ageCalculator.toHumanYears(ageInYears, params),
            lifeStage = lifeStageCalculator.determine(ageInYears, params),
            method = method,
        )
    }

    /**
     * Резолвит видоспецифичные параметры расчёта и метод, попадающий в [CalculatedAge.method].
     *
     * Это единственная точка с `when (pet.species)` — она лишь собирает [SpeciesParams]
     * (с дефолтами подкатегории) и выбирает метод; сами формулы инкапсулированы в калькуляторах.
     * При добавлении вида в Plan 2 сюда добавляется одна ветка.
     */
    private suspend fun resolveParams(
        pet: Pet,
        methodOverride: CalculationMethod?,
    ): Pair<SpeciesParams, CalculationMethod> =
        when (pet.species) {
            Species.Dog -> {
                val method = methodOverride ?: settingsReader.observeDefaultCalculationMethod().first()
                SpeciesParams.Dog(method = method, size = resolveDogSize(pet)) to method
            }
            Species.Cat ->
                SpeciesParams.Cat(type = resolveCatType(pet)) to CalculationMethod.EPIGENETIC
            Species.Rabbit ->
                SpeciesParams.Rabbit(size = resolveRabbitSize(pet)) to CalculationMethod.EPIGENETIC
            Species.Hamster ->
                SpeciesParams.Hamster(type = resolveHamsterType(pet)) to CalculationMethod.EPIGENETIC
            Species.GuineaPig ->
                SpeciesParams.GuineaPig to CalculationMethod.EPIGENETIC
            Species.Rat ->
                SpeciesParams.Rat to CalculationMethod.EPIGENETIC
            Species.Mouse ->
                SpeciesParams.Mouse to CalculationMethod.EPIGENETIC
            Species.Ferret ->
                SpeciesParams.Ferret to CalculationMethod.EPIGENETIC
            Species.Bird ->
                SpeciesParams.Bird(type = resolveBirdType(pet)) to CalculationMethod.EPIGENETIC
            Species.Reptile ->
                SpeciesParams.Reptile(type = resolveReptileType(pet)) to CalculationMethod.EPIGENETIC
            Species.Horse ->
                SpeciesParams.Horse(type = resolveHorseType(pet)) to CalculationMethod.EPIGENETIC
            else -> throw UnsupportedSpeciesException(pet.species)
        }

    private fun resolveDogSize(pet: Pet): DogSize = pet.subcategory?.let(DogSize::fromId) ?: DogSize.Medium

    private fun resolveCatType(pet: Pet): CatType = pet.subcategory?.let(CatType::fromId) ?: CatType.IndoorShortHair

    private fun resolveRabbitSize(pet: Pet): RabbitSize = pet.subcategory?.let(RabbitSize::fromId) ?: RabbitSize.Medium

    private fun resolveHamsterType(pet: Pet): HamsterType =
        pet.subcategory?.let(HamsterType::fromId) ?: HamsterType.Syrian

    private fun resolveBirdType(pet: Pet): BirdType =
        pet.subcategory?.let(BirdType::fromId) ?: BirdType.Budgerigar

    private fun resolveReptileType(pet: Pet): ReptileType =
        pet.subcategory?.let(ReptileType::fromId) ?: ReptileType.BeardedDragon

    private fun resolveHorseType(pet: Pet): HorseType =
        pet.subcategory?.let(HorseType::fromId) ?: HorseType.LightHorse

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
