package id.pilah.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import id.pilah.app.R

@Composable
fun OnboardingScreen(onMulai: () -> Unit) {
    PlaceholderScreen(
        title = stringResource(R.string.screen_onboarding_title),
        body = stringResource(R.string.screen_onboarding_body),
        primaryActionLabel = stringResource(R.string.screen_onboarding_cta),
        onPrimaryAction = onMulai,
    )
}
