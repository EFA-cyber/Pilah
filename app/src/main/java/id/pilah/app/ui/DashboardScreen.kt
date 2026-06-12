package id.pilah.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import id.pilah.app.R

@Composable
fun DashboardScreen(onBukaKarantina: () -> Unit) {
    PlaceholderScreen(
        title = stringResource(R.string.screen_dashboard_title),
        body = stringResource(R.string.screen_dashboard_body),
        primaryActionLabel = stringResource(R.string.screen_dashboard_quarantine_cta),
        onPrimaryAction = onBukaKarantina,
    )
}
