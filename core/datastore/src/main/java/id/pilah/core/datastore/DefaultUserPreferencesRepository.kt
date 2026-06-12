package id.pilah.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import id.pilah.core.model.PrivacyMode
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class DefaultUserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : UserPreferencesRepository {

    override fun observePrivacyMode(): Flow<PrivacyMode> = context.userPreferencesDataStore.data.map { prefs ->
        prefs[Keys.PRIVACY_MODE]
            ?.let { runCatching { PrivacyMode.valueOf(it) }.getOrNull() }
            ?: PrivacyMode.ON_DEVICE
    }

    override suspend fun setPrivacyMode(mode: PrivacyMode) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.PRIVACY_MODE] = mode.name
        }
    }

    private object Keys {
        val PRIVACY_MODE = stringPreferencesKey("privacy_mode")
    }
}
