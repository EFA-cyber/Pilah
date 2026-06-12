package id.pilah.feature.review.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import id.pilah.core.model.Classification
import id.pilah.feature.review.ReviewItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Bottom sheet detail file: pratinjau, metadata lengkap, dan riwayat klasifikasi (PRD §4). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FileDetailSheet(
    item: ReviewItem,
    history: List<Classification>,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .navigationBarsPadding(),
        ) {
            FilePreview(path = item.file.path, type = item.file.type)

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = item.file.name, style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(8.dp))

            DetailRow("Lokasi", item.file.path)
            DetailRow("Ukuran", formatFileSize(item.file.sizeBytes))
            DetailRow("Tipe", item.file.type.uppercase())
            DetailRow("Dibuat", formatInstant(item.file.createdAt))
            DetailRow("Terakhir dibuka", item.file.lastOpened?.let(::formatInstant) ?: "Tidak diketahui")
            item.suggestedFolder?.let { DetailRow("Usulan folder", it) }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Riwayat Klasifikasi", style = MaterialTheme.typography.titleSmall)

            Spacer(modifier = Modifier.height(8.dp))

            history.forEach { classification ->
                ClassificationHistoryRow(classification)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodySmall, textAlign = androidx.compose.ui.text.style.TextAlign.End)
    }
}

@Composable
private fun ClassificationHistoryRow(classification: Classification) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = "${classification.category.label()} · ${classification.importanceScore}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(text = formatInstant(classification.classifiedAt), style = MaterialTheme.typography.bodySmall)
        }
        Text(
            text = classification.reason,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FilePreview(path: String, type: String) {
    if (type in IMAGE_EXTENSIONS) {
        var bitmap by remember(path) { mutableStateOf<Bitmap?>(null) }
        LaunchedEffect(path) {
            bitmap = withContext(Dispatchers.IO) { decodeSampledBitmap(path) }
        }
        val current = bitmap
        if (current != null) {
            Image(
                bitmap = current.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
            )
            return
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "Pratinjau tidak tersedia", style = MaterialTheme.typography.bodySmall)
    }
}

private fun decodeSampledBitmap(path: String): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sampleSize = 1
    while (bounds.outWidth / sampleSize > PREVIEW_DIMENSION || bounds.outHeight / sampleSize > PREVIEW_DIMENSION) {
        sampleSize *= 2
    }

    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    return BitmapFactory.decodeFile(path, options)
}

private const val PREVIEW_DIMENSION = 512
private val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "heic", "webp")
