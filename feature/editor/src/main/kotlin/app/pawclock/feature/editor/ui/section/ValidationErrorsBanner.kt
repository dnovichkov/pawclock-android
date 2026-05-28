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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.pawclock.domain.pet.PetValidationError
import app.pawclock.feature.editor.R

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
                    text = "• " + stringResource(errorLabelRes(error)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }
    }
}

@androidx.annotation.StringRes
private fun errorLabelRes(error: PetValidationError): Int =
    when (error) {
        PetValidationError.NameBlank -> R.string.pet_editor_error_name_blank
        PetValidationError.BirthDateInFuture -> R.string.pet_editor_error_birth_date_in_future
        PetValidationError.BirthDateUnrealistic -> R.string.pet_editor_error_birth_date_unrealistic
    }

private const val PADDING_DP: Int = 12
private const val LINE_SPACING_DP: Int = 4
