package local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bbuddies.madafaker.common_domain.preference.PreferenceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "MF_DATA_STORE")

@Singleton
class PreferenceManagerImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : PreferenceManager {
    /**
     * Returns the current user.
     * If not found locally it will fetch from the server.
     * If not found in the server it will create a new user.
     * */


    companion object {
        private val USER_NAME = stringPreferencesKey("user_name")
        private val AUTH_TOKEN = stringPreferencesKey("auth_token")
    }

    override val authToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[AUTH_TOKEN]
    }

    override suspend fun updateAuthToken(authToken: String) {
        dataStore.edit { preferences ->
            preferences[AUTH_TOKEN] = authToken
        }
    }

}
