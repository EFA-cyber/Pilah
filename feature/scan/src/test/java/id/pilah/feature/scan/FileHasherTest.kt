package id.pilah.feature.scan

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FileHasherTest {

    private val hasher = FileHasher()

    @Test
    fun `hash mengembalikan digest SHA-256 heksadesimal dari isi file`() {
        val file = File.createTempFile("file_hasher_test", ".txt")
        try {
            file.writeText("hello pilah")

            val hash = hasher.hash(file)

            assertEquals("e1014062bcede69b8ad8c3471687bb618d6e6693a7e72b7f84b4c790820882a9", hash)
        } finally {
            file.delete()
        }
    }

    @Test
    fun `hash mengembalikan null untuk file yang tidak ada`() {
        val missing = File("/path/yang/tidak/ada/file.txt")

        assertNull(hasher.hash(missing))
    }
}
