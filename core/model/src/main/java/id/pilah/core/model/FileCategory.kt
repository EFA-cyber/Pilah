package id.pilah.core.model

/**
 * Kategori hasil klasifikasi sebuah file.
 *
 * Ambang batas skor (lihat rule engine di `feature/classification`):
 * - skor >= 70 -> [PENTING]
 * - skor <= 30 -> [LAYAK_DIHAPUS]
 * - selainnya  -> [AMBIGU] (kandidat Analisis Mendalam / review manual)
 */
enum class FileCategory {
    PENTING,
    LAYAK_DIHAPUS,
    AMBIGU,
}
