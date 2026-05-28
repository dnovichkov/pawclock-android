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
import androidx.navigation.toRoute

/**
 * Корневой граф навигации PawClock (Task 17 / Plan 1).
 *
 * На этом этапе все экраны — placeholder Composables, отображающие имя destination.
 * В Task 18..21 placeholder'ы заменяются на реальные экраны с ViewModel и feature-логикой.
 *
 * Стартовый destination — [Route.PetsList] (список питомцев).
 *
 * @param navController опционально-передаваемый [NavHostController] для тестов
 *   и preview'ев. По умолчанию создаётся через `rememberNavController()`.
 */
@Composable
fun PawClockNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Route.PetsList,
    ) {
        composable<Route.PetsList> {
            PlaceholderScreen("Pets List (Task 18)")
        }
        composable<Route.PetDetail> { entry ->
            val args = entry.toRoute<Route.PetDetail>()
            PlaceholderScreen("Pet Detail #${args.petId} (Task 18)")
        }
        composable<Route.PetEditor> { entry ->
            val args = entry.toRoute<Route.PetEditor>()
            val title =
                if (args.petId == null) {
                    "New Pet (Task 19)"
                } else {
                    "Edit Pet #${args.petId} (Task 19)"
                }
            PlaceholderScreen(title)
        }
        composable<Route.QuickCalculator> {
            PlaceholderScreen("Quick Calculator (Task 20)")
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
 * Заглушка экрана для Task 17 — отображает имя destination в центре экрана.
 * Заменяется на реальные экраны в Task 18..21.
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
