package app.pawclock.feature.pets.list

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import app.pawclock.feature.pets.list.ui.PetsListContent
import app.pawclock.model.Pet
import app.pawclock.model.Species
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI тесты для PetsListScreen (Task 18 / §11.8 спецификации).
 *
 * Запускаются как instrumented androidTest на эмуляторе/устройстве — в local sandbox
 * не выполняются (требуется Android runtime), но компилируются в assembleDebugAndroidTest
 * для последующего запуска в nightly.yml (reactivecircus/android-emulator-runner).
 *
 * Используется [PetsListContent] — stateless вариант экрана без Hilt-инжекций, чтобы
 * тест мог подавать произвольные [PetsListState] напрямую.
 */
class PetsListScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val sampleDate: LocalDate = LocalDate.of(2024, 1, 1)

    private fun pet(
        id: Long,
        name: String,
    ): Pet =
        Pet(
            id = id,
            name = name,
            species = Species.Dog,
            birthDate = sampleDate,
            subcategory = "medium",
        )

    @Test
    fun shows_empty_state_when_no_pets() {
        composeRule.setContent {
            PetsListContent(
                state = PetsListState.Empty,
                onPetClick = {},
                onAddPetClick = {},
            )
        }
        composeRule.onNodeWithText("Добавьте первого питомца").assertIsDisplayed()
    }

    @Test
    fun shows_pet_cards_when_pets_exist() {
        composeRule.setContent {
            PetsListContent(
                state = PetsListState.Success(listOf(pet(1L, "Rex"), pet(2L, "Барсик"))),
                onPetClick = {},
                onAddPetClick = {},
            )
        }
        composeRule.onNodeWithText("Rex").assertIsDisplayed()
        composeRule.onNodeWithText("Барсик").assertIsDisplayed()
    }

    @Test
    fun clicking_fab_triggers_add_pet_event() {
        var addClicked = false
        composeRule.setContent {
            PetsListContent(
                state = PetsListState.Empty,
                onPetClick = {},
                onAddPetClick = { addClicked = true },
            )
        }
        composeRule.onNodeWithText("Добавить").performClick()
        assert(addClicked) { "onAddPetClick should be called when FAB is clicked" }
    }
}
