package id.pilah.feature.scan

import java.io.File
import java.security.MessageDigest
import javax.inject.Inject

/** Menghitung hash SHA-256 sebuah berkas secara streaming untuk deteksi duplikat (PRD §3.2). */
class FileHasher @Inject constructor() {

    /** Mengembalikan hash heksadesimal SHA-256, atau `null` jika berkas tidak dapat dibaca. */
    fun hash(file: File): String? = runCatching {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read == -1) break
                digest.update(buffer, 0, read)
            }
        }
        digest.digest().joinToString(separator = "") { "%02x".format(it) }
    }.getOrNull()

    private companion object {
        const val BUFFER_SIZE = 8192
    }
}
