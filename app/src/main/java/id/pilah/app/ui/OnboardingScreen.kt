package id.pilah.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import id.pilah.app.R
import id.pilah.core.model.PrivacyMode
import id.pilah.core.permissions.StoragePermissions
import id.pilah.feature.dashboard.SettingsViewModel

/**
 * Layar edukasi & permintaan izin akses penyimpanan penuh (PRD §3.1, §6), dilanjutkan
 * pemilihan mode privasi (PRD §4 langkah 1) sebelum memulai Smart Scan.
 */
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

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.screen_onboarding_title),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.screen_onboarding_body),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = {
                when {
                    hasAccess -> showPrivacyStep = true
                    StoragePermissions.requiresSettingsRedirect() ->
                        settingsLauncher.launch(StoragePermissions.manageAllFilesIntent(context))
                    else -> legacyPermissionLauncher.launch(StoragePermissions.legacyPermissions())
                }
            }) {
                Text(
                    if (hasAccess) {
                        stringResource(R.string.screen_onboarding_cta)
                    } else {
                        stringResource(R.string.screen_onboarding_grant_permission)
                    },
                )
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
            Button(
                onClick = onMulai,
                modifier = Modifier.fillMaxWidth().padding(24.dp),
            ) {
                Text(stringResource(R.string.screen_onboarding_cta))
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
        ) {
            Text(
                text = stringResource(R.string.screen_onboarding_privacy_title),
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.screen_onboarding_privacy_body),
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(24.dp))
            PrivacyModeOption(
                selected = privacyMode == PrivacyMode.ON_DEVICE,
                title = stringResource(R.string.privacy_mode_on_device_title),
                description = stringResource(R.string.privacy_mode_on_device_description),
                onClick = { onPrivacyModeChange(PrivacyMode.ON_DEVICE) },
            )
            Spacer(modifier = Modifier.height(8.dp))
            PrivacyModeOption(
                selected = privacyMode == PrivacyMode.DEEP_ANALYSIS,
                title = stringResource(R.string.privacy_mode_deep_analysis_title),
                description = stringResource(R.string.privacy_mode_deep_analysis_description),
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
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            RadioButton(selected = selected, onClick = null)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
