package app.pawclock.feature.quickcalc.ui.section

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import app.pawclock.feature.quickcalc.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Поле даты рождения для Quick Calculator (§10 спецификации).
 *
 * Read-only OutlinedTextField + pointerInput-перехват открывает modal DatePicker
 * (тот же UX, что и PetEditor BirthDateField — единообразие важно для пользователя).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun QuickCalcBirthDateField(
    value: LocalDate?,
    onChange: (LocalDate) -> Unit,
    isError: Boolean,
    modifier: Modifier = Modifier,
) {
    var showDialog by remember { mutableStateOf(false) }
    val datePickerState =
        rememberDatePickerState(
            initialSelectedDateMillis =
                value?.atStartOfDay(ZoneId.of("UTC"))?.toInstant()?.toEpochMilli(),
        )

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .testTag(QUICK_CALC_BIRTH_DATE_FIELD_TEST_TAG)
                .clickable { showDialog = true },
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value?.toString().orEmpty(),
            onValueChange = { /* read-only */ },
            readOnly = true,
            // enabled намеренно true: см. комментарий в BirthDateField (editor).
            label = { Text(text = stringResource(R.string.quick_calc_birth_date_label)) },
            isError = isError,
            singleLine = true,
        )
    }

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            val date =
                                Instant
                                    .ofEpochMilli(millis)
                                    .atZone(ZoneId.of("UTC"))
                                    .toLocalDate()
                            onChange(date)
                        }
                        showDialog = false
                    },
                ) {
                    Text(text = stringResource(R.string.quick_calc_date_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(text = stringResource(R.string.quick_calc_date_cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    LaunchedEffect(value) {
        if (value != null) {
            val asMillis = value.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
            if (datePickerState.selectedDateMillis != asMillis) {
                datePickerState.selectedDateMillis = asMillis
            }
        }
    }
}

internal const val QUICK_CALC_BIRTH_DATE_FIELD_TEST_TAG: String = "quick_calc_birth_date_field"
