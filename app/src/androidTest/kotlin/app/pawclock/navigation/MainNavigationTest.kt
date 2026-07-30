package app.pawclock.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.pawclock.MainActivity
import app.pawclock.feature.editor.R as editorR
import app.pawclock.feature.pets.R as petsR
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose UI тест навигации между экранами PawClock (Task 23 / Plan 1, §11.8).
 *
 * Покрывает достижимые из UI navigation paths в текущем состоянии Plan 1.
 * Bottom navigation bar (откроет UI-вход в QuickCalculator / Settings)
 * появится в Plan 2 — соответствующие тесты добавятся тогда же.
 *
 * Сейчас тестируем:
 *  1. PetsList → tap FAB "Добавить" → PetEditor (assertVisible "Новый питомец").
 *  2. PetEditor → tap back IconButton → возврат на PetsList (assertVisible "Питомцы").
 *  3. PetEditor → попытка Save без обязательных полей → остаёмся на PetEditor
 *     (валидационный banner отображается, навигация не происходит).
 *
 * Тест опирается на реальный Hilt-граф + Room БД (см. AppLaunchTest комментарий
 * про изоляцию). Если предыдущий прогон оставил питомцев в БД, проверки empty-state
 * перенесены в [AppLaunchTest.cold_start_renders_pets_list_destination] и здесь
 * не дублируются — этот тест фокусируется только на навигационных переходах.
 */
@RunWith(AndroidJUnit4::class)
class MainNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    // Строки сверяются через ресурсы, а не литералы: эмулятор CI работает в en-US,
    // и default (ru) перекрывается values-en — литералы делали тесты locale-зависимыми.
    // Extended FAB ищутся по contentDescription: M3 1.4 вычищает их text-слот из
    // semantics (clearAndSetSemantics), поэтому onNodeWithText для FAB не работает.
    private fun string(resId: Int): String = composeRule.activity.getString(resId)

    @Test
    fun navigates_from_pets_list_to_pet_editor_via_fab() {
        // Starting destination — PetsList; должен быть его title.
        composeRule.onNodeWithText(string(petsR.string.pets_list_title)).assertIsDisplayed()

        // Тап по Extended FAB "Добавить".
        composeRule.onNodeWithContentDescription(string(petsR.string.pets_list_add)).performClick()

        // На PetEditor отображается title для нового питомца.
        composeRule.onNodeWithText(string(editorR.string.pet_editor_title_new)).assertIsDisplayed()
    }

    @Test
    fun navigates_back_from_pet_editor_to_pets_list() {
        // Перейти на PetEditor.
        composeRule.onNodeWithContentDescription(string(petsR.string.pets_list_add)).performClick()
        composeRule.onNodeWithText(string(editorR.string.pet_editor_title_new)).assertIsDisplayed()

        // Тап по back IconButton — у него contentDescription "Назад"
        // (см. PetEditorScreen.kt → stringResource(R.string.pet_editor_back)).
        composeRule.onNodeWithContentDescription(string(editorR.string.pet_editor_back)).performClick()

        // Возврат на PetsList.
        composeRule.onNodeWithText(string(petsR.string.pets_list_title)).assertIsDisplayed()
    }

    @Test
    fun pet_editor_save_with_empty_form_stays_on_editor() {
        composeRule.onNodeWithContentDescription(string(petsR.string.pets_list_add)).performClick()
        composeRule.onNodeWithText(string(editorR.string.pet_editor_title_new)).assertIsDisplayed()

        // Тап по Save FAB без заполнения формы.
        composeRule.onNodeWithContentDescription(string(editorR.string.pet_editor_save)).performClick()

        // Должны остаться на PetEditor (валидация падает на пустом species/birthDate).
        composeRule.onNodeWithText(string(editorR.string.pet_editor_title_new)).assertIsDisplayed()
    }
}
