package id.pilah.feature.review

import id.pilah.core.model.FileCategory
import id.pilah.feature.classification.RuleWeights
import id.pilah.feature.classification.RuleWeightsRepository
import javax.inject.Inject

/**
 * Penyesuaian bobot rule engine secara heuristik dari akumulasi koreksi pengguna (PRD §3.2/§4, Fase 3).
 *
 * Setiap koreksi menggeser bobot sinyal yang menjadi alasan utama klasifikasi sebelumnya
 * sebesar [STEP], mendekat ke nol jika koreksi pengguna melemahkan sinyal tersebut,
 * atau menjauh dari nol jika koreksi menguatkannya. Pemetaan alasan->sinyal mengikuti
 * string yang dihasilkan [id.pilah.feature.classification.DefaultRuleEngine].
 */
class RuleWeightsAdjuster @Inject constructor(
    private val ruleWeightsRepository: RuleWeightsRepository,
) {

    suspend fun adjust(reason: String, fromCategory: FileCategory, toCategory: FileCategory) {
        val direction = (rank(toCategory) - rank(fromCategory)).coerceIn(-1, 1)
        if (direction == 0) return

        val step = direction * STEP
        ruleWeightsRepository.updateWeights { weights -> applyStep(weights, reason, step) }
    }

    /** Urutan "kepentingan" kategori: dipakai untuk menentukan arah penyesuaian. */
    private fun rank(category: FileCategory): Int = when (category) {
        FileCategory.LAYAK_DIHAPUS -> 0
        FileCategory.AMBIGU -> 1
        FileCategory.PENTING -> 2
    }

    private fun applyStep(weights: RuleWeights, reason: String, step: Int): RuleWeights = when {
        reason.startsWith(REASON_DUPLICATE) -> weights.copy(duplicateDelta = shift(weights.duplicateDelta, step))
        reason.startsWith(REASON_OLD_SCREENSHOT) -> weights.copy(oldScreenshotDelta = shift(weights.oldScreenshotDelta, step))
        reason.startsWith(REASON_INSTALLED_APK) -> weights.copy(installedApkDelta = shift(weights.installedApkDelta, step))
        reason.startsWith(REASON_UNOPENED_DOWNLOAD) -> weights.copy(unopenedDownloadDelta = shift(weights.unopenedDownloadDelta, step))
        reason.startsWith(REASON_BLURRY_PHOTO) -> weights.copy(blurryPhotoDelta = shift(weights.blurryPhotoDelta, step))
        reason.startsWith(REASON_IMPORTANT_NAME) -> weights.copy(importantNameDelta = shift(weights.importantNameDelta, step))
        reason.startsWith(REASON_MEMORY_PHOTO) -> weights.copy(memoryPhotoDelta = shift(weights.memoryPhotoDelta, step))
        else -> weights.copy(baseScore = (weights.baseScore + step).coerceIn(0, 100))
    }

    /** Geser [delta] sebesar [step], tetap di sisi nol yang sama (negatif tetap <= 0, positif tetap >= 0). */
    private fun shift(delta: Int, step: Int): Int {
        val shifted = delta + step
        return if (delta < 0) shifted.coerceIn(-MAX_MAGNITUDE, 0) else shifted.coerceIn(0, MAX_MAGNITUDE)
    }

    private companion object {
        const val STEP = 2
        const val MAX_MAGNITUDE = 80

        const val REASON_DUPLICATE = "Duplikat dari"
        const val REASON_OLD_SCREENSHOT = "Screenshot lama"
        const val REASON_INSTALLED_APK = "APK sudah terinstal"
        const val REASON_UNOPENED_DOWNLOAD = "File unduhan"
        const val REASON_BLURRY_PHOTO = "Foto terdeteksi buram"
        const val REASON_IMPORTANT_NAME = "Nama file menunjukkan dokumen penting"
        const val REASON_MEMORY_PHOTO = "Foto Kenangan"
    }
}
