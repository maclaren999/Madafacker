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
    fun fromReplyList(value: List<Reply>?): String? {
        return value?.let { Json.encodeToString(it) }
    }

    @TypeConverter
    fun toReplyList(value: String?): List<Reply>? {
        return value?.let { Json.decodeFromString(it) }
    }
    // Note: RatingStats uses @Embedded, not TypeConverter - Room handles it automatically
}

@Database(
    entities = [
        Message::class,
        Reply::class,
        User::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MadafakerDatabase : RoomDatabase() {
    abstract fun getMadafakerDao(): MadafakerDao

}