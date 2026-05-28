package app.pawclock.feature.pets.list

import app.pawclock.model.Pet

/**
 * Возможные состояния экрана списка питомцев (MVI / §11.7 спецификации).
 *
 *  - [Loading] — стартовое состояние, до первой эмиссии из [PetRepository.observeAll].
 *    UI показывает progress-indicator.
 *  - [Empty] — нет ни одного питомца. UI показывает «Добавьте первого питомца» + кнопку
 *    с навигацией на PetEditor (Task 19).
 *  - [Success] — список питомцев (отсортирован репозиторием по name COLLATE NOCASE).
 *  - [Error] — отдалённый случай: ошибка чтения из БД (corruption, IO). UI показывает
 *    snackbar + retry. Текст ошибки локализуется на UI-слое через resource id.
 */
sealed interface PetsListState {
    data object Loading : PetsListState

    data object Empty : PetsListState

    data class Success(
        val pets: List<Pet>,
    ) : PetsListState

    data class Error(
        val messageKey: String,
    ) : PetsListState
}
