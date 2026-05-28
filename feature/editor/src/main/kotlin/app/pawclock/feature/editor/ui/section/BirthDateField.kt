package app.pawclock.feature.editor.ui.section

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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Поле даты рождения с DatePickerDialog'ом (Material 3).
 *
 * Read-only OutlinedTextField (визуально совпадает с другими полями формы) +
 * pointerInput-перехват кликов открывает modal DatePicker. Это лучше, чем
 * Inline-DatePicker на одном экране с длинной формой — экономит вертикальное место.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BirthDateField(
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

    OutlinedTextField(
        modifier =
            modifier
                .fillMaxWidth()
                .testTag(BIRTH_DATE_FIELD_TEST_TAG)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent()
                            showDialog = true
                        }
                    }
                },
        value = value?.toString().orEmpty(),
        onValueChange = { /* read-only */ },
        readOnly = true,
        label = { Text(text = "Дата рождения *") },
        isError = isError,
        singleLine = true,
    )

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
                    Text(text = "ОК")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(text = "Отмена")
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

internal const val BIRTH_DATE_FIELD_TEST_TAG: String = "pet_editor_birth_date_field"
