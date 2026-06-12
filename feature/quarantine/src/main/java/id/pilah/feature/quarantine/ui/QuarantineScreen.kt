package id.pilah.feature.quarantine.ui

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import id.pilah.feature.quarantine.QuarantineItem
import id.pilah.feature.quarantine.QuarantineViewModel

@Composable
fun QuarantineScreen(
    onKembali: () -> Unit,
    viewModel: QuarantineViewModel = hiltViewModel(),
) {
    val items by viewModel.items.collectAsState()

    QuarantineContent(
        items = items,
        onKembali = onKembali,
        onPulihkan = viewModel::restore,
        onHapusSekarang = viewModel::deleteNow,
    )
}

@Composable
private fun QuarantineContent(
    items: List<QuarantineItem>,
    onKembali: () -> Unit,
    onPulihkan: (QuarantineItem) -> Unit,
    onHapusSekarang: (QuarantineItem) -> Unit,
) {
    var confirmDelete by remember { mutableStateOf<QuarantineItem?>(null) }

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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            Text(
                text = "Karantina",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp),
            )

            if (items.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(items, key = { it.file.id }) { item ->
                        QuarantineCard(
                            item = item,
                            onPulihkan = { onPulihkan(item) },
                            onHapusSekarang = { confirmDelete = item },
                        )
                    }
                }
            }
        }
    }

    confirmDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text("Hapus permanen?") },
            text = { Text("\"${item.file.name}\" akan dihapus permanen dan tidak dapat dikembalikan.") },
            confirmButton = {
                TextButton(onClick = {
                    onHapusSekarang(item)
                    confirmDelete = null
                }) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = null }) {
                    Text("Batal")
                }
            },
        )
    }
}

@Composable
private fun QuarantineCard(
    item: QuarantineItem,
    onPulihkan: () -> Unit,
    onHapusSekarang: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = item.file.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = formatFileSize(item.file.sizeBytes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = formatDaysRemaining(item.daysRemaining),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onPulihkan, modifier = Modifier.weight(1f)) {
                    Text("Pulihkan")
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(onClick = onHapusSekarang, modifier = Modifier.weight(1f)) {
                    Text("Hapus Sekarang")
                }
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
        Text(
            text = "Tidak ada file di Karantina",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
