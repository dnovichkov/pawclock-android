package app.pawclock.feature.editor.ui.section

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
import app.pawclock.feature.editor.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Поле даты рождения с DatePickerDialog'ом (Material 3).
 *
 * Read-only OutlinedTextField (визуально совпадает с другими полями формы) +
 * прозрачный clickable-оверлей поверх него открывает modal DatePicker. Это лучше,
 * чем Inline-DatePicker на одном экране с длинной формой — экономит вертикальное место.
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
    // Fallback на "1 год назад", если value == null. Без этого Material 3 DatePicker
    // открывается БЕЗ выделенной даты → selectedDateMillis = null → tap "ОК" не
    // вызывает onChange (см. confirmButton ниже: `if (millis != null)`), и E2E-флоу
    // (maestro create_first_pet) не может сохранить питомца без явной навигации
    // по календарю. "1 год назад" — разумный стартовый якорь (типичный возраст
    // молодого питомца) и гарантированно валидная past-дата для SavePetUseCase.
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

    val label = stringResource(R.string.pet_editor_birth_date_label)
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .testTag(BIRTH_DATE_FIELD_TEST_TAG),
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value?.toString().orEmpty(),
            onValueChange = { /* read-only */ },
            readOnly = true,
            // enabled намеренно остаётся true: при false Material 3 рендерит поле
            // приглушённым цветом (выглядит как "недоступно"), TalkBack озвучивает
            // как disabled, и isError-стиль не применяется.
            label = { Text(text = label) },
            isError = isError,
            singleLine = true,
        )
        // Оверлей объявлен ПОСЛЕ поля: enabled read-only текстовое поле само
        // потребляет тапы (остаётся фокусируемым для выделения текста), поэтому
        // clickable на родительском Box не срабатывает — событие до него не доходит.
        // matchParentSize не участвует в измерении Box'а — оверлей растягивается
        // ровно до размеров поля. Оверлей заслоняет поле и в accessibility-дереве,
        // поэтому дублирует label и выбранную дату в contentDescription для TalkBack.
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
        BirthDatePickerDialog(
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
private fun BirthDatePickerDialog(
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
                Text(text = stringResource(R.string.pet_editor_date_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.pet_editor_date_cancel))
            }
        },
    ) {
        DatePicker(state = state)
    }
}

internal const val BIRTH_DATE_FIELD_TEST_TAG: String = "pet_editor_birth_date_field"
