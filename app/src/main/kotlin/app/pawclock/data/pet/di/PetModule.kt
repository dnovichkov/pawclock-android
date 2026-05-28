package app.pawclock.data.pet.di

import app.pawclock.data.pet.RoomPetRepository
import app.pawclock.domain.pet.PetRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt-модуль для замыкания [PetRepository] на production Room-backed реализацию (Task 18).
 *
 * Используется `@Binds` (а не `@Provides`): Hilt просто использует константный binding
 * `PetRepository → RoomPetRepository`, не вызывая factory-функцию для каждой инжекции.
 *
 * До Task 18 граф зависимостей был валиден без этого модуля, так как ни один
 * `@HiltViewModel` не запрашивал UseCase'ы, требующие PetRepository.
 * Task 18 добавляет [app.pawclock.feature.pets.list.PetsListViewModel] и
 * [app.pawclock.feature.pets.detail.PetDetailViewModel], которые косвенно зависят
 * от PetRepository через GetPetsUseCase / GetPetByIdUseCase, поэтому связь нужно
 * предоставить здесь.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PetModule {
    @Binds
    @Singleton
    abstract fun bindPetRepository(impl: RoomPetRepository): PetRepository
}
