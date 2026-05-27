package app.pawclock.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Большая «карточка возраста» — главный hero-блок на экране PetDetail / QuickCalculator
 * (§5.3 спецификации).
 *
 * Структура:
 *  - Сверху: возраст в собачьих/кошачьих годах (calendar age) — `bodyMedium` лейбл +
 *    `headlineMedium` значение.
 *  - Снизу: возраст в человеческих годах (ЧГ) — `bodyMedium` лейбл + `displayMedium`
 *    значение (главная цифра, к которой притягивается взгляд).
 *
 * Тексты передаются уже локализованными — composable не знает о plurals
 * (см. [app.pawclock.format.AgePluralFormatter] в Task 22).
 *
 * @param ageLabel например, «5 лет» или «1 year».
 * @param humanYearsLabel например, «36 ЧГ» или «36 human years».
 * @param ageDescriptor лейбл для [ageLabel] («Возраст», «Calendar age»).
 * @param humanYearsDescriptor лейбл для [humanYearsLabel] («В человеческих годах»).
 */
@Composable
fun AgeBigCard(
    ageLabel: String,
    humanYearsLabel: String,
    ageDescriptor: String,
    humanYearsDescriptor: String,
    modifier: Modifier = Modifier,
) {
    PawClockCard(modifier = modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = ageDescriptor,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Text(
                text = ageLabel,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = humanYearsDescriptor,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = humanYearsLabel,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
        }
    }
}
