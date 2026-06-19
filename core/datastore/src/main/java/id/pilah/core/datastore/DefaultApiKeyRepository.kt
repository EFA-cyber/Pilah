package id.pilah.core.datastore

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import id.pilah.core.common.DispatcherProvider
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class DefaultApiKeyRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: DispatcherProvider,
) : ApiKeyRepository {

    private val prefs: SharedPreferences by lazy {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            PREFS_FILE_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    override fun observeHasApiKey(): Flow<Boolean> = callbackFlow {
        trySend(prefs.contains(KEY_API_KEY))

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_API_KEY) trySend(prefs.contains(KEY_API_KEY))
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.flowOn(dispatchers.io)

    override suspend fun getApiKey(): String? = withContext(dispatchers.io) {
        prefs.getString(KEY_API_KEY, null)
    }

    override suspend fun setApiKey(apiKey: String) = withContext(dispatchers.io) {
        prefs.edit().putString(KEY_API_KEY, apiKey).apply()
    }

    override suspend fun clearApiKey() = withContext(dispatchers.io) {
        prefs.edit().remove(KEY_API_KEY).apply()
    }

    private companion object {
        const val PREFS_FILE_NAME = "secure_api_key_prefs"
        const val KEY_API_KEY = "claude_api_key"
    }
}
