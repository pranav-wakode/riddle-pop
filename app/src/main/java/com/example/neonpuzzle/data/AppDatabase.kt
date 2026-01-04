package com.example.neonpuzzle.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "scores")
data class UserScore(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val gameType: String, // "QUIZ", "SLIDING", "JIGSAW"
    val score: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface ScoreDao {
    @Query("SELECT * FROM scores ORDER BY score DESC LIMIT 10")
    fun getHighScores(): Flow<List<UserScore>>

    @Insert
    suspend fun insertScore(userScore: UserScore)
}

@Database(entities = [UserScore::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scoreDao(): ScoreDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "neon_puzzle_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}