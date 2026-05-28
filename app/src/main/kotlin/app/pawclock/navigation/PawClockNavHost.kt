package app.pawclock.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.pawclock.BuildConfig
import app.pawclock.feature.editor.ui.PetEditorScreen
import app.pawclock.feature.pets.detail.ui.PetDetailScreen
import app.pawclock.feature.pets.list.ui.PetsListScreen
import app.pawclock.feature.quickcalc.ui.QuickCalcScreen
import app.pawclock.feature.settings.ui.AboutScreen
import app.pawclock.feature.settings.ui.SettingsScreen

/**
 * Корневой граф навигации PawClock.
 *
 * Реальные экраны PetsList/PetDetail подключены в Task 18, PetEditor — в Task 19,
 * QuickCalculator — в Task 20, Settings/About — в Task 21.
 *
 * Стартовый destination — [Route.PetsList].
 */
@Composable
fun PawClockNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Route.PetsList,
    ) {
        composable<Route.PetsList> {
            PetsListScreen(
                onPetClick = { petId -> navController.navigate(Route.PetDetail(petId)) },
                onAddPetClick = { navController.navigate(Route.PetEditor()) },
            )
        }
        composable<Route.PetDetail> {
            PetDetailScreen(
                onBackClick = { navController.popBackStack() },
                onEditClick = { petId -> navController.navigate(Route.PetEditor(petId)) },
            )
        }
        composable<Route.PetEditor> {
            PetEditorScreen(
                // После сохранения возвращаемся в список (или на detail в edit-mode).
                onSaved = { _ -> navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }
        composable<Route.QuickCalculator> {
            QuickCalcScreen(onBack = { navController.popBackStack() })
        }
        composable<Route.Settings> {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenAbout = { navController.navigate(Route.About) },
            )
        }
        composable<Route.About> {
            AboutScreen(
                appVersion = BuildConfig.VERSION_NAME,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
