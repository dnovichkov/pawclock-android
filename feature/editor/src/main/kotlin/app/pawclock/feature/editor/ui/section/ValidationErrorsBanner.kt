package app.pawclock.feature.editor.ui.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.pawclock.domain.pet.PetValidationError

/**
 * Errored-banner со списком всех ошибок валидации.
 *
 * Локализация: на этом этапе hardcoded русский, Task 22 заменит на stringResource(error.messageKey).
 */
@Composable
internal fun ValidationErrorsBanner(
    errors: List<PetValidationError>,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(PADDING_DP.dp),
            verticalArrangement = Arrangement.spacedBy(LINE_SPACING_DP.dp),
        ) {
            errors.forEach { error ->
                Text(
                    text = "• ${errorLabel(error)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }
    }
}

private fun errorLabel(error: PetValidationError): String =
    when (error) {
        PetValidationError.NameBlank -> "Введите имя питомца"
        PetValidationError.BirthDateInFuture -> "Дата рождения не может быть в будущем"
        PetValidationError.BirthDateUnrealistic -> "Дата рождения слишком давно"
    }

private const val PADDING_DP: Int = 12
private const val LINE_SPACING_DP: Int = 4
