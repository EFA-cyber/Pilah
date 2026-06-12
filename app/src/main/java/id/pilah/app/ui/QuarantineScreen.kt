package id.pilah.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import id.pilah.app.R

@Composable
fun QuarantineScreen(onKembali: () -> Unit) {
    PlaceholderScreen(
        title = stringResource(R.string.screen_quarantine_title),
        body = stringResource(R.string.screen_quarantine_body),
        primaryActionLabel = stringResource(R.string.action_back),
        onPrimaryAction = onKembali,
    )
}
