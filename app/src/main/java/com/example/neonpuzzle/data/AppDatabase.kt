package com.example.neonpuzzle.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow // Import Flow

@Entity(tableName = "user_scores")
data class UserScore(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val gameType: String, 
    val score: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface ScoreDao {
    @Insert
    suspend fun insertScore(score: UserScore)

    // Changed to return Flow. This makes it "Live"
    @Query("SELECT * FROM user_scores")
    fun getAllScoresFlow(): Flow<List<UserScore>>
}

@Database(entities = [UserScore::class], version = 3, exportSchema = false) // Bumped version
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
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}