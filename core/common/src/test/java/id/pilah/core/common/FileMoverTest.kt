package id.pilah.core.common

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test

class FileMoverTest {

    private lateinit var root: File

    @Before
    fun setUp() {
        root = File.createTempFile("file-mover-test", "").apply {
            delete()
            mkdirs()
        }
    }

    @After
    fun tearDown() {
        root.deleteRecursively()
    }

    @Test
    fun `pindah file ke folder tujuan yang belum ada`() {
        val from = File(root, "asal.txt").apply { writeText("isi") }
        val to = File(root, "Karantina/asal.txt")

        assertTrue(FileMover.move(from, to))

        assertFalse(from.exists())
        assertEquals("isi", to.readText())
    }

    @Test
    fun `gagal memindahkan file yang tidak ada`() {
        val from = File(root, "tidak-ada.txt")
        val to = File(root, "Karantina/tidak-ada.txt")

        assertFalse(FileMover.move(from, to))
        assertFalse(to.exists())
    }
}
