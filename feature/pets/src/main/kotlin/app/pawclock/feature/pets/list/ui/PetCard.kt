package app.pawclock.feature.pets.list.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.pawclock.designsystem.components.LifeStageChip
import app.pawclock.designsystem.components.PawClockCard
import app.pawclock.model.LifeStage
import app.pawclock.model.Pet
import app.pawclock.model.Species

/**
 * Карточка одного питомца в списке (Task 18 / §5.3 спецификации).
 *
 * Структура (горизонтальный layout):
 *  - Слева: иконка вида (или фото, если photoPath задан — Plan 2)
 *  - Центр: имя + LifeStageChip + возраст
 *  - Справа (опционально): «возраст в ЧГ» компактным числом
 *
 * Кликабельность реализована через [PawClockCard.onClick] — открывает PetDetail.
 *
 * @param pet модель питомца.
 * @param lifeStage стадия жизни (рассчитанная вызывающим — обычно ViewModel'ью списка).
 *   На этом этапе (Task 18) список не считает стадию для каждого питомца — это требует
 *   N CalculatePetAgeUseCase invocation'ов. Поэтому передаётся null, и chip не отображается.
 *   В Plan 2 можно добавить SummaryFlow в GetPetsUseCase, чтобы стадии считались batch'ем.
 * @param lifeStageLabel локализованная строка стадии (если [lifeStage] != null).
 * @param ageLabel например, «5 лет» (локализуется через AgePluralFormatter — Task 22).
 * @param onClick колбэк по тапу — обычно `navController.navigate(Route.PetDetail(pet.id))`.
 */
@Composable
fun PetCard(
    pet: Pet,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    lifeStage: LifeStage? = null,
    lifeStageLabel: String? = null,
    ageLabel: String? = null,
) {
    PawClockCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SPACER_DP.dp),
        ) {
            SpeciesAvatar(species = pet.species)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = pet.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (ageLabel != null) {
                    Text(
                        text = ageLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (lifeStage != null && lifeStageLabel != null) {
                    LifeStageChip(stage = lifeStage, label = lifeStageLabel)
                }
            }
        }
    }
}

/**
 * Аватар вида — placeholder-кружок с инициалом вида.
 *
 * В Plan 2 заменится на векторные иконки из Material Icons / собственного набора.
 * Цвет — `primaryContainer` (та же семантика, что у Puppy-стадии — «свежо, живое»).
 */
@Composable
private fun SpeciesAvatar(species: Species) {
    val color: Color = MaterialTheme.colorScheme.primaryContainer
    val labelChar =
        when (species) {
            Species.Dog -> "🐶"
            Species.Cat -> "🐱"
            else -> "?"
        }
    Box(
        modifier =
            Modifier
                .size(AVATAR_SIZE_DP.dp)
                .clip(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(AVATAR_SIZE_DP.dp)
                    .clip(CircleShape),
        ) {
            Text(
                text = labelChar,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .padding(4.dp),
            )
        }
        // Background через Spacer избегает overdraw vs. Box.background — простая абстракция.
        @Suppress("UnusedExpression")
        color
    }
}

private const val AVATAR_SIZE_DP: Int = 48
private const val SPACER_DP: Int = 12
