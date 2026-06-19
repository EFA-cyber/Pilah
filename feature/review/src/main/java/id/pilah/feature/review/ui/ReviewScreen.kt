package id.pilah.feature.review.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import id.pilah.core.model.FileCategory
import id.pilah.feature.review.ReviewItem
import id.pilah.feature.review.ReviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    onLanjut: () -> Unit,
    viewModel: ReviewViewModel = hiltViewModel(),
) {
    val penting by viewModel.penting.collectAsState()
    val layakDihapus by viewModel.layakDihapus.collectAsState()

    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var detailItem by remember { mutableStateOf<ReviewItem?>(null) }

    Scaffold(
        bottomBar = {
            Button(
                onClick = onLanjut,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Text("Lanjut")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Penting (${penting.size})") },
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Layak Dihapus (${layakDihapus.size})") },
                )
            }

            val items = if (selectedTab == 0) penting else layakDihapus
            val targetCategory = if (selectedTab == 0) FileCategory.LAYAK_DIHAPUS else FileCategory.PENTING

            if (items.isEmpty()) {
                EmptyState(isPentingTab = selectedTab == 0)
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(items, key = { it.file.id }) { item ->
                        SwipeableReviewCard(
                            item = item,
                            targetCategory = targetCategory,
                            onSwiped = { newCategory -> viewModel.correctCategory(item, newCategory) },
                            onClick = { detailItem = item },
                        )
                    }
                }
            }
        }
    }

    detailItem?.let { item ->
        val history by viewModel.history(item.file.id).collectAsState(initial = emptyList())
        FileDetailSheet(
            item = item,
            history = history,
            onDismiss = { detailItem = null },
        )
    }
}

@Composable
private fun EmptyState(isPentingTab: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (isPentingTab) "Belum ada file Penting" else "Belum ada file Layak Dihapus",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
