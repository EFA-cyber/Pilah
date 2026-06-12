package id.pilah.feature.classification

/** Deteksi foto buram tanpa model ML besar, via varians Laplacian (PRD §3.2). */
interface BlurDetector {

    /** Varians Laplacian gambar di [path] (semakin rendah = semakin buram), atau `null` jika gagal dibaca. */
    fun varianceOf(path: String): Double?
}
