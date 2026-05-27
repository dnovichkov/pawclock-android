package app.pawclock.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Базовая карточка PawClock — обёртка над [ElevatedCard] с дефолтным паддингом и
 * формой `shapes.large` (24.dp по §5.1 спецификации).
 *
 * Используется во всех feature-модулях: PetsList ([androidx.compose.foundation.lazy.LazyColumn]),
 * PetDetail (hero-блок), Settings (group containers).
 *
 * @param onClick если задан — карточка становится кликабельной (применяет ripple).
 * @param contentPadding внутренний padding содержимого. По умолчанию `16.dp` со всех сторон.
 */
@Composable
fun PawClockCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    colors: CardColors = CardDefaults.elevatedCardColors(),
    content: @Composable () -> Unit,
) {
    if (onClick != null) {
        ElevatedCard(
            onClick = onClick,
            modifier = modifier.semantics { role = Role.Button },
            colors = colors,
        ) {
            Box(modifier = Modifier.padding(contentPadding)) {
                content()
            }
        }
    } else {
        ElevatedCard(
            modifier = modifier,
            colors = colors,
        ) {
            Box(modifier = Modifier.padding(contentPadding)) {
                content()
            }
        }
    }
}
