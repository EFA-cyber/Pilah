package id.pilah.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import id.pilah.app.ui.DashboardScreen
import id.pilah.app.ui.OnboardingScreen
import id.pilah.app.ui.QuarantineScreen
import id.pilah.app.ui.ReviewScreen
import id.pilah.app.ui.ScanScreen

/**
 * Graf navigasi alur utama PILAH:
 * Onboarding -> Pindai -> Tinjau Hasil -> Dashboard -> Karantina (PRD §4).
 * Setiap layar adalah placeholder yang akan diisi pada fase masing-masing.
 */
@Composable
fun PilahNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = PilahDestination.ONBOARDING.route,
    ) {
        composable(PilahDestination.ONBOARDING.route) {
            OnboardingScreen(
                onMulai = { navController.navigate(PilahDestination.SCAN.route) },
            )
        }
        composable(PilahDestination.SCAN.route) {
            ScanScreen(
                onLanjut = { navController.navigate(PilahDestination.REVIEW.route) },
            )
        }
        composable(PilahDestination.REVIEW.route) {
            ReviewScreen(
                onLanjut = { navController.navigate(PilahDestination.DASHBOARD.route) },
            )
        }
        composable(PilahDestination.DASHBOARD.route) {
            DashboardScreen(
                onBukaKarantina = { navController.navigate(PilahDestination.QUARANTINE.route) },
            )
        }
        composable(PilahDestination.QUARANTINE.route) {
            QuarantineScreen(
                onKembali = { navController.popBackStack() },
            )
        }
    }
}
