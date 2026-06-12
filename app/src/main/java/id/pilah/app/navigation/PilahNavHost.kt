package id.pilah.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import id.pilah.app.ui.DashboardScreen
import id.pilah.app.ui.OnboardingScreen
import id.pilah.feature.foldering.ui.FolderingScreen
import id.pilah.feature.quarantine.ui.QuarantineScreen
import id.pilah.feature.review.ui.ReviewScreen
import id.pilah.feature.scan.ui.ScanScreen

/**
 * Graf navigasi alur utama PILAH:
 * Onboarding -> Pindai -> Tinjau Hasil -> Sebelum/Sesudah -> Dashboard -> Karantina (PRD §4).
 * Dashboard masih placeholder, akan diisi pada fase berikutnya.
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
                onLanjut = { navController.navigate(PilahDestination.FOLDERING.route) },
            )
        }
        composable(PilahDestination.FOLDERING.route) {
            FolderingScreen(
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
