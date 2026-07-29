package app.pawclock.feature.quickcalc.ui.section

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import app.pawclock.feature.quickcalc.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Поле даты рождения для Quick Calculator (§10 спецификации).
 *
 * Read-only OutlinedTextField + прозрачный clickable-оверлей открывает modal DatePicker
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
    // Fallback на "1 год назад" см. BirthDateField (editor). Без этого
    // Material 3 picker открывается без selection → tap "ОК" не передаёт дату,
    // и E2E quick_calc_dog.yaml-flow не может выполнить Calculate.
    val defaultInitialMillis =
        remember {
            LocalDate
                .now()
                .minusYears(1)
                .atStartOfDay(ZoneId.of("UTC"))
                .toInstant()
                .toEpochMilli()
        }
    val datePickerState =
        rememberDatePickerState(
            initialSelectedDateMillis =
                value?.atStartOfDay(ZoneId.of("UTC"))?.toInstant()?.toEpochMilli()
                    ?: defaultInitialMillis,
        )

    val label = stringResource(R.string.quick_calc_birth_date_label)
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .testTag(QUICK_CALC_BIRTH_DATE_FIELD_TEST_TAG),
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value?.toString().orEmpty(),
            onValueChange = { /* read-only */ },
            readOnly = true,
            // enabled намеренно true: см. комментарий в BirthDateField (editor).
            label = { Text(text = label) },
            isError = isError,
            singleLine = true,
        )
        // Оверлей объявлен ПОСЛЕ поля: enabled read-only текстовое поле само
        // потребляет тапы, clickable на родительском Box не срабатывает; оверлей
        // заслоняет поле и в accessibility-дереве, поэтому дублирует label и дату
        // в contentDescription — см. подробный комментарий в BirthDateField (editor).
        val overlayDescription = value?.let { "$label: $it" } ?: label
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .semantics { contentDescription = overlayDescription }
                    .clickable(role = Role.Button) { showDialog = true },
        )
    }

    if (showDialog) {
        QuickCalcDatePickerDialog(
            state = datePickerState,
            onConfirm = onChange,
            onDismiss = { showDialog = false },
        )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickCalcDatePickerDialog(
    state: DatePickerState,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = state.selectedDateMillis
                    if (millis != null) {
                        val date =
                            Instant
                                .ofEpochMilli(millis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                        onConfirm(date)
                    }
                    onDismiss()
                },
            ) {
                Text(text = stringResource(R.string.quick_calc_date_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.quick_calc_date_cancel))
            }
        },
    ) {
        DatePicker(state = state)
    }
}

internal const val QUICK_CALC_BIRTH_DATE_FIELD_TEST_TAG: String = "quick_calc_birth_date_field"
