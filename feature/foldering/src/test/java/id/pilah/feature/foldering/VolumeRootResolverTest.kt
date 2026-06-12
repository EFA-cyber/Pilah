package id.pilah.feature.foldering

import org.junit.Assert.assertEquals
import org.junit.Test

class VolumeRootResolverTest {

    @Test
    fun `path penyimpanan internal diresolusi ke root emulated`() {
        assertEquals("/storage/emulated/0", VolumeRootResolver.resolve("/storage/emulated/0/Download/file.txt"))
    }

    @Test
    fun `path SD card diresolusi ke root volume kartu`() {
        assertEquals("/storage/ABCD-1234", VolumeRootResolver.resolve("/storage/ABCD-1234/DCIM/Camera/foto.jpg"))
    }

    @Test
    fun `path tak dikenal jatuh ke default root emulated 0`() {
        assertEquals("/storage/emulated/0", VolumeRootResolver.resolve("/data/local/tmp/file.txt"))
    }
}
