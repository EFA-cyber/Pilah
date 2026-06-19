package id.pilah.feature.review.ui

import id.pilah.core.model.FileCategory
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter
    .ofPattern("d MMM yyyy, HH:mm", Locale("id", "ID"))
    .withZone(ZoneId.systemDefault())

internal fun formatInstant(instant: Instant): String = DATE_TIME_FORMATTER.format(instant)

internal fun formatFileSize(bytes: Long): String {
    val units = listOf("B", "KB", "MB", "GB")
    var size = bytes.toDouble()
    var unitIndex = 0
    while (size >= 1024 && unitIndex < units.lastIndex) {
        size /= 1024
        unitIndex++
    }
    return if (unitIndex == 0) "$bytes ${units[unitIndex]}" else String.format(Locale("id", "ID"), "%.1f %s", size, units[unitIndex])
}

internal fun FileCategory.label(): String = when (this) {
    FileCategory.PENTING -> "Penting"
    FileCategory.LAYAK_DIHAPUS -> "Layak Dihapus"
    FileCategory.AMBIGU -> "Ambigu"
}
