package app.pawclock.designsystem.theme

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * Unit-тесты для PawClock fallback-палитры (когда Material You недоступен).
 *
 * Эти тесты НЕ запускают `setContent` (для этого нужен Robolectric + Compose runtime).
 * Они верифицируют, что ColorScheme сконструирован с правильными значениями цветов из
 * [PawClockPalette]. Полноценные screenshot-tests на тему — в *ScreenshotTest классах
 * (opt-in под Roborazzi, см. README модуля).
 */
class PawClockThemeTest {
    @Test
    @DisplayName("Light color scheme uses brand teal as primary")
    fun lightSchemeUsesBrandPrimary() {
        assertEquals(PawClockPalette.Primary, PawClockLightColors.primary)
        assertEquals(PawClockPalette.OnPrimary, PawClockLightColors.onPrimary)
        assertEquals(PawClockPalette.PrimaryContainer, PawClockLightColors.primaryContainer)
    }

    @Test
    @DisplayName("Dark color scheme uses light teal as primary (for contrast on dark background)")
    fun darkSchemeUsesDarkBrandPrimary() {
        assertEquals(PawClockPalette.PrimaryDark, PawClockDarkColors.primary)
        assertEquals(PawClockPalette.OnPrimaryDark, PawClockDarkColors.onPrimary)
        assertEquals(PawClockPalette.PrimaryContainerDark, PawClockDarkColors.primaryContainer)
    }

    @Test
    @DisplayName("Light and dark schemes have distinct primary colors")
    fun lightAndDarkAreDistinct() {
        assertNotEquals(
            PawClockLightColors.primary,
            PawClockDarkColors.primary,
            "Light/dark схемы должны иметь разные primary-цвета (иначе тёмная тема не сработает)",
        )
        assertNotEquals(
            PawClockLightColors.background,
            PawClockDarkColors.background,
            "Light/dark схемы должны иметь разный background",
        )
    }

    @Test
    @DisplayName("Light scheme has bright background (luminance > 0.5)")
    fun lightSchemeBackgroundIsBright() {
        val luminance = PawClockLightColors.background.luminance()
        kotlin.test.assertTrue(
            luminance > 0.5f,
            "Light background luminance=$luminance, ожидалось > 0.5 (для светлой темы)",
        )
    }

    @Test
    @DisplayName("Dark scheme has dim background (luminance < 0.2)")
    fun darkSchemeBackgroundIsDim() {
        val luminance = PawClockDarkColors.background.luminance()
        kotlin.test.assertTrue(
            luminance < 0.2f,
            "Dark background luminance=$luminance, ожидалось < 0.2 (для тёмной темы)",
        )
    }

    @Test
    @DisplayName("Error and primary are distinct in both schemes")
    fun errorAndPrimaryAreDistinct() {
        assertNotEquals(
            PawClockLightColors.primary,
            PawClockLightColors.error,
            "primary и error не должны совпадать (visual confusion)",
        )
        assertNotEquals(
            PawClockDarkColors.primary,
            PawClockDarkColors.error,
            "primary и error не должны совпадать в тёмной теме",
        )
    }

    @Test
    @DisplayName("Surface and background match in M3 default (intentional)")
    fun surfaceAndBackgroundMatch() {
        assertEquals(
            PawClockLightColors.surface,
            PawClockLightColors.background,
            "M3 рекомендует surface == background для базового tier",
        )
    }

    /**
     * Простая оценка luminance цвета (без зависимости от Android графики).
     * Формула sRGB → relative luminance из W3C (https://www.w3.org/TR/WCAG21/#dfn-relative-luminance),
     * упрощённая (без gamma-correction): достаточно для дискриминации light/dark схем.
     */
    private fun androidx.compose.ui.graphics.Color.luminance(): Float {
        val r = red
        val g = green
        val b = blue
        return 0.2126f * r + 0.7152f * g + 0.0722f * b
    }
}
