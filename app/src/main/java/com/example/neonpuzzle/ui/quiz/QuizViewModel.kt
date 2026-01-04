package com.example.neonpuzzle.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neonpuzzle.data.AppDatabase
import com.example.neonpuzzle.data.LevelProgress
import com.example.neonpuzzle.data.RiddleAssets
import com.example.neonpuzzle.data.RiddleQuestion
import com.example.neonpuzzle.data.UserScore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuizUiState(
    val currentQuestionIndex: Int = 0,
    val score: Int = 0,
    val answerInput: String = "",
    val isHintUsed: Boolean = false,
    val hintText: String? = null,
    val isCorrect: Boolean = false,
    val isWrong: Boolean = false,
    val isGameOver: Boolean = false,
    val lastPointsEarned: Int = 0
)

class QuizViewModel(private val database: AppDatabase) : ViewModel() {

    private var questions: List<RiddleQuestion> = emptyList()
    private var currentLevelId: Int = 1

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState = _uiState.asStateFlow()

    // NEW: Load specific level
    fun loadLevel(levelId: Int) {
        currentLevelId = levelId
        val level = RiddleAssets.getLevelById(levelId)
        if (level != null) {
            questions = level.questions
            _uiState.value = QuizUiState() // Reset state
        }
    }

    fun onInputChange(text: String) {
        _uiState.update { it.copy(answerInput = text, isWrong = false) }
    }

    fun useHint() {
        if (questions.isEmpty()) return
        val currentQ = questions[_uiState.value.currentQuestionIndex]
        if (!_uiState.value.isHintUsed) {
            _uiState.update {
                it.copy(isHintUsed = true, hintText = currentQ.hint)
            }
        }
    }

    fun submitAnswer() {
        if (questions.isEmpty()) return
        val state = _uiState.value
        val currentQ = questions[state.currentQuestionIndex]
        val cleanInput = state.answerInput.trim().uppercase()

        if (cleanInput == currentQ.answer || (cleanInput.length > 3 && currentQ.answer.contains(cleanInput))) {
            viewModelScope.launch {
                val pointsEarned = if (state.isHintUsed) 8 else 10
                saveScore(pointsEarned) // Save global score

                _uiState.update { 
                    it.copy(
                        isCorrect = true, 
                        score = it.score + pointsEarned,
                        lastPointsEarned = pointsEarned
                    ) 
                }
                delay(2000)
                nextQuestion()
            }
        } else {
            _uiState.update { it.copy(isWrong = true) }
        }
    }

    private fun nextQuestion() {
        val currentIndex = _uiState.value.currentQuestionIndex
        if (currentIndex < questions.size - 1) {
            _uiState.update {
                it.copy(
                    currentQuestionIndex = currentIndex + 1,
                    answerInput = "",
                    isCorrect = false,
                    isHintUsed = false,
                    hintText = null
                )
            }
        } else {
            finishGame()
        }
    }

    private fun finishGame() {
        _uiState.update { it.copy(isGameOver = true) }
        unlockNextLevel()
    }

    private fun unlockNextLevel() {
        viewModelScope.launch {
            // Calculate Stars based on score
            // Max score per level (5 Qs) = 50. 
            // 3 Stars > 40, 2 Stars > 25, 1 Star > 0
            val stars = when {
                _uiState.value.score >= 40 -> 3
                _uiState.value.score >= 25 -> 2
                else -> 1
            }

            // 1. Save Progress for CURRENT Level
            database.scoreDao().updateLevelProgress(
                LevelProgress(levelId = currentLevelId, isUnlocked = true, stars = stars)
            )

            // 2. Unlock NEXT Level (if it exists)
            val nextLevelId = currentLevelId + 1
            if (RiddleAssets.getLevelById(nextLevelId) != null) {
                // Only unlock if not already unlocked (preserve previous stars if any)
                val existing = database.scoreDao().getLevelStatus(nextLevelId)
                if (existing == null) {
                    database.scoreDao().updateLevelProgress(
                        LevelProgress(levelId = nextLevelId, isUnlocked = true, stars = 0)
                    )
                }
            }
        }
    }

    private suspend fun saveScore(points: Int) {
        database.scoreDao().insertScore(
            UserScore(gameType = "RIDDLES", score = points)
        )
    }
    
    fun getCurrentQuestion(): RiddleQuestion? {
        if (questions.isEmpty()) return null
        return questions[_uiState.value.currentQuestionIndex]
    }
    
    fun getCurrentLevelTitle(): String {
        return RiddleAssets.getLevelById(currentLevelId)?.title ?: "Riddle"
    }
}