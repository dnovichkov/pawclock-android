package app.pawclock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pawclock.datastore.AppSettings
import app.pawclock.datastore.SettingsRepository
import app.pawclock.designsystem.theme.PawClockTheme
import app.pawclock.model.ThemeMode
import app.pawclock.navigation.PawClockNavHost
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * Корневая Activity PawClock (Task 17 / Plan 1).
 *
 * `@AndroidEntryPoint` подключает Activity к Hilt-DI-графу, что необходимо для
 * `hiltViewModel<...>()` calls в Composable-функциях ниже по дереву (Task 18+).
 *
 * Тема/dynamicColor читаются из [SettingsRepository] через [SettingsEntryPoint] — это
 * самый дешёвый способ получить Singleton-зависимость из не-Composable / не-ViewModel
 * контекста; полноценный root-ViewModel был бы избыточен для одного reactive-snapshot'а.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SettingsEntryPoint {
        fun settingsRepository(): SettingsRepository
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settingsRepository =
            EntryPointAccessors
                .fromApplication(applicationContext, SettingsEntryPoint::class.java)
                .settingsRepository()
        setContent {
            ThemedApp(settingsRepository)
        }
    }
}

@Composable
private fun ThemedApp(settingsRepository: SettingsRepository) {
    val settings by settingsRepository
        .observe()
        .collectAsStateWithLifecycle(initialValue = AppSettings.Default)
    val darkTheme =
        when (settings.themeMode) {
            ThemeMode.Light -> false
            ThemeMode.Dark -> true
            ThemeMode.System -> isSystemInDarkTheme()
        }
    PawClockTheme(
        darkTheme = darkTheme,
        dynamicColor = settings.dynamicColor,
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            PawClockNavHost()
        }
    }
}
