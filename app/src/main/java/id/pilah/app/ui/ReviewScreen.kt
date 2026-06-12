package id.pilah.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import id.pilah.app.R

@Composable
fun ReviewScreen(onLanjut: () -> Unit) {
    PlaceholderScreen(
        title = stringResource(R.string.screen_review_title),
        body = stringResource(R.string.screen_review_body),
        primaryActionLabel = stringResource(R.string.action_next),
        onPrimaryAction = onLanjut,
    )
}
