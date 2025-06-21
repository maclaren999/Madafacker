package local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.preference.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import local.PreferenceManagerImpl.Companion.PreferenceKey
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "MF_DATA_STORE")

@Singleton
class PreferenceManagerImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : PreferenceManager {
    companion object {
        sealed class PreferenceKey<T>(val key: Preferences.Key<T>) {
            object AuthToken : PreferenceKey<String>(stringPreferencesKey("auth_token"))
            object CurrentMode : PreferenceKey<String>(stringPreferencesKey("current_mode"))
            object UnsentDraftBody : PreferenceKey<String>(stringPreferencesKey("unsent_draft_body"))
            object UnsentDraftMode : PreferenceKey<String>(stringPreferencesKey("unsent_draft_mode"))
            object UnsentDraftTimestamp : PreferenceKey<Long>(longPreferencesKey("unsent_draft_timestamp"))
        }
    }

    override val currentMode: StateFlow<Mode> = dataStore.get<String>(PreferenceKey.CurrentMode)
        .map { it?.let { Mode.valueOf(it) } ?: Mode.SHINE }
        .stateIn(
            scope = CoroutineScope(Dispatchers.IO),
            started = SharingStarted.Eagerly,
            initialValue = Mode.SHINE
        )

    override suspend fun updateCurrentMode(mode: Mode) {
        dataStore.set(PreferenceKey.CurrentMode, mode.name)
    }

    override val authToken: StateFlow<String?> = dataStore.get<String>(PreferenceKey.AuthToken)
        .stateIn(
            scope = CoroutineScope(Dispatchers.IO),
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    override suspend fun updateAuthToken(authToken: String) {
        dataStore.set(PreferenceKey.AuthToken, authToken)
    }

    override suspend fun clearUserData() {
        dataStore.edit {
            it.clear()
        }
    }

    override suspend fun updateMode(mode: Mode) {
        dataStore.set(PreferenceKey.CurrentMode, mode.name)
    }
}

private suspend inline fun <T> DataStore<Preferences>.set(
    key: PreferenceKey<T>,
    value: T
) {
    edit { preferences ->
        preferences[key.key] = value
    }
}

private fun <T> DataStore<Preferences>.get(
    key: PreferenceKey<T>
): Flow<T?> = data.map { preferences ->
    preferences[key.key]
}

