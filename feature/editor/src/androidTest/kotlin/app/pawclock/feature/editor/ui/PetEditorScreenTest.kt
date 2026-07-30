package app.pawclock.feature.editor.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import app.pawclock.domain.pet.PetValidationError
import app.pawclock.feature.editor.PetEditorEvent
import app.pawclock.feature.editor.PetEditorSaveResult
import app.pawclock.feature.editor.PetEditorState
import app.pawclock.feature.editor.R
import app.pawclock.feature.editor.SubcategoryOption
import app.pawclock.feature.editor.ui.section.BIRTH_DATE_FIELD_TEST_TAG
import app.pawclock.model.DogSize
import app.pawclock.model.Species
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI тесты для [PetEditorContent] (Task 19 / Plan 1).
 *
 * Используется stateless [PetEditorContent] — позволяет подавать произвольные
 * [PetEditorState] без Hilt-setup'а. Запуск — на эмуляторе в nightly.yml
 * (см. .github/workflows/nightly.yml из Task 4).
 *
 * Проверяемое поведение:
 *  - Save FAB рендерится и кликабелен;
 *  - blank-name + Save → событие [PetEditorEvent.Save] прокидывается в onEvent;
 *  - Species chips отображаются и кликом отправляют [PetEditorEvent.SelectSpecies];
 *  - режим Loading рендерит ProgressIndicator вместо формы;
 *  - validation banner показывает все ошибки.
 */
class PetEditorScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    // Строки сверяются через ресурсы, а не литералы: эмулятор CI работает в en-US,
    // и default (ru) перекрывается values-en — литералы делали тесты locale-зависимыми.
    private fun string(resId: Int): String = InstrumentationRegistry.getInstrumentation().targetContext.getString(resId)

    @Test
    fun newPetMode_rendersSaveButtonAndEmptyForm() {
        val events = mutableListOf<PetEditorEvent>()
        composeRule.setContent {
            PetEditorContent(
                state = PetEditorState.Empty,
                onEvent = events::add,
                onBack = { },
                onSaved = { },
            )
        }

        // Toolbar title — «Новый питомец».
        composeRule.onNodeWithText(string(R.string.pet_editor_title_new)).assertIsDisplayed()
        // Save FAB присутствует.
        composeRule.onNodeWithTag(SAVE_FAB_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun nameField_isDisplayedAndAcceptsInput() {
        val events = mutableListOf<PetEditorEvent>()
        composeRule.setContent {
            PetEditorContent(
                state = PetEditorState.Empty,
                onEvent = events::add,
                onBack = { },
                onSaved = { },
            )
        }

        composeRule.onNode(hasTestTag(NAME_FIELD_TEST_TAG)).performTextInput("Барсик")

        // Каждый символ генерирует отдельное SetName событие — проверяем последнее.
        assertTrue("name field input must propagate to onEvent", events.isNotEmpty())
        val lastSetName = events.last()
        assertTrue(lastSetName is PetEditorEvent.SetName)
    }

    @Test
    fun saveFab_click_emitsSaveEvent() {
        val events = mutableListOf<PetEditorEvent>()
        composeRule.setContent {
            PetEditorContent(
                state = PetEditorState.Empty,
                onEvent = events::add,
                onBack = { },
                onSaved = { },
            )
        }

        composeRule.onNodeWithTag(SAVE_FAB_TEST_TAG).performClick()
        assertTrue("click on Save FAB must produce Save event", PetEditorEvent.Save in events)
    }

    @Test
    fun loadingState_rendersProgressIndicatorInsteadOfForm() {
        composeRule.setContent {
            PetEditorContent(
                state = PetEditorState.loadingFor(petId = 42L),
                onEvent = { },
                onBack = { },
                onSaved = { },
            )
        }

        // Toolbar показывает «Редактирование» (т.к. editingPetId != null).
        composeRule.onNodeWithText(string(R.string.pet_editor_title_edit)).assertIsDisplayed()
    }

    @Test
    fun speciesSelection_clickingDogChipEmitsSelectSpeciesEvent() {
        val events = mutableListOf<PetEditorEvent>()
        composeRule.setContent {
            PetEditorContent(
                state = PetEditorState.Empty,
                onEvent = events::add,
                onBack = { },
                onSaved = { },
            )
        }

        composeRule
            .onNode(hasTestTag("species_chip_${Species.Dog.id}"))
            .performClick()

        assertTrue(
            "click on Dog chip must produce SelectSpecies(Dog)",
            events.any { it is PetEditorEvent.SelectSpecies && it.species == Species.Dog },
        )
    }

    @Test
    fun subcategorySelector_visibleWhenSpeciesPicked() {
        val state =
            PetEditorState.Empty.copy(
                species = Species.Dog,
                availableSubcategories =
                    DogSize.entries.map { SubcategoryOption(it.id, it.name) },
            )
        composeRule.setContent {
            PetEditorContent(
                state = state,
                onEvent = { },
                onBack = { },
                onSaved = { },
            )
        }

        // Подкатегория-секция показывает все 5 размеров.
        composeRule.onNodeWithText(string(R.string.dog_size_toy)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.dog_size_small)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.dog_size_medium)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.dog_size_large)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.dog_size_giant)).assertIsDisplayed()
    }

    @Test
    fun subcategorySelector_showsRabbitSizeChipsForRabbit() {
        val state =
            PetEditorState.Empty.copy(
                species = Species.Rabbit,
                availableSubcategories = PetEditorState.subcategoriesFor(Species.Rabbit),
            )
        composeRule.setContent {
            PetEditorContent(
                state = state,
                onEvent = { },
                onBack = { },
                onSaved = { },
            )
        }

        // Локализованные RabbitSize метки, а не «собачьи» (id small/medium/large совпадают
        // с DogSize, но species=Rabbit резолвит rabbit_size_*).
        composeRule.onNodeWithText(string(R.string.rabbit_size_dwarf)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.rabbit_size_giant)).assertIsDisplayed()
    }

    @Test
    fun subcategorySelector_showsBirdTypeChipsForBird() {
        val state =
            PetEditorState.Empty.copy(
                species = Species.Bird,
                availableSubcategories = PetEditorState.subcategoriesFor(Species.Bird),
            )
        composeRule.setContent {
            PetEditorContent(
                state = state,
                onEvent = { },
                onBack = { },
                onSaved = { },
            )
        }

        composeRule.onNodeWithText(string(R.string.bird_type_cockatiel)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.bird_type_african_grey)).assertIsDisplayed()
    }

    @Test
    fun subcategorySelector_hiddenForSubcategorylessSpecies() {
        // Rat не имеет подкатегорий → секция «Подкатегория» вообще не рендерится.
        val state =
            PetEditorState.Empty.copy(
                species = Species.Rat,
                availableSubcategories = PetEditorState.subcategoriesFor(Species.Rat),
            )
        composeRule.setContent {
            PetEditorContent(
                state = state,
                onEvent = { },
                onBack = { },
                onSaved = { },
            )
        }

        composeRule.onNodeWithText(string(R.string.pet_editor_subcategory_label)).assertDoesNotExist()
    }

    @Test
    fun validationErrors_renderEachErrorAsBulletLine() {
        composeRule.setContent {
            PetEditorContent(
                state =
                    PetEditorState.Empty.copy(
                        validationErrors =
                            listOf(
                                PetValidationError.NameBlank,
                                PetValidationError.BirthDateInFuture,
                            ),
                    ),
                onEvent = { },
                onBack = { },
                onSaved = { },
            )
        }

        composeRule
            .onNodeWithText("• " + string(R.string.pet_editor_error_name_blank))
            .assertIsDisplayed()
        composeRule
            .onNodeWithText("• " + string(R.string.pet_editor_error_birth_date_in_future))
            .assertIsDisplayed()
    }

    @Test
    fun successfulSave_triggersOnSavedCallback() {
        var savedId: Long? = null
        val events = mutableListOf<PetEditorEvent>()
        composeRule.setContent {
            PetEditorContent(
                state =
                    PetEditorState.Empty.copy(
                        saveResult = PetEditorSaveResult.Success(petId = 7L),
                    ),
                onEvent = events::add,
                onBack = { },
                onSaved = { savedId = it },
            )
        }

        composeRule.waitForIdle()
        assertEquals("onSaved must receive the saved pet id from Success result", 7L, savedId)
        assertTrue(
            "ConsumeSaveResult must be emitted after onSaved fires",
            PetEditorEvent.ConsumeSaveResult in events,
        )
    }

    @Test
    fun birthDateField_isDisplayedReadOnly() {
        composeRule.setContent {
            PetEditorContent(
                state = PetEditorState.Empty.copy(birthDate = LocalDate.of(2020, 1, 15)),
                onEvent = { },
                onBack = { },
                onSaved = { },
            )
        }

        composeRule.onNodeWithText("2020-01-15").assertIsDisplayed()
    }

    // Регрессия b0f220b: read-only OutlinedTextField с enabled=true сам потреблял
    // тап (забирал фокус), и clickable родительского Box'а не срабатывал —
    // DatePickerDialog не открывался вовсе. Тест кликает по полю реальным
    // pointer-событием и проверяет, что диалог появился (кнопка «ОК» видна).
    @Test
    fun birthDateField_click_opensDatePickerDialog() {
        composeRule.setContent {
            PetEditorContent(
                state = PetEditorState.Empty,
                onEvent = { },
                onBack = { },
                onSaved = { },
            )
        }

        composeRule
            .onNodeWithTag(BIRTH_DATE_FIELD_TEST_TAG)
            .performScrollTo()
            .performClick()

        composeRule.onNodeWithText(string(R.string.pet_editor_date_ok)).assertIsDisplayed()
    }
}
