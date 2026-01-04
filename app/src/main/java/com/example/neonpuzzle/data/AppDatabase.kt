package com.example.neonpuzzle.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// Existing Score Table
@Entity(tableName = "user_scores")
data class UserScore(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val gameType: String, 
    val score: Int,
    val timestamp: Long = System.currentTimeMillis()
)

// NEW: Level Progress Table
@Entity(tableName = "level_progress")
data class LevelProgress(
    @PrimaryKey val levelId: Int, // 1, 2, 3
    val isUnlocked: Boolean = false,
    val stars: Int = 0 // 0 to 3
)

@Dao
interface ScoreDao {
    @Insert
    suspend fun insertScore(score: UserScore)

    @Query("SELECT * FROM user_scores")
    fun getAllScoresFlow(): Flow<List<UserScore>>
    
    // --- LEVEL METHODS ---
    @Query("SELECT * FROM level_progress")
    fun getLevelProgressFlow(): Flow<List<LevelProgress>>
    
    @Query("SELECT * FROM level_progress WHERE levelId = :id")
    suspend fun getLevelStatus(id: Int): LevelProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateLevelProgress(progress: LevelProgress)
}

@Database(entities = [UserScore::class, LevelProgress::class], version = 4, exportSchema = false) // Bumped to 4
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
                .fallbackToDestructiveMigration() // Wipes data on update (Safe for dev)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}