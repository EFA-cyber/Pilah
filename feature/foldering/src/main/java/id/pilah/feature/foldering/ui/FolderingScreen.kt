package id.pilah.feature.foldering.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import id.pilah.feature.foldering.FolderingPlan
import id.pilah.feature.foldering.FolderingSummary
import id.pilah.feature.foldering.FolderingUiState
import id.pilah.feature.foldering.FolderingViewModel

@Composable
fun FolderingScreen(
    onLanjut: () -> Unit,
    viewModel: FolderingViewModel = hiltViewModel(),
) {
    val plan by viewModel.plan.collectAsState()
    val executionState by viewModel.executionState.collectAsState()

    FolderingContent(
        plan = plan,
        executionState = executionState,
        onRapikanSekarang = viewModel::rapikanSekarang,
        onLanjut = onLanjut,
    )
}

@Composable
private fun FolderingContent(
    plan: FolderingPlan,
    executionState: FolderingUiState,
    onRapikanSekarang: () -> Unit,
    onLanjut: () -> Unit,
) {
    Scaffold(
        bottomBar = {
            FolderingBottomBar(
                plan = plan,
                executionState = executionState,
                onRapikanSekarang = onRapikanSekarang,
                onLanjut = onLanjut,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
        ) {
            Text(text = "Sebelum -> Sesudah", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))

            when {
                executionState.isDone -> {
                    Text(
                        text = "Selesai! ${executionState.filesMoved} file sudah dirapikan.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                executionState.isRunning -> {
                    Text(text = "Memindahkan file...", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    val fraction = if (executionState.totalFiles > 0) {
                        executionState.filesMoved.toFloat() / executionState.totalFiles
                    } else {
                        0f
                    }
                    LinearProgressIndicator(progress = { fraction }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${executionState.filesMoved} dari ${executionState.totalFiles} file dipindahkan")
                }
                plan.items.isEmpty() -> {
                    EmptyState()
                }
                else -> {
                    Text(
                        text = "${plan.items.size} file akan dipindahkan ke folder berikut:",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(plan.summaries, key = { it.targetFolder }) { summary ->
                            FolderSummaryCard(summary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderingBottomBar(
    plan: FolderingPlan,
    executionState: FolderingUiState,
    onRapikanSekarang: () -> Unit,
    onLanjut: () -> Unit,
) {
    when {
        executionState.isDone || plan.items.isEmpty() -> {
            Button(
                onClick = onLanjut,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            ) {
                Text("Lanjut")
            }
        }
        executionState.isRunning -> Unit
        else -> {
            Button(
                onClick = onRapikanSekarang,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            ) {
                Text("Rapikan Sekarang")
            }
        }
    }
}

@Composable
private fun FolderSummaryCard(summary: FolderingSummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = summary.targetFolder, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${summary.fileCount} file · ${formatFileSize(summary.totalSizeBytes)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Semua file sudah rapi",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tidak ada file yang perlu dipindahkan saat ini.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
