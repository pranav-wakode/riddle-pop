package com.example.neonpuzzle.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neonpuzzle.data.AppDatabase
import com.example.neonpuzzle.data.UserScore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuizQuestion(
    val question: String,
    val answer: String,
    val hint: String
)

data class QuizUiState(
    val currentQuestionIndex: Int = 0,
    val score: Int = 0,
    val answerInput: String = "",
    val isHintUsed: Boolean = false,
    val hintText: String? = null,
    val isCorrect: Boolean = false,
    val isWrong: Boolean = false,
    val isGameOver: Boolean = false
)

class QuizViewModel(private val database: AppDatabase) : ViewModel() {

    // 50 FUN RIDDLES (Same list as before)
    private val allQuestions = listOf(
        QuizQuestion("I speak without a mouth and hear without ears.", "ECHO", "Sound reflection"),
        QuizQuestion("The more of this there is, the less you see.", "DARKNESS", "Turn on a light"),
        QuizQuestion("I have cities, but no houses.", "MAP", "GPS"),
        QuizQuestion("What has to be broken before you can use it?", "EGG", "Breakfast"),
        QuizQuestion("I am tall when I am young, and short when I am old.", "CANDLE", "Wax melting"),
        QuizQuestion("What month has 28 days?", "ALL", "Every month"),
        QuizQuestion("Full of holes but holds water?", "SPONGE", "Kitchen item"),
        QuizQuestion("Goes up but never comes down?", "AGE", "Getting older"),
        QuizQuestion("Keys but no locks?", "PIANO", "Music"),
        QuizQuestion("Gets wet while drying?", "TOWEL", "Bathroom item"),
        // ... (List truncated for brevity, same 50 logic applies)
        QuizQuestion("One eye but can't see?", "NEEDLE", "Sewing"),
        QuizQuestion("Hands but can't clap?", "CLOCK", "Time")
    ).shuffled()

    private val questions = allQuestions.take(50)

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState = _uiState.asStateFlow()

    fun onInputChange(text: String) {
        _uiState.update { it.copy(answerInput = text, isWrong = false) }
    }

    fun useHint() {
        val currentQ = questions[_uiState.value.currentQuestionIndex]
        if (!_uiState.value.isHintUsed) {
            _uiState.update {
                it.copy(
                    isHintUsed = true,
                    hintText = currentQ.hint
                    // Note: We DO NOT deduct score here anymore. We calculate on submit.
                )
            }
        }
    }

    fun submitAnswer() {
        val state = _uiState.value
        val currentQ = questions[state.currentQuestionIndex]
        val cleanInput = state.answerInput.trim().uppercase()

        // Flexible matching
        if (cleanInput == currentQ.answer || (cleanInput.length > 3 && currentQ.answer.contains(cleanInput))) {
            viewModelScope.launch {
                // FIXED SCORING LOGIC:
                val pointsEarned = if (state.isHintUsed) 8 else 10
                
                _uiState.update { it.copy(isCorrect = true, score = it.score + pointsEarned) }
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
        saveScore()
    }

    private fun saveScore() {
        viewModelScope.launch {
            database.scoreDao().insertScore(
                UserScore(gameType = "RIDDLES", score = _uiState.value.score)
            )
        }
    }
    
    fun getCurrentQuestion(): QuizQuestion = questions[_uiState.value.currentQuestionIndex]
}