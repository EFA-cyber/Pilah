package id.pilah.core.datastore

import kotlinx.coroutines.flow.Flow

/** Penyimpanan kunci API Claude (Fase 6 - Analisis Mendalam), dienkripsi via Android Keystore. */
interface ApiKeyRepository {

    /** Mengamati apakah kunci API sudah diatur, tanpa membocorkan nilainya. */
    fun observeHasApiKey(): Flow<Boolean>

    suspend fun getApiKey(): String?

    suspend fun setApiKey(apiKey: String)

    suspend fun clearApiKey()
}
