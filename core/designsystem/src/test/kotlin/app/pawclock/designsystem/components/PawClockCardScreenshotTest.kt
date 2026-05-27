package app.pawclock.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.pawclock.designsystem.SCREENSHOT_DIR
import app.pawclock.designsystem.SCREENSHOT_TEST_SDK
import app.pawclock.designsystem.theme.PawClockTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Screenshot-тесты для [PawClockCard] (light + dark темы).
 *
 * Эти тесты OPT-IN: они пропускаются по умолчанию (Robolectric SDK не всегда доступен
 * в offline-среде разработки). Запускать так:
 *
 * ```
 * ./gradlew :core:designsystem:recordRoboImages \
 *   -Droborazzi.test.record=true -Pscreenshot=true
 * ```
 *
 * Baseline-снимки сохраняются в src/test/screenshots/PawClockCard_*.png и должны быть
 * закоммичены в репозиторий (см. ADR-0001 + §11.9 спецификации).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [SCREENSHOT_TEST_SDK], qualifiers = "w360dp-h640dp-xhdpi")
class PawClockCardScreenshotTest {
    @Before
    fun checkScreenshotMode() {
        Assume.assumeTrue(
            "Screenshot-тесты opt-in: запустите с -Pscreenshot=true или -Droborazzi.test.record=true",
            System.getProperty("roborazzi.test.record") != null ||
                System.getProperty("roborazzi.test.verify") != null ||
                System.getProperty("roborazzi.test.compare") != null ||
                System.getProperty("screenshot") != null,
        )
    }

    @Test
    fun pawClockCardLight() {
        captureRoboImage("$SCREENSHOT_DIR/PawClockCard_light.png") {
            PawClockTheme(darkTheme = false, dynamicColor = false) {
                SamplePawClockCard()
            }
        }
    }

    @Test
    fun pawClockCardDark() {
        captureRoboImage("$SCREENSHOT_DIR/PawClockCard_dark.png") {
            PawClockTheme(darkTheme = true, dynamicColor = false) {
                SamplePawClockCard()
            }
        }
    }
}

@Composable
private fun SamplePawClockCard() {
    PawClockCard(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
    ) {
        Text(
            text = "Барсик",
            style = MaterialTheme.typography.titleLarge,
        )
    }
}
