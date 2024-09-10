package local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserDB(
    @PrimaryKey val id: String,
    val name: String,
    val updatedAt: String, // TODO  чи потрібні?
    val createdAt: String
)
