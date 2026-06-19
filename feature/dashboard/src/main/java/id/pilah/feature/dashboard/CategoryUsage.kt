package id.pilah.feature.dashboard

import id.pilah.core.model.FileCategory

data class CategoryUsage(
    val category: FileCategory,
    val fileCount: Int,
    val totalSizeBytes: Long,
)
