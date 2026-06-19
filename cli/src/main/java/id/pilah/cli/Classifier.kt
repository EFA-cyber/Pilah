package id.pilah.cli

import id.pilah.core.model.FileCategory
import id.pilah.core.model.FileItem
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

/** Hasil penilaian [Classifier] untuk satu file. */
data class ClassificationResult(
    val score: Int,
    val category: FileCategory,
    val reason: String,
)

/** Data lintas-file yang dipakai [Classifier] untuk menilai satu file tanpa I/O tambahan. */
data class RuleContext(
    val now: Instant,
    /** Path file duplikat -> nama file asli yang dipertahankan. */
    val duplicateOfOriginalName: Map<String, String> = emptyMap(),
)

/** Bobot sinyal rule engine (PRD §3.2), subset yang relevan untuk pemindaian folder di laptop. */
data class RuleWeights(
    val baseScore: Int = 50,
    val duplicateDelta: Int = -50,
    val oldScreenshotDelta: Int = -25,
    val unopenedDownloadDelta: Int = -20,
    val importantNameDelta: Int = 35,
    val memoryPhotoDelta: Int = 15,
    val oldFileThresholdDays: Long = 30,
)

/** Rule engine berbobot ala PRD §3.2 untuk mengklasifikasikan file di sebuah folder. */
object Classifier {

    fun classify(file: FileItem, context: RuleContext, weights: RuleWeights = RuleWeights()): ClassificationResult {
        val negativeSignals = listOfNotNull(
            duplicateSignal(file, context, weights),
            oldScreenshotSignal(file, context, weights),
            unopenedDownloadSignal(file, context, weights),
        )

        val positiveSignals = listOfNotNull(
            importantNameSignal(file, weights),
            memoryPhotoSignal(file, weights)?.takeIf { negativeSignals.isEmpty() },
        )

        val signals = negativeSignals + positiveSignals
        val score = (weights.baseScore + signals.sumOf { it.delta }).coerceIn(0, 100)
        val category = when {
            score >= PENTING_THRESHOLD -> FileCategory.PENTING
            score <= LAYAK_DIHAPUS_THRESHOLD -> FileCategory.LAYAK_DIHAPUS
            else -> FileCategory.AMBIGU
        }
        val reason = signals.maxByOrNull { abs(it.delta) }?.reason ?: DEFAULT_REASON

        return ClassificationResult(score = score, category = category, reason = reason)
    }

    private fun duplicateSignal(file: FileItem, context: RuleContext, weights: RuleWeights): RuleSignal? {
        val originalName = context.duplicateOfOriginalName[file.path] ?: return null
        return RuleSignal(weights.duplicateDelta, "Duplikat dari \"$originalName\"")
    }

    private fun oldScreenshotSignal(file: FileItem, context: RuleContext, weights: RuleWeights): RuleSignal? {
        if (file.lastOpened != null) return null
        if (!isInFolder(file.path, "screenshot")) return null
        if (!isOlderThan(file.createdAt, context.now, weights.oldFileThresholdDays)) return null

        val date = file.createdAt.atZone(ZoneId.systemDefault()).format(DATE_FORMATTER)
        return RuleSignal(weights.oldScreenshotDelta, "Screenshot lama, tidak dibuka sejak $date")
    }

    private fun unopenedDownloadSignal(file: FileItem, context: RuleContext, weights: RuleWeights): RuleSignal? {
        if (file.lastOpened != null) return null
        if (!isInFolder(file.path, "download")) return null
        if (!isOlderThan(file.createdAt, context.now, weights.oldFileThresholdDays)) return null

        return RuleSignal(weights.unopenedDownloadDelta, "File unduhan, belum pernah dibuka")
    }

    private fun importantNameSignal(file: FileItem, weights: RuleWeights): RuleSignal? {
        if (!IMPORTANT_NAME_REGEX.containsMatchIn(file.name)) return null
        return RuleSignal(weights.importantNameDelta, "Nama file menunjukkan dokumen penting")
    }

    private fun memoryPhotoSignal(file: FileItem, weights: RuleWeights): RuleSignal? {
        if (!isInFolder(file.path, "dcim") || file.type !in MEMORY_EXTENSIONS) return null
        return RuleSignal(weights.memoryPhotoDelta, "Foto Kenangan")
    }

    private fun isInFolder(path: String, folder: String): Boolean = path.lowercase().contains("/$folder")

    private fun isOlderThan(createdAt: Instant, now: Instant, days: Long): Boolean =
        Duration.between(createdAt, now).toDays() >= days

    private data class RuleSignal(val delta: Int, val reason: String)

    private const val PENTING_THRESHOLD = 70
    private const val LAYAK_DIHAPUS_THRESHOLD = 30
    private const val DEFAULT_REASON = "Belum ada sinyal khusus, perlu ditinjau manual"

    private val IMPORTANT_NAME_REGEX = Regex(
        "ijazah|ktp|npwp|invoice|faktur|kontrak|skripsi|tesis|disertasi|sertifikat|akta|" +
            "paspor|kartu[ _-]?keluarga|\\bkk\\b|\\bsim\\b|bpkb|stnk|slip[ _-]?gaji|polis|asuransi",
        RegexOption.IGNORE_CASE,
    )

    private val MEMORY_EXTENSIONS = setOf("jpg", "jpeg", "png", "heic", "webp", "mp4", "mov", "3gp", "mkv")

    private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("id", "ID"))
}
