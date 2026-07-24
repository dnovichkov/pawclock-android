package app.pawclock.designsystem.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.pawclock.designsystem.SCREENSHOT_DIR
import app.pawclock.designsystem.SCREENSHOT_TEST_SDK
import app.pawclock.designsystem.theme.PawClockTheme
import app.pawclock.model.Species
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Screenshot-тесты для [SpeciesIcon] — по одному снимку на каждый из 12 видов (§5.6, Task 14).
 * Помимо baseline-снимков, тест служит проверкой «composable рендерится без падения»: если
 * [SpeciesIcon] бросит на любом виде, captureRoboImage упадёт.
 *
 * Opt-in (см. [PawClockCardScreenshotTest] для деталей запуска).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [SCREENSHOT_TEST_SDK], qualifiers = "w360dp-h640dp-xhdpi")
class SpeciesIconScreenshotTest {
    @Before
    fun checkScreenshotMode() {
        Assume.assumeTrue(
            "Screenshot-тесты opt-in",
            System.getProperty("roborazzi.test.record") != null ||
                System.getProperty("roborazzi.test.verify") != null ||
                System.getProperty("roborazzi.test.compare") != null ||
                System.getProperty("screenshot") != null,
        )
    }

    @Test
    fun speciesIcons() {
        Species.all().forEach { species ->
            captureRoboImage("$SCREENSHOT_DIR/SpeciesIcon_${species.id}.png") {
                PawClockTheme(darkTheme = false, dynamicColor = false) {
                    Surface {
                        SpeciesIcon(
                            species = species,
                            modifier = Modifier.padding(8.dp).size(48.dp),
                        )
                    }
                }
            }
        }
    }
}
