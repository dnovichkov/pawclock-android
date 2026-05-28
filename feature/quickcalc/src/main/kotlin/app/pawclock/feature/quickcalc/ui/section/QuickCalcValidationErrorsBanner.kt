package app.pawclock.feature.quickcalc.ui.section

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
import app.pawclock.feature.quickcalc.QuickCalcValidationError
import app.pawclock.feature.quickcalc.R

/**
 * Баннер с ошибками валидации Quick Calculator.
 *
 * Накапливает все ошибки списком (не fail-fast), чтобы пользователь заполнил/исправил
 * всё за один проход. Стиль — errorContainer-цвет surface.
 *
 * Локализация: hardcoded русские строки на этом этапе; полный stringResource — Task 22
 * с использованием [QuickCalcValidationError.messageKey].
 */
@Composable
internal fun QuickCalcValidationErrorsBanner(
    errors: List<QuickCalcValidationError>,
    modifier: Modifier = Modifier,
) {
    if (errors.isEmpty()) return
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(BANNER_PADDING_DP.dp),
            verticalArrangement = Arrangement.spacedBy(BANNER_ITEM_GAP_DP.dp),
        ) {
            errors.forEach { error ->
                Text(
                    text = "• " + stringResource(errorMessageRes(error)),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@androidx.annotation.StringRes
private fun errorMessageRes(error: QuickCalcValidationError): Int =
    when (error) {
        QuickCalcValidationError.SpeciesRequired -> R.string.quick_calc_error_species_required
        QuickCalcValidationError.BirthDateRequired -> R.string.quick_calc_error_birth_date_required
        QuickCalcValidationError.BirthDateInFuture -> R.string.quick_calc_error_birth_date_in_future
        QuickCalcValidationError.UnsupportedSpecies -> R.string.quick_calc_error_unsupported_species
        QuickCalcValidationError.UnexpectedError -> R.string.quick_calc_error_unexpected
    }

private const val BANNER_PADDING_DP: Int = 12
private const val BANNER_ITEM_GAP_DP: Int = 4
