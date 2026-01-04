package com.example.neonpuzzle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.neonpuzzle.ui.HomeScreen
import com.example.neonpuzzle.ui.puzzle.JigsawPuzzleScreen
import com.example.neonpuzzle.ui.puzzle.SlidingPuzzleScreen
import com.example.neonpuzzle.ui.quiz.QuizScreen
import com.example.neonpuzzle.ui.theme.NeonPuzzleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NeonPuzzleTheme {
                NeonApp()
            }
        }
    }
}

@Composable
fun NeonApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onNavigateToQuiz = { navController.navigate("quiz") },
                onNavigateToSliding = { navController.navigate("sliding") },
                onNavigateToJigsaw = { navController.navigate("jigsaw") }
            )
        }
        composable("quiz") {
            QuizScreen(onBack = { navController.popBackStack() })
        }
        composable("sliding") {
            SlidingPuzzleScreen(onBack = { navController.popBackStack() })
        }
        composable("jigsaw") {
            JigsawPuzzleScreen(onBack = { navController.popBackStack() })
        }
    }
}