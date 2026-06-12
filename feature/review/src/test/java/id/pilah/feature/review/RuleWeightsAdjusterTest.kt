package id.pilah.feature.review

import id.pilah.core.model.FileCategory
import id.pilah.feature.classification.RuleWeights
import id.pilah.feature.classification.RuleWeightsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RuleWeightsAdjusterTest {

    private val repository = FakeRuleWeightsRepository()
    private val adjuster = RuleWeightsAdjuster(repository)

    @Test
    fun `koreksi ke kategori lebih penting melemahkan sinyal negatif duplikat`() = runTest {
        adjuster.adjust(
            reason = "Duplikat dari \"original.jpg\"",
            fromCategory = FileCategory.LAYAK_DIHAPUS,
            toCategory = FileCategory.PENTING,
        )

        assertEquals(-48, repository.current.duplicateDelta)
    }

    @Test
    fun `koreksi ke kategori kurang penting menguatkan sinyal negatif duplikat`() = runTest {
        adjuster.adjust(
            reason = "Duplikat dari \"original.jpg\"",
            fromCategory = FileCategory.PENTING,
            toCategory = FileCategory.LAYAK_DIHAPUS,
        )

        assertEquals(-52, repository.current.duplicateDelta)
    }

    @Test
    fun `koreksi ke kategori lebih penting menguatkan sinyal positif nama dokumen penting`() = runTest {
        adjuster.adjust(
            reason = "Nama file menunjukkan dokumen penting",
            fromCategory = FileCategory.AMBIGU,
            toCategory = FileCategory.PENTING,
        )

        assertEquals(37, repository.current.importantNameDelta)
    }

    @Test
    fun `koreksi ke kategori kurang penting melemahkan sinyal positif nama dokumen penting`() = runTest {
        adjuster.adjust(
            reason = "Nama file menunjukkan dokumen penting",
            fromCategory = FileCategory.PENTING,
            toCategory = FileCategory.AMBIGU,
        )

        assertEquals(33, repository.current.importantNameDelta)
    }

    @Test
    fun `koreksi tanpa sinyal khusus menyesuaikan baseScore`() = runTest {
        adjuster.adjust(
            reason = "Belum ada sinyal khusus, perlu ditinjau manual",
            fromCategory = FileCategory.LAYAK_DIHAPUS,
            toCategory = FileCategory.PENTING,
        )

        assertEquals(52, repository.current.baseScore)
    }

    @Test
    fun `koreksi ke kategori yang sama tidak mengubah bobot`() = runTest {
        adjuster.adjust(
            reason = "Foto terdeteksi buram",
            fromCategory = FileCategory.LAYAK_DIHAPUS,
            toCategory = FileCategory.LAYAK_DIHAPUS,
        )

        assertEquals(RuleWeights(), repository.current)
    }

    @Test
    fun `sinyal positif tidak melewati nol saat terus dilemahkan`() = runTest {
        repository.current = RuleWeights(importantNameDelta = 1)

        adjuster.adjust(
            reason = "Nama file menunjukkan dokumen penting",
            fromCategory = FileCategory.PENTING,
            toCategory = FileCategory.AMBIGU,
        )

        assertEquals(0, repository.current.importantNameDelta)
    }

    private class FakeRuleWeightsRepository(initial: RuleWeights = RuleWeights()) : RuleWeightsRepository {
        private val state = MutableStateFlow(initial)

        var current: RuleWeights
            get() = state.value
            set(value) { state.value = value }

        override fun weights(): Flow<RuleWeights> = state

        override suspend fun updateWeights(transform: (RuleWeights) -> RuleWeights) {
            state.value = transform(state.value)
        }
    }
}
