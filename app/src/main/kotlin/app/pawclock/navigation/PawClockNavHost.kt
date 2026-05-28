package app.pawclock.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.pawclock.feature.editor.ui.PetEditorScreen
import app.pawclock.feature.pets.detail.ui.PetDetailScreen
import app.pawclock.feature.pets.list.ui.PetsListScreen
import app.pawclock.feature.quickcalc.ui.QuickCalcScreen

/**
 * Корневой граф навигации PawClock.
 *
 * Реальные экраны PetsList/PetDetail подключены в Task 18, PetEditor — в Task 19,
 * QuickCalculator — в Task 20. Settings/About — placeholder'ы до Task 21.
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
            PlaceholderScreen("Settings (Task 21)")
        }
        composable<Route.About> {
            PlaceholderScreen("About (Task 21)")
        }
    }
}

/**
 * Заглушка экрана для destinations, ещё не реализованных в Plan 1.
 * Заменяется на реальные экраны в Tasks 19-21.
 */
@Composable
private fun PlaceholderScreen(label: String) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(SCREEN_PADDING_DP.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label)
    }
}

private const val SCREEN_PADDING_DP = 16
