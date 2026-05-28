package app.pawclock

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Холодный старт приложения без crash (Task 23 / Plan 1, §11.8).
 *
 * Расширяет [MainActivityLaunchTest] (Task 17 — ActivityScenario-based smoke).
 * В отличие от того теста, [AppLaunchTest] использует `createAndroidComposeRule<MainActivity>()`
 * и проверяет, что Compose-граф действительно рендерит стартовый экран PetsList
 * (TopAppBar title "Питомцы" из feature/pets/res/values/strings.xml).
 *
 * Тест валидирует:
 *  1. Hilt-инициализация (`PawClockApplication` boot через `@HiltAndroidApp`).
 *  2. `@AndroidEntryPoint` MainActivity injection без crash.
 *  3. `PawClockTheme` + `PawClockNavHost` boot.
 *  4. Реальный рендеринг стартового destination'а (Route.PetsList).
 *
 * Compose-ui-test-junit4 1.11.1 доступен через BOM 2026.05.00 (см. Task 17 bump notes).
 *
 * Используется реальная `PawClockApplication` (не HiltTestApplication), потому что задача
 * smoke-теста — убедиться, что production-граф bootstrap'ится корректно. Замена биндингов
 * через `@UninstallModules` появится в Plan 2 (когда понадобится изолированная Room БД).
 *
 * Запуск:
 *  - локально на эмуляторе: `./gradlew :app:connectedDebugAndroidTest`.
 *  - в CI: nightly.yml workflow (matrix API 24/30/35).
 */
@RunWith(AndroidJUnit4::class)
class AppLaunchTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun cold_start_renders_pets_list_destination() {
        // По умолчанию NavHost открывает Route.PetsList — должен появиться его title.
        composeRule.onNodeWithText("Питомцы").assertIsDisplayed()
    }
}
