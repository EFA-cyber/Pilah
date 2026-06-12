package id.pilah.feature.scan.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import id.pilah.feature.scan.ScanPipelinePhase
import id.pilah.feature.scan.ScanUiState
import id.pilah.feature.scan.ScanViewModel

@Composable
fun ScanScreen(
    onLanjut: () -> Unit,
    viewModel: ScanViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startScan()
    }

    ScanContent(
        uiState = uiState,
        onLanjut = onLanjut,
        onBatal = viewModel::cancelScan,
    )
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
            } else {
                Text(
                    text = when (uiState.phase) {
                        ScanPipelinePhase.SCANNING -> "Memindai file..."
                        ScanPipelinePhase.HASHING -> "Menganalisis duplikat..."
                        ScanPipelinePhase.CLASSIFYING -> "Mengklasifikasikan file..."
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
                        Text("${uiState.filesClassified} dari ${uiState.totalFiles} file diklasifikasikan")
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
