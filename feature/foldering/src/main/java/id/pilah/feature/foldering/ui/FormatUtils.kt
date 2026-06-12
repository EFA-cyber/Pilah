package id.pilah.feature.foldering.ui

import java.util.Locale

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
