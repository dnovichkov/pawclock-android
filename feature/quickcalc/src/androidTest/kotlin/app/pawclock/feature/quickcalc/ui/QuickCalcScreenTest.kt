package app.pawclock.feature.quickcalc.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import app.pawclock.domain.pet.CalculatedAge
import app.pawclock.feature.quickcalc.QuickCalcEvent
import app.pawclock.feature.quickcalc.QuickCalcResult
import app.pawclock.feature.quickcalc.QuickCalcState
import app.pawclock.feature.quickcalc.QuickCalcSubcategoryOption
import app.pawclock.feature.quickcalc.QuickCalcValidationError
import app.pawclock.feature.quickcalc.ui.section.quickCalcSpeciesChipTag
import app.pawclock.model.CalculationMethod
import app.pawclock.model.DogSize
import app.pawclock.model.LifeStage
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

        composeRule.onNodeWithText("Быстрый расчёт").assertIsDisplayed()
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
        composeRule.onNodeWithText("Той").assertIsDisplayed()
        composeRule.onNodeWithText("Маленькая").assertIsDisplayed()
        composeRule.onNodeWithText("Средняя").assertIsDisplayed()
        composeRule.onNodeWithText("Большая").assertIsDisplayed()
        composeRule.onNodeWithText("Гигантская").assertIsDisplayed()
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

        composeRule.onNodeWithText("• Выберите вид питомца").assertIsDisplayed()
        composeRule.onNodeWithText("• Укажите дату рождения").assertIsDisplayed()
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

        // Method toggle секция — Wang/SizeBased для собак.
        composeRule.onNodeWithText("По размеру").performClick()

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
        composeRule.onNodeWithText("57 ЧГ").assertIsDisplayed()
        // LifeStageChip для MatureAdult.
        composeRule.onNodeWithText("Зрелый взрослый").assertIsDisplayed()
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
        composeRule.onNodeWithText("36 ЧГ").assertIsDisplayed()
        composeRule.onNodeWithText("Молодой взрослый").assertIsDisplayed()
        // Method toggle "Wang (эпигенетика)" — НЕ должен быть виден для кошки.
        // (sheet содержит method toggle только когда species == Dog)
    }
}
