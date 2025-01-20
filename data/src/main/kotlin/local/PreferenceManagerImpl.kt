package local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bbuddies.madafaker.common_domain.enums.Mode
import com.bbuddies.madafaker.common_domain.model.User
import com.bbuddies.madafaker.common_domain.preference.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "MF_DATA_STORE")

@Singleton
class PreferenceManagerImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : PreferenceManager {
    companion object {
        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_COINS = intPreferencesKey("user_coins")
        private val USER_UPDATED_AT = stringPreferencesKey("user_updated_at")
        private val USER_CREATED_AT = stringPreferencesKey("user_created_at")
        private val AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val CURRENT_MODE = stringPreferencesKey("current_mode")
    }

    override val currentUser: StateFlow<User?> = dataStore.data
        .map { preferences ->
            val id = preferences[USER_ID] ?: return@map null
            val name = preferences[USER_NAME] ?: return@map null
            val coins = preferences[USER_COINS] ?: return@map null
            val updatedAt = preferences[USER_UPDATED_AT] ?: return@map null
            val createdAt = preferences[USER_CREATED_AT] ?: return@map null

            User(id, name, coins, updatedAt, createdAt)
        }
        .stateIn(
            scope = CoroutineScope(Dispatchers.IO),
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    override suspend fun updateCurrentUser(user: User) {
        dataStore.edit { preferences ->
            preferences[USER_ID] = user.id
            preferences[USER_NAME] = user.name
            preferences[USER_COINS] = user.coins
            preferences[USER_UPDATED_AT] = user.updatedAt
            preferences[USER_CREATED_AT] = user.createdAt
        }
    }

    override val currentMode: Flow<Mode>
        get() = dataStore.data.map { preferences ->
            preferences[CURRENT_MODE]?.let { Mode.valueOf(it) } ?: Mode.LIGHT
        }

    override suspend fun updateCurrentMode(mode: Mode) {
        dataStore.edit { preferences ->
            preferences[CURRENT_MODE] = mode.name
        }
    }

    override val authToken: StateFlow<String?> = dataStore.data
        .map { preferences -> preferences[AUTH_TOKEN] }
        .stateIn(
            scope = CoroutineScope(Dispatchers.IO),
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    override suspend fun updateAuthToken(authToken: String) {
        dataStore.edit { preferences ->
            preferences[AUTH_TOKEN] = authToken
        }
    }
}
