package app.pawclock.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Формы (углы скругления) PawClock.
 *
 * По §5.1 спецификации: карточки 24.dp, кнопки 28.dp. Остальные категории заполнены
 * градиентом от 4.dp (extraSmall) до 24.dp (large) для эстетической согласованности.
 *
 * `Shapes` в M3 содержит 5 категорий: extraSmall (компоненты с минимальным скруглением),
 * small, medium, large, extraLarge. PawClock использует `large = 24.dp` для основной
 * карточной геометрии — все ElevatedCard в feature-модулях наследуют эту форму.
 */
internal val PawClockShapes =
    Shapes(
        // Чипы, мини-иконки.
        extraSmall = RoundedCornerShape(4.dp),
        // Tag, label, малые кнопки.
        small = RoundedCornerShape(8.dp),
        // Карточки рекомендаций (внутри секций), стандартные текстовые поля.
        medium = RoundedCornerShape(16.dp),
        // Основные ElevatedCard / Surface — карточки питомцев (§5.1).
        large = RoundedCornerShape(24.dp),
        // BottomSheet (Quick Calculator) — §5.3.
        extraLarge = RoundedCornerShape(28.dp),
    )
