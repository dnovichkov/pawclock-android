package app.pawclock

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke-тест запуска [MainActivity] (Task 17 / Plan 1).
 *
 * Проверяет, что:
 *  1. Hilt инициализируется без ошибок (PawClockApplication boot).
 *  2. MainActivity создаётся через `@AndroidEntryPoint` injection без crash.
 *  3. Compose-граф настраивает [PawClockTheme] + [PawClockNavHost] и переходит
 *     в RESUMED state.
 *
 * Использует [ActivityScenario] вместо `createAndroidComposeRule` (compose-ui-test-junit4
 * 1.7.5 metadata пока отсутствует в offline cache — см. Task 16 / `core/designsystem`).
 * Этот более узкий тест достаточен для smoke-проверки: он не валидирует Compose-рендеринг,
 * но падает на любой Hilt/Application/Activity init-failure.
 *
 * Запуск:
 *  - локально на эмуляторе: `./gradlew :app:connectedDebugAndroidTest`
 *  - в CI: nightly.yml workflow (см. Task 4) на reactivecircus/android-emulator-runner
 *    с матрицей API 24/30/35.
 *
 * Полноценный Compose-rendering smoke-тест добавится в Task 18 одновременно с
 * `PetsListScreenTest` через `createAndroidComposeRule<MainActivity>()`.
 */
@RunWith(AndroidJUnit4::class)
class MainActivityLaunchTest {
    @Test
    fun mainActivityLaunchesAndReachesResumed() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.moveToState(Lifecycle.State.RESUMED)
            scenario.onActivity { activity ->
                // Проверяем, что Activity успешно дошла до RESUMED — это автоматически
                // означает, что Hilt-инъекция, Compose setContent и NavHost prepare
                // отработали без exception.
                assertEquals(Lifecycle.State.RESUMED, activity.lifecycle.currentState)
            }
        }
    }
}
