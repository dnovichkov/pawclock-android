package app.pawclock.feature.pets.list

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import app.pawclock.feature.pets.R
import app.pawclock.feature.pets.list.ui.ADD_PET_FAB_TEST_TAG
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

    // Строки сверяются через ресурсы, а не литералы: эмулятор CI работает в en-US,
    // и default (ru) перекрывается values-en — литералы делали тесты locale-зависимыми.
    private fun string(resId: Int): String = InstrumentationRegistry.getInstrumentation().targetContext.getString(resId)

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
        composeRule.onNodeWithText(string(R.string.pets_list_empty_title)).assertIsDisplayed()
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
        // FAB ищется по тегу: M3 1.4 вычищает text-слот Extended FAB из semantics.
        composeRule.onNodeWithTag(ADD_PET_FAB_TEST_TAG).performClick()
        assert(addClicked) { "onAddPetClick should be called when FAB is clicked" }
    }
}
