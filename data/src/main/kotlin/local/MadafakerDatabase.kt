package local

import androidx.room.Database
import androidx.room.RoomDatabase
import local.entity.MessageDB
import local.entity.ReplyDB
import local.entity.UserDB

@Database(entities = [MessageDB::class, ReplyDB::class, UserDB::class], version = 1)
abstract class MadafakerDatabase : RoomDatabase() {

    abstract fun getMadafakerDao(): MadafakerDao

}