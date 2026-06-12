package id.pilah.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import id.pilah.app.R

@Composable
fun ScanScreen(onLanjut: () -> Unit) {
    PlaceholderScreen(
        title = stringResource(R.string.screen_scan_title),
        body = stringResource(R.string.screen_scan_body),
        primaryActionLabel = stringResource(R.string.action_next),
        onPrimaryAction = onLanjut,
    )
}
