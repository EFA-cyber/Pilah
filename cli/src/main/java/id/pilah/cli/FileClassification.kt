package id.pilah.cli

import id.pilah.core.model.FileItem
import java.io.File
import java.time.Instant

/** Menjalankan [Classifier] untuk seluruh [files], dengan deteksi duplikat otomatis lewat hash SHA-256. */
object FileClassification {

    fun classify(files: List<FileItem>): Map<String, ClassificationResult> {
        val context = RuleContext(now = Instant.now(), duplicateOfOriginalName = findDuplicates(files))
        val weights = RuleWeights()
        return files.associate { it.path to Classifier.classify(it, context, weights) }
    }

    /** Kelompokkan file berdasarkan hash SHA-256; selain file tertua per kelompok dianggap duplikat. */
    private fun findDuplicates(files: List<FileItem>): Map<String, String> {
        val duplicates = mutableMapOf<String, String>()
        files
            .mapNotNull { file -> FileHasher.hash(File(file.path))?.let { hash -> hash to file } }
            .groupBy({ it.first }, { it.second })
            .values
            .filter { it.size > 1 }
            .forEach { group ->
                val original = group.minByOrNull { it.createdAt } ?: return@forEach
                group.filter { it.path != original.path }.forEach { duplicate ->
                    duplicates[duplicate.path] = original.name
                }
            }
        return duplicates
    }
}
