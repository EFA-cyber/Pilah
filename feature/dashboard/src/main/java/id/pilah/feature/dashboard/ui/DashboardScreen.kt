package id.pilah.feature.dashboard.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import id.pilah.feature.dashboard.CategoryUsage
import id.pilah.feature.dashboard.CleanupSession
import id.pilah.feature.dashboard.DashboardViewModel
import id.pilah.feature.dashboard.SavingsSummary

@Composable
fun DashboardScreen(
    onBukaKarantina: () -> Unit,
    onBukaPengaturan: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val storageByCategory by viewModel.storageByCategory.collectAsState()
    val savings by viewModel.savings.collectAsState()
    val cleanupHistory by viewModel.cleanupHistory.collectAsState()

    DashboardContent(
        storageByCategory = storageByCategory,
        savings = savings,
        cleanupHistory = cleanupHistory,
        onBukaKarantina = onBukaKarantina,
        onBukaPengaturan = onBukaPengaturan,
    )
}

@Composable
private fun DashboardContent(
    storageByCategory: List<CategoryUsage>,
    savings: SavingsSummary,
    cleanupHistory: List<CleanupSession>,
    onBukaKarantina: () -> Unit,
    onBukaPengaturan: () -> Unit,
) {
    Scaffold(
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Button(onClick = onBukaKarantina, modifier = Modifier.fillMaxWidth()) {
                    Text("Buka Karantina")
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onBukaPengaturan, modifier = Modifier.fillMaxWidth()) {
                    Text("Pengaturan")
                }
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
                    text = "Dashboard",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            }
            item { SavingsCard(savings) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item {
                Text(text = "Penggunaan per Kategori", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (storageByCategory.isEmpty()) {
                item {
                    Text(
                        text = "Belum ada data. Lakukan pemindaian terlebih dahulu.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                item { StorageByCategoryChart(storageByCategory) }
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(storageByCategory, key = { it.category }) { usage ->
                    CategoryUsageRow(usage)
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item {
                Text(text = "Riwayat Rapikan Sekarang", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (cleanupHistory.isEmpty()) {
                item {
                    Text(
                        text = "Belum ada riwayat \"Rapikan Sekarang\".",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(cleanupHistory, key = { it.date }) { session ->
                    CleanupSessionRow(session)
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun SavingsCard(savings: SavingsSummary) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Total ruang dihemat", style = MaterialTheme.typography.bodyMedium)
            Text(text = formatFileSize(savings.savedBytes), style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Potensi hemat dari Karantina: ${formatFileSize(savings.potentialBytes)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StorageByCategoryChart(storageByCategory: List<CategoryUsage>) {
    val modelProducer = remember { CartesianChartModelProducer.build() }

    LaunchedEffect(storageByCategory) {
        modelProducer.tryRunTransaction {
            columnSeries {
                series(storageByCategory.map { it.totalSizeBytes.toFloat() })
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(),
            startAxis = rememberStartAxis(),
            bottomAxis = rememberBottomAxis(),
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
    )
}

@Composable
private fun CategoryUsageRow(usage: CategoryUsage) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = "${usage.category.label()} (${usage.fileCount} file)", style = MaterialTheme.typography.bodyMedium)
        Text(text = formatFileSize(usage.totalSizeBytes), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun CleanupSessionRow(session: CleanupSession) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = formatDate(session.date), style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "${session.fileCount} file · ${formatFileSize(session.totalSizeBytes)}",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
