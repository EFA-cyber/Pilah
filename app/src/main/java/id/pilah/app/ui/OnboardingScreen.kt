package id.pilah.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import id.pilah.app.R
import id.pilah.core.permissions.StoragePermissions

/**
 * Layar edukasi & permintaan izin akses penyimpanan penuh (PRD §3.1, §6).
 * Android 11+ diarahkan ke halaman Pengaturan "Izinkan akses ke semua file";
 * Android 10 ke bawah memakai dialog izin runtime standar.
 */
@Composable
fun OnboardingScreen(onMulai: () -> Unit) {
    val context = LocalContext.current
    var hasAccess by remember { mutableStateOf(StoragePermissions.hasAccess(context)) }

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
                    hasAccess -> onMulai()
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
