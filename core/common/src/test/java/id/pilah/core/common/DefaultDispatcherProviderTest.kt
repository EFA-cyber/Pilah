package id.pilah.core.common

import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultDispatcherProviderTest {

    private val provider: DispatcherProvider = DefaultDispatcherProvider()

    @Test
    fun `io dispatcher matches Dispatchers IO`() {
        assertEquals(Dispatchers.IO, provider.io)
    }

    @Test
    fun `default dispatcher matches Dispatchers Default`() {
        assertEquals(Dispatchers.Default, provider.default)
    }
}
