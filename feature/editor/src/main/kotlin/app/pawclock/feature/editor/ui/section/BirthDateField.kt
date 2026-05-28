package app.pawclock.feature.editor.ui.section

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
import app.pawclock.feature.editor.R
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

    // OutlinedTextField сам не ловит таппинг в read-only режиме, поэтому
    // оборачиваем в Box с clickable. Раньше использовался pointerInput с awaitPointerEvent
    // в while(true) — это ловило ВСЕ pointer-события (Down/Move/Up/Cancel), из-за
    // чего диалог переоткрывался от случайного касания после dismiss'а.
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .testTag(BIRTH_DATE_FIELD_TEST_TAG)
                .clickable { showDialog = true },
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value?.toString().orEmpty(),
            onValueChange = { /* read-only */ },
            readOnly = true,
            // enabled намеренно остаётся true: при false Material 3 рендерит поле
            // приглушённым цветом (выглядит как "недоступно"), TalkBack озвучивает
            // как disabled, и isError-стиль не применяется. Тапы ловит родительский
            // Box.clickable, поэтому read-only-сценарий работает без enabled = false.
            label = { Text(text = stringResource(R.string.pet_editor_birth_date_label)) },
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
                    Text(text = stringResource(R.string.pet_editor_date_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(text = stringResource(R.string.pet_editor_date_cancel))
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
