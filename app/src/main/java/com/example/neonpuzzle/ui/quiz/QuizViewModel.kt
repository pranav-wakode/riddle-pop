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
    val isGameOver: Boolean = false,
    val lastPointsEarned: Int = 0 // New field to show +10 or +8
)

class QuizViewModel(private val database: AppDatabase) : ViewModel() {

    // 50 FUN RIDDLES
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
        QuizQuestion("One eye but can't see?", "NEEDLE", "Sewing"),
        QuizQuestion("Hands but can't clap?", "CLOCK", "Time"),
        QuizQuestion("Legs but doesn't walk?", "TABLE", "Furniture"),
        QuizQuestion("A neck but no head?", "SHIRT", "Clothing"),
        QuizQuestion("Starts with T, ends with T, has T in it?", "TEAPOT", "Drink"),
        QuizQuestion("Has words but never speaks?", "BOOK", "Reading"),
        QuizQuestion("Has a thumb and four fingers but isn't alive?", "GLOVE", "Hand wear"),
        QuizQuestion("What gets bigger the more you take away?", "HOLE", "Digging"),
        QuizQuestion("What comes down but never goes up?", "RAIN", "Weather"),
        QuizQuestion("I have a face and two hands but no arms or legs.", "CLOCK", "Timepiece"),
        QuizQuestion("What has to be broken before you can use it?", "EGG", "Cooking"),
        QuizQuestion("What begins with an E and only has one letter?", "ENVELOPE", "Mail"),
        QuizQuestion("What has many keys but can't open a single lock?", "PIANO", "Instrument"),
        QuizQuestion("What has a head and a tail but no body?", "COIN", "Money"),
        QuizQuestion("Where does today come before yesterday?", "DICTIONARY", "Words"),
        QuizQuestion("What has 13 hearts but no other organs?", "CARDS", "Deck"),
        QuizQuestion("I have a bed but I never sleep.", "RIVER", "Water"),
        QuizQuestion("What can you break, even if you never pick it up?", "PROMISE", "Trust"),
        QuizQuestion("I shave every day, but my beard stays the same.", "BARBER", "Haircut"),
        QuizQuestion("What can't be put in a saucepan?", "LID", "Cover"),
        QuizQuestion("What goes up and down but doesn't move?", "STAIRS", "Steps"),
        QuizQuestion("If you’re running in a race and you pass the person in second place, what place are you in?", "SECOND", "Logic"),
        QuizQuestion("It belongs to you, but other people use it more than you do.", "NAME", "Identity"),
        QuizQuestion("What has a bottom at the top?", "LEG", "Body part"),
        QuizQuestion("What has an eye but can not see?", "NEEDLE", "Thread"),
        QuizQuestion("What kind of band never plays music?", "RUBBER", "Elastic"),
        QuizQuestion("What has many teeth, but can’t bite?", "COMB", "Hair"),
        QuizQuestion("What has words, but never speaks?", "BOOK", "Read"),
        QuizQuestion("What is cut on a table, but never eaten?", "CARDS", "Play"),
        QuizQuestion("What has legs, but doesn’t walk?", "TABLE", "Furniture"),
        QuizQuestion("What loses its head in the morning and gets it back at night?", "PILLOW", "Sleep"),
        QuizQuestion("What is black when it’s clean and white when it’s dirty?", "CHALKBOARD", "School"),
        QuizQuestion("What is bought by the yard and worn by the foot?", "CARPET", "Rug"),
        QuizQuestion("What has a neck but no head?", "SHIRT", "Wear"),
        QuizQuestion("What goes up but never comes down?", "AGE", "Years"),
        QuizQuestion("What can fill a room but takes up no space?", "LIGHT", "Bright"),
        QuizQuestion("I am an odd number. Take away a letter and I become even.", "SEVEN", "7"),
        QuizQuestion("If you drop me I’m sure to crack, but give me a smile and I’ll always smile back.", "MIRROR", "Reflect"),
        QuizQuestion("The more you take, the more you leave behind.", "FOOTSTEPS", "Walk"),
        QuizQuestion("What is full of holes but still holds water?", "SPONGE", "Soak")
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
                it.copy(isHintUsed = true, hintText = currentQ.hint)
            }
        }
    }

    fun submitAnswer() {
        val state = _uiState.value
        val currentQ = questions[state.currentQuestionIndex]
        val cleanInput = state.answerInput.trim().uppercase()

        if (cleanInput == currentQ.answer || (cleanInput.length > 3 && currentQ.answer.contains(cleanInput))) {
            viewModelScope.launch {
                // 1. Calculate Points (10 or 8)
                val pointsEarned = if (state.isHintUsed) 8 else 10
                
                // 2. Save to DB IMMEDIATELY so Home Screen sees it
                saveScore(pointsEarned)

                // 3. Update UI to show success + correct point amount
                _uiState.update { 
                    it.copy(
                        isCorrect = true, 
                        score = it.score + pointsEarned,
                        lastPointsEarned = pointsEarned // Store for UI display
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
    }

    private suspend fun saveScore(points: Int) {
        database.scoreDao().insertScore(
            UserScore(gameType = "RIDDLES", score = points)
        )
    }
    
    fun getCurrentQuestion(): QuizQuestion = questions[_uiState.value.currentQuestionIndex]
}