package id.pilah.feature.classification

/** Ekstraksi cuplikan teks dari sebuah file, dipakai sebagai konteks untuk Analisis Mendalam (Fase 6). */
interface TextExtractor {

    /** Tipe file (ekstensi huruf kecil, mis. "pdf"/"docx"/"txt") yang didukung oleh ekstraktor ini. */
    fun supports(type: String): Boolean

    /** Ambil hingga [maxChars] karakter pertama dari konten file di [path], atau `null` jika gagal/tak didukung. */
    suspend fun extract(path: String, type: String, maxChars: Int): String?
}
