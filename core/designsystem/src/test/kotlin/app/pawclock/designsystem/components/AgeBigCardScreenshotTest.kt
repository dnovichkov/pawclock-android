package app.pawclock.designsystem.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
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
 * Screenshot-тесты для [AgeBigCard] — главный hero-блок на PetDetail/QuickCalculator.
 * Opt-in (см. [PawClockCardScreenshotTest] для деталей запуска).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [SCREENSHOT_TEST_SDK], qualifiers = "w360dp-h640dp-xhdpi")
class AgeBigCardScreenshotTest {
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
    fun ageBigCard5YearsDogLight() {
        captureRoboImage("$SCREENSHOT_DIR/AgeBigCard_5y_dog_light.png") {
            PawClockTheme(darkTheme = false, dynamicColor = false) {
                Surface {
                    AgeBigCard(
                        ageLabel = "5 лет",
                        humanYearsLabel = "57 ЧГ",
                        ageDescriptor = "Календарный возраст",
                        humanYearsDescriptor = "В человеческих годах",
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
    }

    @Test
    fun ageBigCard5YearsDogDark() {
        captureRoboImage("$SCREENSHOT_DIR/AgeBigCard_5y_dog_dark.png") {
            PawClockTheme(darkTheme = true, dynamicColor = false) {
                Surface {
                    AgeBigCard(
                        ageLabel = "5 лет",
                        humanYearsLabel = "57 ЧГ",
                        ageDescriptor = "Календарный возраст",
                        humanYearsDescriptor = "В человеческих годах",
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
    }

    @Test
    fun ageBigCardCat5YearsLight() {
        captureRoboImage("$SCREENSHOT_DIR/AgeBigCard_5y_cat_light.png") {
            PawClockTheme(darkTheme = false, dynamicColor = false) {
                Surface {
                    AgeBigCard(
                        ageLabel = "5 лет",
                        humanYearsLabel = "36 ЧГ",
                        ageDescriptor = "Календарный возраст",
                        humanYearsDescriptor = "В человеческих годах",
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
    }
}
