package id.pilah.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import id.pilah.app.R
import id.pilah.core.designsystem.theme.PilahGreen
import id.pilah.core.designsystem.theme.PilahGreenDark
import id.pilah.core.designsystem.theme.PilahGreenLight
import id.pilah.core.model.PrivacyMode
import id.pilah.core.permissions.StoragePermissions
import id.pilah.feature.dashboard.SettingsViewModel

@Composable
fun OnboardingScreen(
    onMulai: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    var hasAccess by remember { mutableStateOf(StoragePermissions.hasAccess(context)) }
    var showPrivacyStep by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasAccess = StoragePermissions.hasAccess(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val settingsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        hasAccess = StoragePermissions.hasAccess(context)
    }

    val legacyPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        hasAccess = StoragePermissions.hasAccess(context)
    }

    if (showPrivacyStep) {
        val privacyMode by settingsViewModel.privacyMode.collectAsState()
        PrivacyModeStep(
            privacyMode = privacyMode,
            onPrivacyModeChange = settingsViewModel::setPrivacyMode,
            onMulai = onMulai,
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PilahGreen),
    ) {
        // Hero section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 72.dp, start = 32.dp, end = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(22.dp))
                    .border(1.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_pilah_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "PILAH",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = MaterialTheme.typography.displaySmall.letterSpacing,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Biar AI yang beres-beres.",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.75f),
            )
        }

        // Bottom sheet content
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Izin Akses Penyimpanan",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.screen_onboarding_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        when {
                            hasAccess -> showPrivacyStep = true
                            StoragePermissions.requiresSettingsRedirect() ->
                                settingsLauncher.launch(StoragePermissions.manageAllFilesIntent(context))
                            else -> legacyPermissionLauncher.launch(StoragePermissions.legacyPermissions())
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PilahGreen),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        text = if (hasAccess) {
                            stringResource(R.string.screen_onboarding_cta)
                        } else {
                            stringResource(R.string.screen_onboarding_grant_permission)
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun PrivacyModeStep(
    privacyMode: PrivacyMode,
    onPrivacyModeChange: (PrivacyMode) -> Unit,
    onMulai: () -> Unit,
) {
    Scaffold(
        bottomBar = {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                Button(
                    onClick = onMulai,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PilahGreen),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        text = stringResource(R.string.screen_onboarding_cta),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
        ) {
            // Mini header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, bottom = 24.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(PilahGreen)
                    .padding(20.dp),
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.screen_onboarding_privacy_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.screen_onboarding_privacy_body),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                }
            }

            PrivacyModeOption(
                selected = privacyMode == PrivacyMode.ON_DEVICE,
                title = stringResource(R.string.privacy_mode_on_device_title),
                description = stringResource(R.string.privacy_mode_on_device_description),
                emoji = "🔒",
                onClick = { onPrivacyModeChange(PrivacyMode.ON_DEVICE) },
            )

            Spacer(modifier = Modifier.height(10.dp))

            PrivacyModeOption(
                selected = privacyMode == PrivacyMode.DEEP_ANALYSIS,
                title = stringResource(R.string.privacy_mode_deep_analysis_title),
                description = stringResource(R.string.privacy_mode_deep_analysis_description),
                emoji = "✨",
                onClick = { onPrivacyModeChange(PrivacyMode.DEEP_ANALYSIS) },
            )
        }
    }
}

@Composable
private fun PrivacyModeOption(
    selected: Boolean,
    title: String,
    description: String,
    emoji: String,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) PilahGreen else MaterialTheme.colorScheme.outlineVariant
    val borderWidth = if (selected) 2.dp else 1.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(borderWidth, borderColor, MaterialTheme.shapes.medium)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                PilahGreen.copy(alpha = 0.06f)
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (selected) PilahGreenLight.copy(alpha = 0.35f)
                        else MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(12.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(emoji, style = MaterialTheme.typography.titleLarge)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selected) PilahGreenDark else MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            RadioButton(
                selected = selected,
                onClick = null,
                colors = RadioButtonDefaults.colors(selectedColor = PilahGreen),
            )
        }
    }
}
