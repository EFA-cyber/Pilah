package id.pilah.core.datastore

import id.pilah.core.model.PrivacyMode
import kotlinx.coroutines.flow.Flow

/** Preferensi pengguna (PRD §2/§4): mode privasi (On-Device vs Analisis Mendalam). */
interface UserPreferencesRepository {

    /** Mode privasi saat ini, default [PrivacyMode.ON_DEVICE]. */
    fun observePrivacyMode(): Flow<PrivacyMode>

    suspend fun setPrivacyMode(mode: PrivacyMode)
}
