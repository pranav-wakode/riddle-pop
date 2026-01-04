package com.example.neonpuzzle.ui.quiz

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.neonpuzzle.data.AppDatabase
import com.example.neonpuzzle.ui.components.*
import com.example.neonpuzzle.ui.theme.*

@Composable
fun QuizScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    
    val viewModel: QuizViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return QuizViewModel(db) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()
    val currentQuestion = viewModel.getCurrentQuestion()

    val shakeOffset by animateFloatAsState(targetValue = if (uiState.isWrong) 30f else 0f, label = "shake")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MainBackgroundBrush)
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NeonButton("EXIT", onClick = onBack, color = ErrorRed, modifier = Modifier.height(48.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text("SCORE", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("${uiState.score}", color = PrimaryAction, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            }
        }

        if (uiState.isGameOver) {
             NeonCard(modifier = Modifier.align(Alignment.Center)) {
                Text("BRAIN MASTER!", color = PrimaryAction, fontSize = 32.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Final Score: ${uiState.score}", color = TextPrimary, fontSize = 24.sp)
                Spacer(modifier = Modifier.height(32.dp))
                NeonButton("RETURN", onClick = onBack, color = SecondaryAction)
            }
            CelebrationOverlay(visible = true)
        } else {
            // --- MAIN CONTENT ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = shakeOffset.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "RIDDLE ${uiState.currentQuestionIndex + 1}/50",
                    color = SecondaryAction,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(24.dp))

                NeonCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = currentQuestion.question,
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 32.sp
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                NeonTextField(
                    value = uiState.answerInput,
                    onValueChange = { viewModel.onInputChange(it) },
                    label = "WHO AM I?",
                    isError = uiState.isWrong
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.hintText != null) {
                    Text(text = "Hint: ${uiState.hintText}", color = AccentYellow, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                } else {
                    TextButton(onClick = { viewModel.useHint() }) {
                        Text("Need a Clue? (-2 pts)", color = SecondaryAction)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                NeonButton(
                    text = "SOLVE",
                    onClick = { viewModel.submitAnswer() },
                    color = PrimaryAction,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
            }
            
            // --- SUCCESS OVERLAY ---
            if (uiState.isCorrect) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable(enabled = false) {} 
                )
                
                CelebrationOverlay(visible = true)
                
                NeonCard(modifier = Modifier.align(Alignment.Center)) {
                    Text("BRILLIANT!", color = SuccessGreen, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // FIX: Dynamic Points Display
                    Text("+${uiState.lastPointsEarned} Points", color = TextSecondary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading next riddle...", color = SecondaryAction)
                }
            }
        }
    }
}