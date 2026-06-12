package id.pilah.feature.dashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import id.pilah.core.model.FileAction
import id.pilah.core.model.PrivacyMode
import id.pilah.feature.dashboard.SettingsViewModel
import java.io.File

@Composable
fun SettingsScreen(
    onKembali: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val privacyMode by viewModel.privacyMode.collectAsState()
    val hasApiKey by viewModel.hasApiKey.collectAsState()
    val actionLog by viewModel.actionLog.collectAsState()

    SettingsContent(
        privacyMode = privacyMode,
        hasApiKey = hasApiKey,
        actionLog = actionLog,
        onPrivacyModeChange = viewModel::setPrivacyMode,
        onSaveApiKey = viewModel::setApiKey,
        onClearApiKey = viewModel::clearApiKey,
        onKembali = onKembali,
    )
}

@Composable
private fun SettingsContent(
    privacyMode: PrivacyMode,
    hasApiKey: Boolean,
    actionLog: List<FileAction>,
    onPrivacyModeChange: (PrivacyMode) -> Unit,
    onSaveApiKey: (String) -> Unit,
    onClearApiKey: () -> Unit,
    onKembali: () -> Unit,
) {
    Scaffold(
        bottomBar = {
            TextButton(
                onClick = onKembali,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            ) {
                Text("Kembali")
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            item {
                Text(
                    text = "Pengaturan",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            }
            item {
                Text(text = "Mode Privasi", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                PrivacyModeOption(
                    selected = privacyMode == PrivacyMode.ON_DEVICE,
                    title = "Hanya di Perangkat",
                    description = "Seluruh pemindaian dan klasifikasi berjalan secara lokal. " +
                        "Tidak ada data file yang dikirim ke luar perangkat.",
                    onClick = { onPrivacyModeChange(PrivacyMode.ON_DEVICE) },
                )
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item {
                PrivacyModeOption(
                    selected = privacyMode == PrivacyMode.DEEP_ANALYSIS,
                    title = "Analisis Mendalam",
                    description = "File berkategori Ambigu dapat dikirim ke layanan Cloud AI " +
                        "agar klasifikasinya lebih akurat. Memerlukan kunci API.",
                    onClick = { onPrivacyModeChange(PrivacyMode.DEEP_ANALYSIS) },
                )
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
            if (privacyMode == PrivacyMode.DEEP_ANALYSIS) {
                item {
                    Text(text = "Kunci API Claude", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Saat Analisis Mendalam aktif, nama file dan cuplikan teks dari " +
                            "file berkategori Ambigu (PDF, DOCX, TXT) dikirim ke Claude API " +
                            "agar dinilai ulang. Isi file lain tidak pernah dikirim ke luar " +
                            "perangkat. Diperlukan kunci API Claude milik Anda sendiri.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item {
                    ApiKeySection(
                        hasApiKey = hasApiKey,
                        onSaveApiKey = onSaveApiKey,
                        onClearApiKey = onClearApiKey,
                    )
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
            item {
                Text(text = "Riwayat Aksi", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (actionLog.isEmpty()) {
                item {
                    Text(
                        text = "Belum ada riwayat aksi.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(actionLog, key = { it.id }) { action ->
                    ActionLogRow(action)
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
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

@Composable
private fun ApiKeySection(
    hasApiKey: Boolean,
    onSaveApiKey: (String) -> Unit,
    onClearApiKey: () -> Unit,
) {
    if (hasApiKey) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Kunci API tersimpan.", style = MaterialTheme.typography.bodyMedium)
            OutlinedButton(onClick = onClearApiKey) {
                Text("Hapus")
            }
        }
    } else {
        var apiKeyInput by remember { mutableStateOf("") }
        Column {
            OutlinedTextField(
                value = apiKeyInput,
                onValueChange = { apiKeyInput = it },
                label = { Text("Kunci API Claude") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    onSaveApiKey(apiKeyInput)
                    apiKeyInput = ""
                },
                enabled = apiKeyInput.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Simpan")
            }
        }
    }
}

@Composable
private fun ActionLogRow(action: FileAction) {
    val fileName = File(action.toPath.ifEmpty { action.fromPath }).name
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = fileName,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = action.actionType.label(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = formatInstant(action.executedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
