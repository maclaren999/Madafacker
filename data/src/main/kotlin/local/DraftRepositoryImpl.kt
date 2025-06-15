package local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.bbuddies.madafaker.common_domain.model.UnsentDraft
import com.bbuddies.madafaker.common_domain.repository.DraftRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DraftRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : DraftRepository {

    companion object {
        private val DRAFT_BODY_KEY = stringPreferencesKey("draft_body")
        private val DRAFT_MODE_KEY = stringPreferencesKey("draft_mode")
        private val DRAFT_TIMESTAMP_KEY = longPreferencesKey("draft_timestamp")
    }

    override suspend fun saveDraft(draft: UnsentDraft) {
        dataStore.edit { preferences ->
            preferences[DRAFT_BODY_KEY] = draft.body
            preferences[DRAFT_MODE_KEY] = draft.mode
            preferences[DRAFT_TIMESTAMP_KEY] = draft.timestamp
        }
    }

    override fun getCurrentDraft(): Flow<UnsentDraft?> {
        return dataStore.data.map { preferences ->
            val body = preferences[DRAFT_BODY_KEY]
            val mode = preferences[DRAFT_MODE_KEY]
            val timestamp = preferences[DRAFT_TIMESTAMP_KEY]

            if (body != null && mode != null && timestamp != null) {
                UnsentDraft(body, mode, timestamp)
            } else {
                null
            }
        }
    }

    override suspend fun clearDraft() {
        dataStore.edit { preferences ->
            preferences.remove(DRAFT_BODY_KEY)
            preferences.remove(DRAFT_MODE_KEY)
            preferences.remove(DRAFT_TIMESTAMP_KEY)
        }
    }

    override suspend fun getDraftOnce(): UnsentDraft? {
        return getCurrentDraft().first()
    }
}