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

    // 50 FUN RIDDLES (Normal Difficulty)
    private val allQuestions = listOf(
        QuizQuestion("I speak without a mouth and hear without ears. I have no body, but I come alive with wind.", "ECHO", "Sound bouncing back"),
        QuizQuestion("The more of this there is, the less you see.", "DARKNESS", "Turn on a light"),
        QuizQuestion("I have cities, but no houses. I have mountains, but no trees. I have water, but no fish.", "MAP", "Google uses me"),
        QuizQuestion("What has to be broken before you can use it?", "EGG", "Breakfast item"),
        QuizQuestion("I am tall when I am young, and I am short when I am old.", "CANDLE", "Melts away"),
        QuizQuestion("What month of the year has 28 days?", "ALL", "Every month has at least 28"),
        QuizQuestion("What is full of holes but still holds water?", "SPONGE", "Bob lives in one"),
        QuizQuestion("What goes up but never comes down?", "AGE", "Birthdays increase it"),
        QuizQuestion("I have keys but open no locks.", "PIANO", "Musical instrument"),
        QuizQuestion("What gets wet while drying?", "TOWEL", "Used after a shower"),
        QuizQuestion("I can fly but have no wings. I can cry but I have no eyes.", "CLOUD", "Rain comes from me"),
        QuizQuestion("What has one eye, but can’t see?", "NEEDLE", "Sewing tool"),
        QuizQuestion("What has hands, but can’t clap?", "CLOCK", "Tick tock"),
        QuizQuestion("What can you catch, but not throw?", "COLD", "Sneeze!"),
        QuizQuestion("What belongs to you, but other people use it more than you?", "NAME", "People call you by it"),
        QuizQuestion("I have a neck but no head.", "BOTTLE", "Holds water"),
        QuizQuestion("What runs all around a backyard, yet never moves?", "FENCE", "Boundary"),
        QuizQuestion("What has a thumb and four fingers, but is not a hand?", "GLOVE", "Winter wear"),
        QuizQuestion("What comes once in a minute, twice in a moment, but never in a thousand years?", "M", "The letter M"),
        QuizQuestion("I have words, but I never speak.", "BOOK", "Read me"),
        QuizQuestion("The more you take, the more you leave behind.", "FOOTSTEPS", "Walking in sand"),
        QuizQuestion("What goes through cities and fields, but never moves?", "ROAD", "Cars drive on it"),
        QuizQuestion("I am an odd number. Take away a letter and I become even.", "SEVEN", "Number 7"),
        QuizQuestion("What has a head and a tail but no body?", "COIN", "Money"),
        QuizQuestion("What can travel all around the world without leaving its corner?", "STAMP", "On a letter"),
        QuizQuestion("If you drop me I’m sure to crack, but give me a smile and I’ll always smile back.", "MIRROR", "Reflection"),
        QuizQuestion("I have branches, but no fruit, trunk or leaves.", "BANK", "Money is stored here"),
        QuizQuestion("What tastes better than it smells?", "TONGUE", "Body part"),
        QuizQuestion("What kind of room has no doors or windows?", "MUSHROOM", "Fungi"),
        QuizQuestion("What creates a shelter but has no walls?", "UMBRELLA", "Rain protection"),
        QuizQuestion("What has a bottom at the top?", "LEG", "Body part"),
        QuizQuestion("I am always coming, but never arrive.", "TOMORROW", "Future"),
        QuizQuestion("What goes up and down but doesn't move?", "STAIRS", "Steps"),
        QuizQuestion("Where does today come before yesterday?", "DICTIONARY", "Alphabetical order"),
        QuizQuestion("What has 13 hearts, but no other organs?", "CARDS", "Deck of..."),
        QuizQuestion("I have a bed but I never sleep. I have a mouth but I never speak.", "RIVER", "Flows water"),
        QuizQuestion("What can you break, even if you never pick it up or touch it?", "PROMISE", "Word of honor"),
        QuizQuestion("What goes up as soon as the rain comes down?", "UMBRELLA", "Keeps you dry"),
        QuizQuestion("I’m light as a feather, yet the strongest man can’t hold me for five minutes.", "BREATH", "Air in lungs"),
        QuizQuestion("What begins with T, ends with T, and has T in it?", "TEAPOT", "Hot drink vessel"),
        QuizQuestion("What has many teeth, but can’t bite?", "COMB", "Hair tool"),
        QuizQuestion("What has words, but never speaks?", "BOOK", "Reading material"),
        QuizQuestion("What is cut on a table, but never eaten?", "CARDS", "Playing deck"),
        QuizQuestion("What has legs, but doesn’t walk?", "TABLE", "Furniture"),
        QuizQuestion("What loses its head in the morning and gets it back at night?", "PILLOW", "Sleep on it"),
        QuizQuestion("What is black when it’s clean and white when it’s dirty?", "CHALKBOARD", "Classroom item"),
        QuizQuestion("What is bought by the yard and worn by the foot?", "CARPET", "Floor covering"),
        QuizQuestion("What has a neck but no head?", "SHIRT", "Clothing"),
        QuizQuestion("What goes up but never comes down?", "AGE", "Years"),
        QuizQuestion("What can fill a room but takes up no space?", "LIGHT", "From a bulb")
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
                    hintText = currentQ.hint,
                    score = it.score - 2
                )
            }
        }
    }

    fun submitAnswer() {
        val state = _uiState.value
        val currentQ = questions[state.currentQuestionIndex]
        val cleanInput = state.answerInput.trim().uppercase()

        // Relaxed matching: Allow containment if answer is long, or exact match
        if (cleanInput == currentQ.answer || (cleanInput.length > 3 && currentQ.answer.contains(cleanInput))) {
            viewModelScope.launch {
                _uiState.update { it.copy(isCorrect = true, score = it.score + 10) }
                delay(2000) // Longer celebration
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