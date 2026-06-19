package id.pilah.core.network

/** Model Claude yang dipakai untuk Analisis Mendalam (Fase 6). */
enum class ClaudeModel(val id: String) {
    /** Klasifikasi batch awal — murah & cepat. */
    HAIKU("claude-haiku-4-5-20251001"),

    /** Eskalasi untuk file yang masih Ambigu setelah batch Haiku. */
    SONNET("claude-sonnet-4-6"),
}
