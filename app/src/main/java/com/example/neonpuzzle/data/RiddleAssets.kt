package com.example.neonpuzzle.data

// Simple data class for the questions
data class RiddleQuestion(
    val question: String,
    val answer: String,
    val hint: String
)

// Data class for a Level
data class RiddleLevel(
    val id: Int, // 1, 2, 3...
    val title: String, // "The Beginning", "Tricky Business"...
    val questions: List<RiddleQuestion>
)

// --- THIS IS WHERE YOU ADD NEW LEVELS ---
object RiddleAssets {

    val allLevels = listOf(
        RiddleLevel(
            id = 1,
            title = "Warm Up",
            questions = listOf(
                RiddleQuestion("I speak without a mouth and hear without ears.", "ECHO", "Sound reflection"),
                RiddleQuestion("The more of this there is, the less you see.", "DARKNESS", "Turn on a light"),
                RiddleQuestion("I have cities, but no houses.", "MAP", "GPS"),
                RiddleQuestion("What has to be broken before you can use it?", "EGG", "Breakfast"),
                RiddleQuestion("I am tall when I am young, and short when I am old.", "CANDLE", "Wax melting")
            )
        ),
        RiddleLevel(
            id = 2,
            title = "Getting Tricky",
            questions = listOf(
                RiddleQuestion("What month has 28 days?", "ALL", "Every month"),
                RiddleQuestion("Full of holes but holds water?", "SPONGE", "Kitchen item"),
                RiddleQuestion("Goes up but never comes down?", "AGE", "Getting older"),
                RiddleQuestion("Keys but no locks?", "PIANO", "Music"),
                RiddleQuestion("Gets wet while drying?", "TOWEL", "Bathroom item")
            )
        ),
        RiddleLevel(
            id = 3,
            title = "Brain Twisters",
            questions = listOf(
                RiddleQuestion("One eye but can't see?", "NEEDLE", "Sewing"),
                RiddleQuestion("Hands but can't clap?", "CLOCK", "Time"),
                RiddleQuestion("Legs but doesn't walk?", "TABLE", "Furniture"),
                RiddleQuestion("A neck but no head?", "SHIRT", "Clothing"),
                RiddleQuestion("Starts with T, ends with T, has T in it?", "TEAPOT", "Drink")
            )
        ),
        RiddleLevel(
            id = 4,
            title = "Master Class",
            questions = listOf(
                RiddleQuestion("Has words but never speaks?", "BOOK", "Reading"),
                RiddleQuestion("Has a thumb and four fingers but isn't alive?", "GLOVE", "Hand wear"),
                RiddleQuestion("What gets bigger the more you take away?", "HOLE", "Digging"),
                RiddleQuestion("What comes down but never goes up?", "RAIN", "Weather"),
                RiddleQuestion("I have a face and two hands but no arms or legs.", "CLOCK", "Timepiece")
            )
        )
        // COPY AND PASTE TO ADD LEVEL 5, 6, etc...
    )
    
    fun getLevelById(id: Int): RiddleLevel? {
        return allLevels.find { it.id == id }
    }
}