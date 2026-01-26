package local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.model.MessageState
import com.bbuddies.madafaker.common_domain.model.Reply
import com.bbuddies.madafaker.common_domain.model.User
import kotlinx.coroutines.flow.Flow


@Dao
interface MadafakerDao {
    // Message operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<Message>)

    @Query("SELECT * FROM messages ORDER BY createdAt DESC")
    suspend fun getAllMessages(): List<Message>

    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: String): Message?

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Update
    suspend fun updateMessage(message: Message)

    @Query("SELECT * FROM messages WHERE authorId != :currentUserId ORDER BY createdAt DESC")
    fun observeIncomingMessages(currentUserId: String): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE authorId = :currentUserId ORDER BY createdAt DESC")
    fun observeOutgoingMessages(currentUserId: String): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE localState = :state")
    suspend fun getMessagesByState(state: MessageState): List<Message>

    @Query("DELETE FROM messages WHERE localState = :state")
    suspend fun deleteMessagesByState(state: MessageState)

    @Query("DELETE FROM messages WHERE authorId != :currentUserId AND localState = 'SENT'")
    suspend fun deleteIncomingMessages(currentUserId: String)

    @Deprecated("Postponed sending removed; pending/failed counts kept for legacy data.")
    @Query("SELECT COUNT(*) FROM messages WHERE localState IN ('PENDING', 'FAILED')")
    fun observePendingCount(): Flow<Int>

    // Read state management
    @Query("SELECT * FROM messages WHERE authorId != :currentUserId AND isRead = false ORDER BY createdAt DESC LIMIT 1")
    suspend fun getMostRecentUnreadMessage(currentUserId: String): Message?

    @Query("UPDATE messages SET isRead = true, readAt = :readAt WHERE id = :messageId")
    suspend fun markMessageAsRead(messageId: String, readAt: Long)

    @Query("UPDATE messages SET isRead = true, readAt = :readAt WHERE authorId != :currentUserId AND isRead = false")
    suspend fun markAllIncomingMessagesAsRead(currentUserId: String, readAt: Long)

    // Reply operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReply(reply: Reply)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReplies(replies: List<Reply>)

    @Query("SELECT * FROM replies WHERE parentMessageId = :parentMessageId ORDER BY createdAt ASC")
    suspend fun getRepliesByParentId(parentMessageId: String): List<Reply>

    @Query("SELECT * FROM replies WHERE parentMessageId = :parentMessageId AND authorId = :authorId ORDER BY createdAt ASC")
    suspend fun getRepliesByParentIdAndAuthor(parentMessageId: String, authorId: String): List<Reply>

    @Query("SELECT * FROM replies WHERE id = :replyId")
    suspend fun getReplyById(replyId: String): Reply?

    @Update
    suspend fun updateReply(reply: Reply)

    // User operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)

    // Single-user app: allow offline fallback without relying on a token-mapped ID.
    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getAnyUser(): User?

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): User?

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)

    @Update
    suspend fun updateUser(user: User)

    //Clear operations
    @Query("DELETE FROM messages")
    suspend fun clearMessages()

    @Query("DELETE FROM replies")
    suspend fun clearReplies()

    @Query("DELETE FROM users")
    suspend fun clearUsers()

    /**
     * Replace all messages with localState SENT in a single transaction to avoid
     * intermediate empty emissions on Flows.
     */
    @Transaction
    suspend fun replaceSentMessages(messages: List<Message>) {
        deleteMessagesByState(MessageState.SENT)
        insertMessages(messages)
    }

    @Transaction
    suspend fun clearAllData() {
        clearMessages()
        clearReplies()
        clearUsers()
    }

}

