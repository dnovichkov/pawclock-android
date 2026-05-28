package app.pawclock.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.pawclock.designsystem.SCREENSHOT_DIR
import app.pawclock.designsystem.SCREENSHOT_TEST_SDK
import app.pawclock.designsystem.theme.PawClockTheme
import app.pawclock.model.LifeStage
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Screenshot-тесты для [LifeStageChip] — все стадии Dog × Cat в светлой/тёмной темах.
 * Opt-in (см. [PawClockCardScreenshotTest] для деталей запуска).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [SCREENSHOT_TEST_SDK], qualifiers = "w360dp-h640dp-xhdpi")
class LifeStageChipScreenshotTest {
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
    fun lifeStageChipsDogLight() {
        captureRoboImage("$SCREENSHOT_DIR/LifeStageChip_dog_light.png") {
            PawClockTheme(darkTheme = false, dynamicColor = false) {
                AllDogStageChips()
            }
        }
    }

    @Test
    fun lifeStageChipsDogDark() {
        captureRoboImage("$SCREENSHOT_DIR/LifeStageChip_dog_dark.png") {
            PawClockTheme(darkTheme = true, dynamicColor = false) {
                AllDogStageChips()
            }
        }
    }

    @Test
    fun lifeStageChipsCatLight() {
        captureRoboImage("$SCREENSHOT_DIR/LifeStageChip_cat_light.png") {
            PawClockTheme(darkTheme = false, dynamicColor = false) {
                AllCatStageChips()
            }
        }
    }

    @Test
    fun lifeStageChipsCatDark() {
        captureRoboImage("$SCREENSHOT_DIR/LifeStageChip_cat_dark.png") {
            PawClockTheme(darkTheme = true, dynamicColor = false) {
                AllCatStageChips()
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun AllDogStageChips() {
    Surface {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LifeStageChip(stage = LifeStage.Dog.Puppy, label = "Щенок")
            LifeStageChip(stage = LifeStage.Dog.YoungAdult, label = "Молодой")
            LifeStageChip(stage = LifeStage.Dog.MatureAdult, label = "Зрелый")
            LifeStageChip(stage = LifeStage.Dog.Senior, label = "Сеньор")
            LifeStageChip(stage = LifeStage.Dog.EndOfLife, label = "Преклонный")
        }
    }
}

@androidx.compose.runtime.Composable
private fun AllCatStageChips() {
    Surface {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LifeStageChip(stage = LifeStage.Cat.Kitten, label = "Котёнок")
            LifeStageChip(stage = LifeStage.Cat.YoungAdult, label = "Молодой")
            LifeStageChip(stage = LifeStage.Cat.MatureAdult, label = "Зрелый")
            LifeStageChip(stage = LifeStage.Cat.Senior, label = "Сеньор")
            LifeStageChip(stage = LifeStage.Cat.EndOfLife, label = "Преклонный")
        }
    }
}
