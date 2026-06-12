package id.pilah.feature.scan

import android.content.Context
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import id.pilah.core.model.FileItem
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow

/**
 * Menelusuri seluruh penyimpanan internal & SD card untuk mengumpulkan metadata
 * file mentah (PRD §3.1: path, name, type, size, created_at). Direktori/berkas
 * tersembunyi (diawali `.`) dilewati.
 */
class FileSystemScanner @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /** Memancarkan satu [FileItem] per berkas yang ditemukan di seluruh [storageRoots]. */
    fun scan(): Flow<FileItem> = flow {
        for (root in storageRoots()) {
            emitFilesIn(root)
        }
    }

    private suspend fun FlowCollector<FileItem>.emitFilesIn(directory: File) {
        val children = directory.listFiles() ?: return
        for (child in children) {
            when {
                child.isHidden -> continue
                child.isDirectory -> emitFilesIn(child)
                child.isFile -> emit(child.toFileItem())
            }
        }
    }

    private fun File.toFileItem(): FileItem {
        val createdAt = runCatching {
            Files.readAttributes(toPath(), BasicFileAttributes::class.java).creationTime().toInstant()
        }.getOrElse { Instant.ofEpochMilli(lastModified()) }

        return FileItem(
            path = absolutePath,
            name = name,
            type = extension.lowercase().ifEmpty { "unknown" },
            sizeBytes = length(),
            createdAt = createdAt,
        )
    }

    /** Direktori akar penyimpanan internal & setiap SD card yang dapat ditelusuri. */
    private fun storageRoots(): List<File> =
        ContextCompat.getExternalFilesDirs(context, null)
            .filterNotNull()
            .mapNotNull { it.toStorageRoot() }
            .filter { it.exists() && it.isDirectory }

    /** Mengubah `.../Android/data/<package>/files` menjadi akar volume penyimpanan. */
    private fun File.toStorageRoot(): File? {
        val index = absolutePath.indexOf(ANDROID_DATA_SEGMENT)
        return if (index >= 0) File(absolutePath.substring(0, index)) else null
    }

    private companion object {
        const val ANDROID_DATA_SEGMENT = "/Android/data"
    }
}
