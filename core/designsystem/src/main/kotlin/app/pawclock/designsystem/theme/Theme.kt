package app.pawclock.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Главная тема приложения PawClock.
 *
 * Применяет Material 3 цветовую схему, типографику и формы. На Android 12+ (API 31+)
 * подхватывает динамические цвета пользователя (Material You) — см. §5.1, §5.5 спецификации.
 * На более ранних версиях использует fallback-палитру из [PawClockLightColors] /
 * [PawClockDarkColors].
 *
 * @param darkTheme принудительный выбор тёмной темы. По умолчанию следует системному.
 * @param dynamicColor включение динамических цветов на Android 12+. По умолчанию `true`.
 * @param content контент, обёрнутый в [MaterialTheme].
 */
@Composable
fun PawClockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> PawClockDarkColors
            else -> PawClockLightColors
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PawClockTypography,
        shapes = PawClockShapes,
        content = content,
    )
}
