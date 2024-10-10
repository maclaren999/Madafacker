package local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "replies")
data class ReplyDB(
    @PrimaryKey val id: String,
    val body: String,
    val mode: String,
    val isPublic: Boolean,
    val createdAt: String,//TODO чи потрібні?
    val authorId: String,//TODO чи потрібні?
    val parentId: String?
)