package local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "messages")
data class MessageDB(
    @PrimaryKey val id: String,
    val body: String,
    val mode: String,
    val isPublic: Boolean,
    val createdAt: String,
    val authorId: String,
)


