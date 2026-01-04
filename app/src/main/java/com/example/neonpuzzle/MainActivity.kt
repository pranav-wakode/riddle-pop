package com.example.neonpuzzle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.neonpuzzle.ui.HomeScreen
import com.example.neonpuzzle.ui.puzzle.JigsawPuzzleScreen
import com.example.neonpuzzle.ui.puzzle.SlidingPuzzleScreen
import com.example.neonpuzzle.ui.quiz.QuizScreen
import com.example.neonpuzzle.ui.quiz.RiddleLevelScreen // New Import
import com.example.neonpuzzle.ui.theme.NeonPuzzleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NeonPuzzleTheme {
                val navController = rememberNavController()
                
                NavHost(navController = navController, startDestination = "home") {
                    
                    // Home
                    composable("home") {
                        HomeScreen(
                            onNavigateToQuiz = { navController.navigate("riddle_levels") }, // Changed destination
                            onNavigateToSliding = { navController.navigate("sliding") },
                            onNavigateToJigsaw = { navController.navigate("jigsaw") }
                        )
                    }

                    // NEW: Riddle Levels Menu
                    composable("riddle_levels") {
                        RiddleLevelScreen(
                            onLevelSelected = { levelId -> 
                                navController.navigate("quiz/$levelId") 
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // Quiz Game (Accepts Level ID)
                    composable(
                        "quiz/{levelId}",
                        arguments = listOf(navArgument("levelId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val levelId = backStackEntry.arguments?.getInt("levelId") ?: 1
                        QuizScreen(
                            levelId = levelId,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // Sliding Puzzle
                    composable("sliding") {
                        SlidingPuzzleScreen(onBack = { navController.popBackStack() })
                    }

                    // Jigsaw Puzzle
                    composable("jigsaw") {
                        JigsawPuzzleScreen(onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}