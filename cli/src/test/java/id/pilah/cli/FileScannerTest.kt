package id.pilah.cli

import java.io.File
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FileScannerTest {

    private lateinit var root: File

    @Before
    fun setUp() {
        root = File.createTempFile("file-scanner-test", "").apply {
            delete()
            mkdirs()
        }
    }

    @After
    fun tearDown() {
        root.deleteRecursively()
    }

    @Test
    fun `menelusuri file di subfolder dan melewati file tersembunyi`() {
        File(root, "catatan.txt").writeText("isi")
        File(root, "Documents").mkdirs()
        File(root, "Documents/ktp.pdf").writeText("isi")
        File(root, ".hidden").writeText("isi")

        val files = FileScanner.scan(root)

        val names = files.map { it.name }
        assertEquals(2, files.size)
        assertTrue(names.contains("catatan.txt"))
        assertTrue(names.contains("ktp.pdf"))
    }

    @Test
    fun `tipe file diambil dari ekstensi dalam huruf kecil`() {
        File(root, "FOTO.JPG").writeText("isi")

        val file = FileScanner.scan(root).single()

        assertEquals("jpg", file.type)
    }
}
