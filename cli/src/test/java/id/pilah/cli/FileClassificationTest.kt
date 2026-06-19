package id.pilah.cli

import id.pilah.core.model.FileCategory
import id.pilah.core.model.FileItem
import java.io.File
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FileClassificationTest {

    private lateinit var root: File

    @Before
    fun setUp() {
        root = File.createTempFile("file-classification-test", "").apply {
            delete()
            mkdirs()
        }
    }

    @After
    fun tearDown() {
        root.deleteRecursively()
    }

    @Test
    fun `file dengan konten sama dianggap duplikat dan dapat Layak Dihapus`() {
        val original = File(root, "asli.jpg").apply { writeText("isi yang sama") }
        val duplicate = File(root, "salinan.jpg").apply { writeText("isi yang sama") }

        val files = listOf(
            fileItem(path = original.path, name = original.name),
            fileItem(path = duplicate.path, name = duplicate.name),
        )

        val results = FileClassification.classify(files)

        assertEquals(FileCategory.AMBIGU, results[original.path]?.category)
        assertEquals(FileCategory.LAYAK_DIHAPUS, results[duplicate.path]?.category)
    }

    private fun fileItem(path: String, name: String): FileItem = FileItem(
        path = path,
        name = name,
        type = "jpg",
        sizeBytes = 1_000L,
        createdAt = java.time.Instant.now(),
    )
}
