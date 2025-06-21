package local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.model.MessageState
import com.bbuddies.madafaker.common_domain.model.PendingMessage
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

    @Query("SELECT * FROM messages WHERE authorId != :currentUserId ORDER BY localCreatedAt DESC")
    fun observeIncomingMessages(currentUserId: String): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE authorId = :currentUserId ORDER BY localCreatedAt DESC")
    fun observeOutgoingMessages(currentUserId: String): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE localState = :state")
    suspend fun getMessagesByState(state: MessageState): List<Message>

    @Query("DELETE FROM messages WHERE localState = :state")
    suspend fun deleteMessagesByState(state: MessageState)

    @Query("SELECT COUNT(*) FROM messages WHERE localState IN ('PENDING', 'FAILED')")
    fun observePendingCount(): Flow<Int>

    // Reply operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReply(reply: Reply)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReplies(replies: List<Reply>)

    @Query("SELECT * FROM replies WHERE parentId = :parentId ORDER BY createdAt ASC")
    suspend fun getRepliesByParentId(parentId: String): List<Reply>

    @Query("SELECT * FROM replies WHERE id = :replyId")
    suspend fun getReplyById(replyId: String): Reply?

    @Update
    suspend fun updateReply(reply: Reply)

    // User operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): User?

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)

    @Update
    suspend fun updateUser(user: User)

    // Pending Messages operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingMessage(pendingMessage: PendingMessage)

    @Query("SELECT * FROM pending_messages ORDER BY createdAt ASC")
    suspend fun getAllPendingMessages(): List<PendingMessage>

    @Query("SELECT * FROM pending_messages WHERE id = :id")
    suspend fun getPendingMessageById(id: String): PendingMessage?

    @Query("DELETE FROM pending_messages WHERE id = :id")
    suspend fun deletePendingMessage(id: String)

    @Query("DELETE FROM pending_messages")
    suspend fun deleteAllPendingMessages()

    @Update
    suspend fun updatePendingMessage(pendingMessage: PendingMessage)

    @Query("SELECT COUNT(*) FROM pending_messages")
    suspend fun getPendingMessagesCount(): Int
}