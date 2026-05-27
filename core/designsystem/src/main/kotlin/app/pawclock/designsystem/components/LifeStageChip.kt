package app.pawclock.designsystem.components

import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import app.pawclock.model.LifeStage

/**
 * Чип, отображающий стадию жизни питомца.
 *
 * Цветовая семантика:
 *  - Puppy/Kitten — `primary` (молодой, энергия)
 *  - YoungAdult — `tertiary` (стабильная активность)
 *  - MatureAdult — `secondary` (зрелость)
 *  - Senior — `secondaryContainer` (требует внимания, но мягко)
 *  - EndOfLife — `errorContainer` (требует особого ухода)
 *
 * @param stage стадия жизни (`LifeStage.Dog.*` или `LifeStage.Cat.*`).
 * @param label локализованная строка (вызывающая сторона делает [androidx.compose.ui.res.stringResource]
 *   по `stage.displayKey`); сюда передаётся уже готовый текст.
 */
@Composable
fun LifeStageChip(
    stage: LifeStage,
    label: String,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val containerColor: Color =
        when (stage.ordinal) {
            // Puppy / Kitten
            0 -> colorScheme.primaryContainer
            // YoungAdult
            1 -> colorScheme.tertiaryContainer
            // MatureAdult
            2 -> colorScheme.secondaryContainer
            // Senior
            3 -> colorScheme.surfaceVariant
            // EndOfLife
            else -> colorScheme.errorContainer
        }
    val labelColor: Color =
        when (stage.ordinal) {
            0 -> colorScheme.onPrimaryContainer
            1 -> colorScheme.onTertiaryContainer
            2 -> colorScheme.onSecondaryContainer
            3 -> colorScheme.onSurfaceVariant
            else -> colorScheme.onErrorContainer
        }

    AssistChip(
        onClick = { /* read-only chip, без действия */ },
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = labelColor,
            )
        },
        colors =
            AssistChipDefaults.assistChipColors(
                containerColor = containerColor,
                labelColor = labelColor,
            ),
        modifier = modifier,
    )
}
