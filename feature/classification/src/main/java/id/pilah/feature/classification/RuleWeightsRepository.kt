package id.pilah.feature.classification

import kotlinx.coroutines.flow.Flow

/** Penyimpanan [RuleWeights], dapat disesuaikan dari akumulasi koreksi pengguna (Fase 3). */
interface RuleWeightsRepository {

    /** Bobot sinyal saat ini, default jika belum pernah disimpan. */
    fun weights(): Flow<RuleWeights>

    /** Memperbarui bobot sinyal secara atomik. */
    suspend fun updateWeights(transform: (RuleWeights) -> RuleWeights)
}
