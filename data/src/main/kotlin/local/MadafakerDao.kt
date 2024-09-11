package local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import local.entity.MessageDB
import local.entity.ReplyDB
import local.entity.UserDB

@Dao
interface MadafakerDao {
    @Insert
    suspend fun insertMessage(messageDB: MessageDB)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageDB>)

    @Insert
    suspend fun insertReplyDB(replyDB: ReplyDB)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReplyDBs(replyDBs: List<ReplyDB>)

    @Insert
    suspend fun insertUserDB(userDB: UserDB)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserDBs(users: List<UserDB>)

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserDB?

    @Query("SELECT * FROM messages")
    suspend fun getAllMessage(): List<MessageDB>


}