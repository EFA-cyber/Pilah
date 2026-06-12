package id.pilah.feature.dashboard.ui

import id.pilah.core.model.ActionType
import id.pilah.core.model.FileCategory
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("id", "ID"))
private val DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter
    .ofPattern("d MMM yyyy, HH:mm", Locale("id", "ID"))
    .withZone(ZoneId.systemDefault())

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

internal fun formatDate(date: LocalDate): String = DATE_FORMATTER.format(date)

internal fun formatInstant(instant: Instant): String = DATE_TIME_FORMATTER.format(instant)

internal fun FileCategory.label(): String = when (this) {
    FileCategory.PENTING -> "Penting"
    FileCategory.LAYAK_DIHAPUS -> "Layak Dihapus"
    FileCategory.AMBIGU -> "Ambigu"
}

internal fun ActionType.label(): String = when (this) {
    ActionType.MOVE -> "Dipindahkan"
    ActionType.QUARANTINE -> "Dikarantina"
    ActionType.RESTORE -> "Dipulihkan"
    ActionType.PURGE -> "Dihapus permanen"
}
