package com.example.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.RpMessage
import com.example.data.model.UserCharacter
import kotlinx.coroutines.flow.Flow

@Dao
interface UserCharacterDao {
    @Query("SELECT * FROM user_character WHERE id = 1 LIMIT 1")
    fun getUserCharacter(): Flow<UserCharacter?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserCharacter(character: UserCharacter)

    @Query("DELETE FROM user_character")
    suspend fun clearUserCharacter()
}

@Dao
interface RpMessageDao {
    @Query("SELECT * FROM rp_messages WHERE scenarioId = :scenarioId ORDER BY timestamp ASC")
    fun getMessagesForScenario(scenarioId: String): Flow<List<RpMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: RpMessage)

    @Query("DELETE FROM rp_messages WHERE scenarioId = :scenarioId")
    suspend fun clearMessagesForScenario(scenarioId: String)

    @Query("DELETE FROM rp_messages")
    suspend fun clearAllMessages()
}

@Database(entities = [UserCharacter::class, RpMessage::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userCharacterDao(): UserCharacterDao
    abstract fun rpMessageDao(): RpMessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "beelzebub_rp_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
