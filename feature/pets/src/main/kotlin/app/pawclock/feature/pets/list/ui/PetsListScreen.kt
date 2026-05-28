package app.pawclock.feature.pets.list.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pawclock.feature.pets.R
import app.pawclock.feature.pets.list.PetsListState
import app.pawclock.feature.pets.list.PetsListViewModel
import app.pawclock.model.Pet

/**
 * Главный экран приложения — список питомцев (§5.3 спецификации).
 *
 * Состояния:
 *  - Loading → CircularProgressIndicator;
 *  - Empty → пустой стейт с приглашением добавить питомца;
 *  - Success → LazyColumn с PetCard'ами;
 *  - Error → сообщение об ошибке.
 *
 * FAB (Extended FAB по M3-гайду) открывает PetEditor для нового питомца.
 *
 * Тап на PetCard открывает PetDetail.
 *
 * Локализация: на этом этапе используются hardcoded русские строки. Полная локализация
 * через `stringResource(R.string.xxx)` — Task 22 (Localization sweep).
 *
 * @param onPetClick колбэк навигации на PetDetail для конкретного питомца.
 * @param onAddPetClick колбэк навигации на PetEditor для нового питомца.
 * @param viewModel опционально-передаваемый ViewModel (по умолчанию — через Hilt).
 *   Параметр существует для preview'ев и androidTest'ов с подменой ViewModel'и.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetsListScreen(
    onPetClick: (Long) -> Unit,
    onAddPetClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PetsListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PetsListContent(
        state = state,
        onPetClick = onPetClick,
        onAddPetClick = onAddPetClick,
        modifier = modifier,
    )
}

/**
 * Stateless вариант экрана — отделён от ViewModel'и для testability и preview'ев.
 * androidTest'ы могут передать любое [PetsListState] напрямую без Hilt-setup'а.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PetsListContent(
    state: PetsListState,
    onPetClick: (Long) -> Unit,
    onAddPetClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = { Text(text = stringResource(R.string.pets_list_title)) },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddPetClick,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(text = stringResource(R.string.pets_list_add)) },
            )
        },
    ) { padding ->
        when (state) {
            is PetsListState.Loading -> LoadingContent(padding)
            is PetsListState.Empty -> EmptyContent(padding)
            is PetsListState.Success -> SuccessContent(state.pets, onPetClick, padding)
            is PetsListState.Error -> ErrorContent(messageKey = state.messageKey, padding)
        }
    }
}

@Composable
private fun LoadingContent(padding: PaddingValues) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(padding),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyContent(padding: PaddingValues) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = HORIZONTAL_PADDING_DP.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.pets_list_empty_title),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.pets_list_empty_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SuccessContent(
    pets: List<Pet>,
    onPetClick: (Long) -> Unit,
    padding: PaddingValues,
) {
    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(padding),
        contentPadding =
            PaddingValues(
                horizontal = HORIZONTAL_PADDING_DP.dp,
                vertical = VERTICAL_PADDING_DP.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(CARD_SPACING_DP.dp),
    ) {
        items(items = pets, key = { it.id }) { pet ->
            PetCard(
                pet = pet,
                onClick = { onPetClick(pet.id) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ErrorContent(
    messageKey: String,
    padding: PaddingValues,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(padding),
        contentAlignment = Alignment.Center,
    ) {
        // messageKey остаётся технической метрикой логирования; для UI используется
        // общий fallback-текст с подстановкой ключа.
        Text(
            text = stringResource(R.string.pets_list_error, messageKey),
            color = MaterialTheme.colorScheme.error,
        )
    }
}

private const val HORIZONTAL_PADDING_DP: Int = 16
private const val VERTICAL_PADDING_DP: Int = 8
private const val CARD_SPACING_DP: Int = 12
