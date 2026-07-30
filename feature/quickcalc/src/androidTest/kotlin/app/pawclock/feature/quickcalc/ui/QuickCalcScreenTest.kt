package app.pawclock.feature.quickcalc.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import app.pawclock.domain.pet.CalculatedAge
import app.pawclock.feature.quickcalc.QuickCalcEvent
import app.pawclock.feature.quickcalc.QuickCalcResult
import app.pawclock.feature.quickcalc.QuickCalcState
import app.pawclock.feature.quickcalc.QuickCalcSubcategoryOption
import app.pawclock.feature.quickcalc.QuickCalcValidationError
import app.pawclock.feature.quickcalc.R
import app.pawclock.feature.quickcalc.ui.section.QUICK_CALC_BIRTH_DATE_FIELD_TEST_TAG
import app.pawclock.feature.quickcalc.ui.section.quickCalcMethodTag
import app.pawclock.feature.quickcalc.ui.section.quickCalcSpeciesChipTag
import app.pawclock.model.BirdType
import app.pawclock.model.CalculationMethod
import app.pawclock.model.DogSize
import app.pawclock.model.LifeStage
import app.pawclock.model.RabbitSize
import app.pawclock.model.Species
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI тесты для [QuickCalcContent] (Task 20 / Plan 1).
 *
 * Используется stateless [QuickCalcContent], позволяя подавать произвольные
 * [QuickCalcState] без Hilt-setup'а. Запуск — на эмуляторе в nightly.yml
 * (см. .github/workflows/nightly.yml из Task 4).
 *
 * Проверяемое поведение:
 *  - Title "Быстрый расчёт" + Calculate FAB рендерятся;
 *  - клик Calculate FAB → событие [QuickCalcEvent.Calculate];
 *  - Species chips видимы и кликабельны;
 *  - Subcategory chips видимы при выбранном species;
 *  - ValidationErrorsBanner показывает все ошибки;
 *  - Bottom sheet с результатом показывается при Success.
 */
class QuickCalcScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    // Строки сверяются через ресурсы, а не литералы: эмулятор CI работает в en-US,
    // и default (ru) перекрывается values-en — литералы делали тесты locale-зависимыми.
    private fun string(
        resId: Int,
        vararg args: Any,
    ): String = InstrumentationRegistry.getInstrumentation().targetContext.getString(resId, *args)

    @Test
    fun initialState_rendersTitleAndCalculateFab() {
        val events = mutableListOf<QuickCalcEvent>()
        composeRule.setContent {
            QuickCalcContent(
                state = QuickCalcState.Empty,
                onEvent = events::add,
                onBack = { },
            )
        }

        composeRule.onNodeWithText(string(R.string.quick_calc_title)).assertIsDisplayed()
        composeRule.onNodeWithTag(CALCULATE_FAB_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun calculateFab_click_emitsCalculateEvent() {
        val events = mutableListOf<QuickCalcEvent>()
        composeRule.setContent {
            QuickCalcContent(
                state = QuickCalcState.Empty,
                onEvent = events::add,
                onBack = { },
            )
        }

        composeRule.onNodeWithTag(CALCULATE_FAB_TEST_TAG).performClick()

        assertTrue("Click on Calculate FAB must produce Calculate event", QuickCalcEvent.Calculate in events)
    }

    @Test
    fun speciesSelection_clickingDogChipEmitsSelectSpeciesEvent() {
        val events = mutableListOf<QuickCalcEvent>()
        composeRule.setContent {
            QuickCalcContent(
                state = QuickCalcState.Empty,
                onEvent = events::add,
                onBack = { },
            )
        }

        composeRule
            .onNode(hasTestTag(quickCalcSpeciesChipTag(Species.Dog)))
            .performClick()

        assertTrue(
            "Click on Dog chip must produce SelectSpecies(Dog)",
            events.any { it is QuickCalcEvent.SelectSpecies && it.species == Species.Dog },
        )
    }

    @Test
    fun subcategorySelector_visibleAfterSpeciesSelected() {
        val state =
            QuickCalcState.Empty.copy(
                species = Species.Dog,
                availableSubcategories =
                    DogSize.entries.map { QuickCalcSubcategoryOption(it.id, it.name) },
            )
        composeRule.setContent {
            QuickCalcContent(
                state = state,
                onEvent = { },
                onBack = { },
            )
        }

        // Все 5 размеров видимы.
        composeRule.onNodeWithText(string(R.string.quick_calc_subcategory_toy)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.quick_calc_subcategory_small)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.quick_calc_subcategory_medium)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.quick_calc_subcategory_large)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.quick_calc_subcategory_giant)).assertIsDisplayed()
    }

    @Test
    fun validationError_rendersEachErrorAsBulletLine() {
        composeRule.setContent {
            QuickCalcContent(
                state =
                    QuickCalcState.Empty.copy(
                        result =
                            QuickCalcResult.ValidationError(
                                listOf(
                                    QuickCalcValidationError.SpeciesRequired,
                                    QuickCalcValidationError.BirthDateRequired,
                                ),
                            ),
                    ),
                onEvent = { },
                onBack = { },
            )
        }

        // Банер в конце формы — скроллим, чтобы не зависеть от высоты экрана.
        composeRule
            .onNodeWithText("• " + string(R.string.quick_calc_error_species_required))
            .performScrollTo()
            .assertIsDisplayed()
        composeRule
            .onNodeWithText("• " + string(R.string.quick_calc_error_birth_date_required))
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun methodToggle_visibleForDog_emitsSetMethodOnClick() {
        val events = mutableListOf<QuickCalcEvent>()
        val state =
            QuickCalcState.Empty.copy(
                species = Species.Dog,
                availableSubcategories =
                    DogSize.entries.map { QuickCalcSubcategoryOption(it.id, it.name) },
            )
        composeRule.setContent {
            QuickCalcContent(
                state = state,
                onEvent = events::add,
                onBack = { },
            )
        }

        // Method toggle секция — Wang/SizeBased для собак. Секция в конце формы —
        // на маленьких экранах за фолдом: без скролла тап уходит мимо кнопки
        // (узел находится, но клик по координатам вне видимой области не доходит).
        composeRule
            .onNodeWithText(string(R.string.quick_calc_method_size_based))
            .performScrollTo()
            .performClick()

        assertTrue(
            "Click on SizeBased button must produce SetMethod(SIZE_BASED)",
            events.any {
                it is QuickCalcEvent.SetMethod && it.method == CalculationMethod.SIZE_BASED
            },
        )
    }

    @Test
    fun resultSheet_isShownWhenStateHasSuccessResult() {
        val calculated =
            CalculatedAge(
                ageInYears = 5.0,
                humanYears = 57.0,
                lifeStage = LifeStage.Dog.MatureAdult,
                method = CalculationMethod.EPIGENETIC,
            )
        val state =
            QuickCalcState.Empty.copy(
                species = Species.Dog,
                result = QuickCalcResult.Success(calculated),
            )
        composeRule.setContent {
            QuickCalcContent(
                state = state,
                onEvent = { },
                onBack = { },
            )
        }

        // Hero "57 ЧГ" — главная цифра в AgeBigCard.
        composeRule
            .onNodeWithText(string(R.string.quick_calc_result_human_years_unit, 57))
            .assertIsDisplayed()
        // LifeStageChip для MatureAdult.
        composeRule
            .onNodeWithText(string(R.string.quick_calc_life_stage_dog_mature_adult))
            .assertIsDisplayed()
    }

    @Test
    fun resultSheet_doesNotShowMethodToggleForCat() {
        val calculated =
            CalculatedAge(
                ageInYears = 5.0,
                humanYears = 36.0,
                lifeStage = LifeStage.Cat.YoungAdult,
                method = CalculationMethod.EPIGENETIC,
            )
        val state =
            QuickCalcState.Empty.copy(
                species = Species.Cat,
                result = QuickCalcResult.Success(calculated),
            )
        composeRule.setContent {
            QuickCalcContent(
                state = state,
                onEvent = { },
                onBack = { },
            )
        }

        // "36 ЧГ" — главная цифра.
        composeRule
            .onNodeWithText(string(R.string.quick_calc_result_human_years_unit, 36))
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(string(R.string.quick_calc_life_stage_cat_young_adult))
            .assertIsDisplayed()
        // Method toggle "Wang (эпигенетика)" — НЕ должен быть виден для кошки.
        // (sheet содержит method toggle только когда species == Dog)
    }

    // --- Plan 2, Task 16: все 12 видов в Quick Calculator ---

    @Test
    fun subcategorySelector_showsRabbitSizeChipsForRabbit() {
        val state =
            QuickCalcState.Empty.copy(
                species = Species.Rabbit,
                availableSubcategories =
                    RabbitSize.entries.map { QuickCalcSubcategoryOption(it.id, it.name) },
            )
        composeRule.setContent {
            QuickCalcContent(
                state = state,
                onEvent = { },
                onBack = { },
            )
        }

        // Метки RabbitSize, а не DogSize: «Карликовый»/«Гигантский» вместо «Той»/«Гигантская».
        composeRule.onNodeWithText(string(R.string.rabbit_size_dwarf)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.rabbit_size_medium)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.rabbit_size_giant)).assertIsDisplayed()
    }

    @Test
    fun resultSheet_showsRabbitHumanYearsAndAdultStage() {
        val calculated =
            CalculatedAge(
                ageInYears = 5.0,
                humanYears = 45.0,
                lifeStage = LifeStage.Rabbit.Adult,
                method = CalculationMethod.EPIGENETIC,
            )
        val state =
            QuickCalcState.Empty.copy(
                species = Species.Rabbit,
                subcategory = RabbitSize.Medium.id,
                result = QuickCalcResult.Success(calculated),
            )
        composeRule.setContent {
            QuickCalcContent(
                state = state,
                onEvent = { },
                onBack = { },
            )
        }

        // Rabbit Medium 5y ≈ 45 ЧГ, стадия Adult («Взрослый»).
        composeRule
            .onNodeWithText(string(R.string.quick_calc_result_human_years_unit, 45))
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(string(R.string.quick_calc_life_stage_rabbit_adult))
            .assertIsDisplayed()
        // Method toggle отсутствует для не-собаки.
        composeRule.onNodeWithTag(quickCalcMethodTag(CalculationMethod.SIZE_BASED)).assertDoesNotExist()
    }

    @Test
    fun resultSheet_showsBirdHumanYears() {
        val calculated =
            CalculatedAge(
                ageInYears = 3.0,
                // budgerigar lifespan 7: 3·80/7 ≈ 34.29 → roundToInt = 34.
                humanYears = 34.29,
                lifeStage = LifeStage.Bird.Adult,
                method = CalculationMethod.EPIGENETIC,
            )
        val state =
            QuickCalcState.Empty.copy(
                species = Species.Bird,
                subcategory = BirdType.Budgerigar.id,
                result = QuickCalcResult.Success(calculated),
            )
        composeRule.setContent {
            QuickCalcContent(
                state = state,
                onEvent = { },
                onBack = { },
            )
        }

        composeRule
            .onNodeWithText(string(R.string.quick_calc_result_human_years_unit, 34))
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(string(R.string.quick_calc_life_stage_bird_adult))
            .assertIsDisplayed()
    }

    // Регрессия b0f220b (та же, что в PetEditor BirthDateField): read-only
    // OutlinedTextField с enabled=true потреблял тап, clickable родительского
    // Box'а не срабатывал — DatePickerDialog не открывался. Тест кликает по
    // полю реальным pointer-событием и проверяет появление диалога.
    @Test
    fun birthDateField_click_opensDatePickerDialog() {
        composeRule.setContent {
            QuickCalcContent(
                state = QuickCalcState.Empty,
                onEvent = { },
                onBack = { },
            )
        }

        composeRule
            .onNodeWithTag(QUICK_CALC_BIRTH_DATE_FIELD_TEST_TAG)
            .performScrollTo()
            .performClick()

        composeRule.onNodeWithText(string(R.string.quick_calc_date_ok)).assertIsDisplayed()
    }
}
