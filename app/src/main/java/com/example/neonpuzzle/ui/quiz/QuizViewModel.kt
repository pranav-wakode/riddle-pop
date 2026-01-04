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

    // Load Level & Resume Progress
    fun loadLevel(levelId: Int) {
        currentLevelId = levelId
        val level = RiddleAssets.getLevelById(levelId)
        if (level != null) {
            questions = level.questions

            viewModelScope.launch {
                // Fetch saved progress
                val savedProgress = database.scoreDao().getLevelStatus(levelId)
                val resumeIndex = savedProgress?.lastQuestionIndex ?: 0

                // If level was already finished (index >= size), maybe reset to 0 or keep at end?
                // Let's reset to 0 only if they explicitly replay a finished level,
                // but for now, we clamp it.
                val safeIndex = if (resumeIndex >= questions.size) 0 else resumeIndex

                _uiState.value = QuizUiState(currentQuestionIndex = safeIndex)
            }
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
                saveGlobalScore(pointsEarned)

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

    // Move to next and SAVE progress
    private fun nextQuestion() {
        val currentIndex = _uiState.value.currentQuestionIndex
        val nextIndex = currentIndex + 1

        if (nextIndex < questions.size) {
            // 1. Update UI
            _uiState.update {
                it.copy(
                    currentQuestionIndex = nextIndex,
                    answerInput = "",
                    isCorrect = false,
                    isHintUsed = false,
                    hintText = null
                )
            }
            // 2. Save "Last Question Index" to DB
            saveLevelProgress(nextIndex)
        } else {
            finishGame()
        }
    }

    private fun finishGame() {
        _uiState.update { it.copy(isGameOver = true) }
        unlockNextLevel()
        // Reset current level progress to 0 so they can replay it later from start
        saveLevelProgress(0)
    }

    private fun saveLevelProgress(index: Int) {
        viewModelScope.launch {
            // We need to preserve the existing "stars" and "unlocked" status
            val existing = database.scoreDao().getLevelStatus(currentLevelId)
            val stars = existing?.stars ?: 0
            val unlocked = existing?.isUnlocked ?: true

            database.scoreDao().updateLevelProgress(
                LevelProgress(
                    levelId = currentLevelId,
                    isUnlocked = unlocked,
                    stars = stars,
                    lastQuestionIndex = index
                )
            )
        }
    }

    private fun unlockNextLevel() {
        viewModelScope.launch {
            val stars = when {
                _uiState.value.score >= 40 -> 3
                _uiState.value.score >= 25 -> 2
                else -> 1
            }

            // Update current level with Stars (and reset index to 0 as done in finishGame)
            database.scoreDao().updateLevelProgress(
                LevelProgress(
                    levelId = currentLevelId,
                    isUnlocked = true,
                    stars = stars,
                    lastQuestionIndex = 0 // Level complete, reset cursor
                )
            )

            // Unlock Next Level
            val nextLevelId = currentLevelId + 1
            if (RiddleAssets.getLevelById(nextLevelId) != null) {
                val existing = database.scoreDao().getLevelStatus(nextLevelId)
                if (existing == null) {
                    database.scoreDao().updateLevelProgress(
                        LevelProgress(levelId = nextLevelId, isUnlocked = true, stars = 0, lastQuestionIndex = 0)
                    )
                }
            }
        }
    }

    private suspend fun saveGlobalScore(points: Int) {
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

    // Allow user to go back manually if they want
    fun previousQuestion() {
        val idx = _uiState.value.currentQuestionIndex
        if (idx > 0) {
            _uiState.update {
                it.copy(
                    currentQuestionIndex = idx - 1,
                    answerInput = "",
                    isCorrect = false,
                    isHintUsed = false,
                    hintText = null
                )
            }
            saveLevelProgress(idx - 1)
        }
    }
}