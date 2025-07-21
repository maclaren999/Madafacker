package local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.bbuddies.madafaker.common_domain.model.Message
import com.bbuddies.madafaker.common_domain.model.Reply
import com.bbuddies.madafaker.common_domain.model.User
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


// Type converters for complex fields
class Converters {
    @TypeConverter
    fun fromReplyList(value: List<Reply>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toReplyList(value: String): List<Reply> {
        return Json.decodeFromString(value)
    }
}

@Database(
    entities = [
        Message::class,
        Reply::class,
        User::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MadafakerDatabase : RoomDatabase() {
    abstract fun getMadafakerDao(): MadafakerDao

//    // Future: When server supports push
//    suspend fun handlePushNotification(messageId: String) {
//        // Just refresh messages - no API changes needed
//        refreshMessages()
//    }
}