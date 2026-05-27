@file:Suppress("detekt:MagicNumber", "detekt:MatchingDeclarationName")
// Цветовые ARGB-литералы сгенерированы Material Theme Builder из seed `#00B3A3`. Это
// brand palette tokens, а не "magic numbers" — каждое значение задокументировано в
// связанном Material Theme Builder экспорте (см. ADR-0001).
// MatchingDeclarationName suppressed: файл `Color.kt` содержит `PawClockPalette` +
// `PawClockLightColors` + `PawClockDarkColors` — собирательное название по теме файла.

package app.pawclock.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Базовая палитра PawClock — fallback-цвета для устройств без Material You.
 *
 * Сгенерирована Material Theme Builder (seed `#00B3A3`, teal/cyan) и закреплена ADR-0001
 * (Jetpack Compose over Views). Динамические цвета (Android 12+) перекрывают эту палитру,
 * см. [pawClockLightColorScheme] / [pawClockDarkColorScheme] в [Theme.kt].
 */
internal object PawClockPalette {
    // Brand seed — teal/cyan, ассоциация со здоровьем и спокойствием.
    val Primary = Color(0xFF006A60)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFF74F8E5)
    val OnPrimaryContainer = Color(0xFF00201C)

    val Secondary = Color(0xFF4A635F)
    val OnSecondary = Color(0xFFFFFFFF)
    val SecondaryContainer = Color(0xFFCCE8E2)
    val OnSecondaryContainer = Color(0xFF05201C)

    val Tertiary = Color(0xFF456179)
    val OnTertiary = Color(0xFFFFFFFF)
    val TertiaryContainer = Color(0xFFCCE5FF)
    val OnTertiaryContainer = Color(0xFF001E31)

    val Error = Color(0xFFBA1A1A)
    val OnError = Color(0xFFFFFFFF)
    val ErrorContainer = Color(0xFFFFDAD6)
    val OnErrorContainer = Color(0xFF410002)

    val Background = Color(0xFFFAFDFB)
    val OnBackground = Color(0xFF191C1B)
    val Surface = Color(0xFFFAFDFB)
    val OnSurface = Color(0xFF191C1B)

    val SurfaceVariant = Color(0xFFDAE5E1)
    val OnSurfaceVariant = Color(0xFF3F4946)
    val Outline = Color(0xFF6F7976)
    val OutlineVariant = Color(0xFFBEC9C5)

    // --- Dark variants ---
    val PrimaryDark = Color(0xFF53DBC9)
    val OnPrimaryDark = Color(0xFF003731)
    val PrimaryContainerDark = Color(0xFF005048)
    val OnPrimaryContainerDark = Color(0xFF74F8E5)

    val SecondaryDark = Color(0xFFB1CCC6)
    val OnSecondaryDark = Color(0xFF1C3531)
    val SecondaryContainerDark = Color(0xFF324B47)
    val OnSecondaryContainerDark = Color(0xFFCCE8E2)

    val TertiaryDark = Color(0xFFADCAE6)
    val OnTertiaryDark = Color(0xFF153349)
    val TertiaryContainerDark = Color(0xFF2D4961)
    val OnTertiaryContainerDark = Color(0xFFCCE5FF)

    val ErrorDark = Color(0xFFFFB4AB)
    val OnErrorDark = Color(0xFF690005)
    val ErrorContainerDark = Color(0xFF93000A)
    val OnErrorContainerDark = Color(0xFFFFDAD6)

    val BackgroundDark = Color(0xFF101413)
    val OnBackgroundDark = Color(0xFFE0E3E1)
    val SurfaceDark = Color(0xFF101413)
    val OnSurfaceDark = Color(0xFFE0E3E1)

    val SurfaceVariantDark = Color(0xFF3F4946)
    val OnSurfaceVariantDark = Color(0xFFBEC9C5)
    val OutlineDark = Color(0xFF899390)
    val OutlineVariantDark = Color(0xFF3F4946)
}

/**
 * Fallback светлая палитра PawClock (используется когда динамические цвета недоступны).
 */
internal val PawClockLightColors =
    lightColorScheme(
        primary = PawClockPalette.Primary,
        onPrimary = PawClockPalette.OnPrimary,
        primaryContainer = PawClockPalette.PrimaryContainer,
        onPrimaryContainer = PawClockPalette.OnPrimaryContainer,
        secondary = PawClockPalette.Secondary,
        onSecondary = PawClockPalette.OnSecondary,
        secondaryContainer = PawClockPalette.SecondaryContainer,
        onSecondaryContainer = PawClockPalette.OnSecondaryContainer,
        tertiary = PawClockPalette.Tertiary,
        onTertiary = PawClockPalette.OnTertiary,
        tertiaryContainer = PawClockPalette.TertiaryContainer,
        onTertiaryContainer = PawClockPalette.OnTertiaryContainer,
        error = PawClockPalette.Error,
        onError = PawClockPalette.OnError,
        errorContainer = PawClockPalette.ErrorContainer,
        onErrorContainer = PawClockPalette.OnErrorContainer,
        background = PawClockPalette.Background,
        onBackground = PawClockPalette.OnBackground,
        surface = PawClockPalette.Surface,
        onSurface = PawClockPalette.OnSurface,
        surfaceVariant = PawClockPalette.SurfaceVariant,
        onSurfaceVariant = PawClockPalette.OnSurfaceVariant,
        outline = PawClockPalette.Outline,
        outlineVariant = PawClockPalette.OutlineVariant,
    )

/**
 * Fallback тёмная палитра PawClock.
 */
internal val PawClockDarkColors =
    darkColorScheme(
        primary = PawClockPalette.PrimaryDark,
        onPrimary = PawClockPalette.OnPrimaryDark,
        primaryContainer = PawClockPalette.PrimaryContainerDark,
        onPrimaryContainer = PawClockPalette.OnPrimaryContainerDark,
        secondary = PawClockPalette.SecondaryDark,
        onSecondary = PawClockPalette.OnSecondaryDark,
        secondaryContainer = PawClockPalette.SecondaryContainerDark,
        onSecondaryContainer = PawClockPalette.OnSecondaryContainerDark,
        tertiary = PawClockPalette.TertiaryDark,
        onTertiary = PawClockPalette.OnTertiaryDark,
        tertiaryContainer = PawClockPalette.TertiaryContainerDark,
        onTertiaryContainer = PawClockPalette.OnTertiaryContainerDark,
        error = PawClockPalette.ErrorDark,
        onError = PawClockPalette.OnErrorDark,
        errorContainer = PawClockPalette.ErrorContainerDark,
        onErrorContainer = PawClockPalette.OnErrorContainerDark,
        background = PawClockPalette.BackgroundDark,
        onBackground = PawClockPalette.OnBackgroundDark,
        surface = PawClockPalette.SurfaceDark,
        onSurface = PawClockPalette.OnSurfaceDark,
        surfaceVariant = PawClockPalette.SurfaceVariantDark,
        onSurfaceVariant = PawClockPalette.OnSurfaceVariantDark,
        outline = PawClockPalette.OutlineDark,
        outlineVariant = PawClockPalette.OutlineVariantDark,
    )
