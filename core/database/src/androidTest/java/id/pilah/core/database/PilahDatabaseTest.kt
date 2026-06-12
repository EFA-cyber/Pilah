package id.pilah.core.database

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import id.pilah.core.database.entity.FileEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class PilahDatabaseTest {

    private lateinit var database: PilahDatabase

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, PilahDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertAndReadFile() = runTest {
        val file = FileEntity(
            path = "/storage/emulated/0/Download/contoh.pdf",
            name = "contoh.pdf",
            type = "application/pdf",
            sizeBytes = 1024L,
            createdAt = Instant.now(),
        )

        val id = database.fileDao().upsert(file)
        val loaded = database.fileDao().getById(id)

        assertEquals(file.name, loaded?.name)
        assertEquals(file.path, loaded?.path)
    }
}
