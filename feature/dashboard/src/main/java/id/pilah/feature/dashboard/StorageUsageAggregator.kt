package id.pilah.feature.dashboard

import id.pilah.core.model.Classification
import id.pilah.core.model.FileItem
import id.pilah.core.model.UserCorrection

/** Hitung pemakaian penyimpanan per kategori efektif (klasifikasi terbaru, dikoreksi pengguna jika ada). */
object StorageUsageAggregator {
    fun aggregate(
        files: List<FileItem>,
        classifications: List<Classification>,
        corrections: List<UserCorrection>,
    ): List<CategoryUsage> {
        val classificationByFileId = classifications.associateBy { it.fileId }
        val latestCorrectionByFileId = corrections.groupBy { it.fileId }.mapValues { it.value.first() }

        return files
            .mapNotNull { file ->
                val classification = classificationByFileId[file.id] ?: return@mapNotNull null
                val category = latestCorrectionByFileId[file.id]?.userCategory ?: classification.category
                category to file
            }
            .groupBy({ it.first }, { it.second })
            .map { (category, group) ->
                CategoryUsage(category = category, fileCount = group.size, totalSizeBytes = group.sumOf { it.sizeBytes })
            }
            .sortedByDescending { it.totalSizeBytes }
    }
}
