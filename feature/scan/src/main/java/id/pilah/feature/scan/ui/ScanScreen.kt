package id.pilah.feature.scan.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import id.pilah.feature.scan.FolderOption
import id.pilah.feature.scan.ScanPipelinePhase
import id.pilah.feature.scan.ScanUiState
import id.pilah.feature.scan.ScanViewModel

@Composable
fun ScanScreen(
    onLanjut: () -> Unit,
    onBatal: () -> Unit,
    viewModel: ScanViewModel = hiltViewModel(),
) {
    val isPickingFolder by viewModel.isPickingFolder.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    if (isPickingFolder) {
        FolderPickerContent(
            options = viewModel.folderOptions,
            onConfirm = { subDir -> viewModel.confirmAndStartScan(subDir) },
            onBatal = onBatal,
        )
    } else {
        ScanContent(
            uiState = uiState,
            onLanjut = onLanjut,
            onBatal = {
                viewModel.cancelScan()
                onBatal()
            },
        )
    }
}

@Composable
private fun FolderPickerContent(
    options: List<FolderOption>,
    onConfirm: (String?) -> Unit,
    onBatal: () -> Unit,
) {
    var selectedIndex by remember { mutableIntStateOf(0) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
        ) {
            Text(
                text = "Pilih Folder untuk Dipindai",
                style = MaterialTheme.typography.titleLarge,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "AI Pilah akan menganalisis file di folder yang Anda pilih.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(24.dp))

            options.forEachIndexed { index, option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedIndex = index }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onConfirm(options[selectedIndex].subDir) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Mulai Pindai dengan AI")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onBatal,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Batal")
            }
        }
    }
}

@Composable
private fun ScanContent(
    uiState: ScanUiState,
    onLanjut: () -> Unit,
    onBatal: () -> Unit,
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (uiState.isDone) {
                Text(
                    text = "Pemindaian selesai",
                    style = MaterialTheme.typography.titleLarge,
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text("${uiState.totalFiles} file siap untuk ditinjau")

                Spacer(modifier = Modifier.height(32.dp))

                Button(onClick = onLanjut) {
                    Text("Lanjut")
                }
            } else if (uiState.isFailed) {
                Text(
                    text = "Pemindaian dibatalkan atau gagal",
                    style = MaterialTheme.typography.titleLarge,
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text("Silakan kembali dan coba lagi.")

                Spacer(modifier = Modifier.height(32.dp))

                Button(onClick = onBatal) {
                    Text("Kembali")
                }
            } else {
                Text(
                    text = when (uiState.phase) {
                        ScanPipelinePhase.SCANNING -> "Memindai file..."
                        ScanPipelinePhase.HASHING -> "Menganalisis duplikat..."
                        ScanPipelinePhase.CLASSIFYING -> "AI sedang mengklasifikasikan file..."
                    },
                    style = MaterialTheme.typography.titleLarge,
                )

                Spacer(modifier = Modifier.height(24.dp))

                when (uiState.phase) {
                    ScanPipelinePhase.SCANNING -> {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("${uiState.filesScanned} file ditemukan")
                    }
                    ScanPipelinePhase.HASHING -> {
                        val fraction = if (uiState.filesToHash > 0) {
                            uiState.filesHashed.toFloat() / uiState.filesToHash
                        } else {
                            0f
                        }
                        LinearProgressIndicator(
                            progress = { fraction },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("${uiState.filesHashed} dari ${uiState.filesToHash} file dianalisis")
                    }
                    ScanPipelinePhase.CLASSIFYING -> {
                        val fraction = if (uiState.totalFiles > 0) {
                            uiState.filesClassified.toFloat() / uiState.totalFiles
                        } else {
                            0f
                        }
                        LinearProgressIndicator(
                            progress = { fraction },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("${uiState.filesClassified} dari ${uiState.totalFiles} file diklasifikasikan AI")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedButton(onClick = onBatal) {
                    Text("Batal")
                }
            }
        }
    }
}
