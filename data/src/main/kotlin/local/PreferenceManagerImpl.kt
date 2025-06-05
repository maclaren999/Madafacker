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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
            object UserId : PreferenceKey<String>(stringPreferencesKey("user_id"))
            object UserName : PreferenceKey<String>(stringPreferencesKey("user_name"))
            object UserCoins : PreferenceKey<Int>(intPreferencesKey("user_coins"))
            object UserUpdatedAt : PreferenceKey<String>(stringPreferencesKey("user_updated_at"))
            object UserCreatedAt : PreferenceKey<String>(stringPreferencesKey("user_created_at"))
            object AuthToken : PreferenceKey<String>(stringPreferencesKey("auth_token"))
            object CurrentMode : PreferenceKey<String>(stringPreferencesKey("current_mode"))
        }
    }

    override val currentUser: StateFlow<User?> = combine(
        dataStore.get(PreferenceKey.UserId),
        dataStore.get(PreferenceKey.UserName),
        dataStore.get(PreferenceKey.UserCoins),
        dataStore.get(PreferenceKey.UserUpdatedAt),
        dataStore.get(PreferenceKey.UserCreatedAt)
    ) { id, name, coins, updatedAt, createdAt ->
        if (id != null && name != null && coins != null && updatedAt != null && createdAt != null) {
            User(id, name, null, coins, updatedAt, createdAt)
        } else null
    }.stateIn(
        scope = CoroutineScope(Dispatchers.IO),
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    override suspend fun updateCurrentUser(user: User) {
        coroutineScope {
            launch { dataStore.set(PreferenceKey.UserId, user.id) }
            launch { dataStore.set(PreferenceKey.UserName, user.name) }
            launch { dataStore.set(PreferenceKey.UserCoins, user.coins) }
            launch { dataStore.set(PreferenceKey.UserUpdatedAt, user.updatedAt) }
            launch { dataStore.set(PreferenceKey.UserCreatedAt, user.createdAt) }
        }
    }
    override val currentMode: Flow<Mode>
        get() = dataStore.get<String>(PreferenceKey.CurrentMode).map { it?.let { Mode.valueOf(it) } ?: Mode.LIGHT }

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

