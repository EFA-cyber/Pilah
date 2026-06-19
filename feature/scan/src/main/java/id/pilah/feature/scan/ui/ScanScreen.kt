package id.pilah.feature.scan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import id.pilah.core.designsystem.theme.PilahGreen
import id.pilah.core.designsystem.theme.PilahGreenDark
import id.pilah.core.designsystem.theme.PilahGreenLight
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

    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                Button(
                    onClick = { onConfirm(options[selectedIndex].subDir) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PilahGreen),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        "Mulai Pindai dengan AI",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onBatal,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text("Batal")
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp, bottom = 24.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(PilahGreen)
                    .padding(horizontal = 20.dp, vertical = 20.dp),
            ) {
                Column {
                    Text(
                        text = "Pilih Folder",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "AI Pilah akan menganalisis file\ndi folder yang Anda pilih.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                }
            }

            // Folder option cards
            options.forEachIndexed { index, option ->
                FolderOptionCard(
                    option = option,
                    isSelected = selectedIndex == index,
                    onClick = { selectedIndex = index },
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun FolderOptionCard(
    option: FolderOption,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (isSelected) PilahGreen else MaterialTheme.colorScheme.outlineVariant
    val borderWidth = if (isSelected) 2.dp else 1.dp
    val bgColor = if (isSelected) PilahGreen.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surface
    val iconBg = if (isSelected) PilahGreenLight.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(borderWidth, borderColor, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(iconBg, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(option.emoji, style = MaterialTheme.typography.titleLarge)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) PilahGreenDark else MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = option.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(PilahGreen, RoundedCornerShape(11.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("✓", style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.Bold)
                }
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
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (uiState.isDone) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(PilahGreen.copy(alpha = 0.12f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("✅", style = MaterialTheme.typography.displaySmall)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Pemindaian Selesai",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${uiState.totalFiles} file siap untuk ditinjau",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(36.dp))

                Button(
                    onClick = onLanjut,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PilahGreen),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text("Tinjau Hasil", fontWeight = FontWeight.SemiBold)
                }
            } else if (uiState.isFailed) {
                Text("⚠️", style = MaterialTheme.typography.displaySmall)

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Pemindaian Gagal",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Silakan kembali dan coba lagi.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(36.dp))

                Button(
                    onClick = onBatal,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text("Kembali")
                }
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(56.dp),
                    color = PilahGreen,
                    strokeWidth = 4.dp,
                )

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = when (uiState.phase) {
                        ScanPipelinePhase.SCANNING -> "Memindai file..."
                        ScanPipelinePhase.HASHING -> "Menganalisis duplikat..."
                        ScanPipelinePhase.CLASSIFYING -> "AI sedang mengklasifikasikan..."
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when (uiState.phase) {
                        ScanPipelinePhase.SCANNING -> "${uiState.filesScanned} file ditemukan"
                        ScanPipelinePhase.HASHING -> "${uiState.filesHashed} dari ${uiState.filesToHash} file"
                        ScanPipelinePhase.CLASSIFYING -> "${uiState.filesClassified} dari ${uiState.totalFiles} file"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (uiState.phase != ScanPipelinePhase.SCANNING) {
                    Spacer(modifier = Modifier.height(20.dp))
                    val fraction = when (uiState.phase) {
                        ScanPipelinePhase.HASHING ->
                            if (uiState.filesToHash > 0) uiState.filesHashed.toFloat() / uiState.filesToHash else 0f
                        ScanPipelinePhase.CLASSIFYING ->
                            if (uiState.totalFiles > 0) uiState.filesClassified.toFloat() / uiState.totalFiles else 0f
                        else -> 0f
                    }
                    LinearProgressIndicator(
                        progress = { fraction },
                        modifier = Modifier.fillMaxWidth(),
                        color = PilahGreen,
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                OutlinedButton(
                    onClick = onBatal,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text("Batal")
                }
            }
        }
    }
}
