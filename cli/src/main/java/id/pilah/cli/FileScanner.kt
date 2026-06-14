package id.pilah.cli

import id.pilah.core.model.FileItem
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.time.Instant

/** Menelusuri [root] secara rekursif, melewati berkas/direktori tersembunyi (diawali `.`). */
object FileScanner {

    fun scan(root: File): List<FileItem> {
        val result = mutableListOf<FileItem>()
        collect(root, result)
        return result
    }

    private fun collect(directory: File, result: MutableList<FileItem>) {
        val children = directory.listFiles() ?: return
        for (child in children) {
            when {
                child.isHidden -> continue
                child.isDirectory -> collect(child, result)
                child.isFile -> result += child.toFileItem()
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
}
