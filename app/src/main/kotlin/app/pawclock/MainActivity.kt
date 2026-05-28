package app.pawclock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import app.pawclock.designsystem.theme.PawClockTheme
import app.pawclock.navigation.PawClockNavHost
import dagger.hilt.android.AndroidEntryPoint

/**
 * Корневая Activity PawClock (Task 17 / Plan 1).
 *
 * `@AndroidEntryPoint` подключает Activity к Hilt-DI-графу, что необходимо для
 * `hiltViewModel<...>()` calls в Composable-функциях ниже по дереву (Task 18+).
 *
 * Сам Activity максимально тонкий: устанавливает Compose-content с темой и навигацией.
 * Никаких Fragment'ов, кастомного onCreate-логики, ручных биндингов нет.
 *
 * Стэк отображения:
 *  - [PawClockTheme] — Material You / fallback палитра (Task 16)
 *  - [Surface] — даёт background-цвет из темы, важно для edge-to-edge будущих
 *    задач (Plan 2 / Plan 3)
 *  - [PawClockNavHost] — корневой граф навигации с placeholder'ами (Task 17)
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PawClockTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PawClockNavHost()
                }
            }
        }
    }
}
