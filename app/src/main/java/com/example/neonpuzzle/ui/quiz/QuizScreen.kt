package com.example.neonpuzzle.ui.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.neonpuzzle.data.AppDatabase
import com.example.neonpuzzle.ui.components.*
import com.example.neonpuzzle.ui.theme.*

@Composable
fun QuizScreen(
    levelId: Int,
    onBack: () -> Unit,
    onNextLevel: () -> Unit
) {
    BackHandler(onBack = onBack)

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

    LaunchedEffect(levelId) {
        viewModel.loadLevel(levelId)
    }

    val uiState by viewModel.uiState.collectAsState()
    val currentQuestion = viewModel.getCurrentQuestion()
    val shakeOffset by animateFloatAsState(targetValue = if (uiState.isWrong) 30f else 0f, label = "shake")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MainBackgroundBrush)
            .statusBarsPadding()
    ) {
        // --- GAME CONTENT ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(viewModel.getCurrentLevelTitle().uppercase(), color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("IQ: ", color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("${uiState.score}", color = PrimaryAction, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            // Riddle Area
            if (currentQuestion != null && !uiState.isGameOver) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(x = shakeOffset.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (uiState.currentQuestionIndex > 0) {
                            Text(
                                text = "< PREV",
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.clickable { viewModel.previousQuestion() }
                            )
                        } else {
                            Spacer(Modifier.width(1.dp))
                        }
                        Text("RIDDLE ${uiState.currentQuestionIndex + 1}", color = SecondaryAction, fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Spacer(Modifier.width(40.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    NeonCard(modifier = Modifier.fillMaxWidth()) {
                        Text(currentQuestion.question, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, lineHeight = 32.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    NeonTextField(
                        value = uiState.answerInput,
                        onValueChange = { viewModel.onInputChange(it) },
                        label = "WHO AM I?",
                        isError = uiState.isWrong
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (uiState.hintText == null) {
                            NeonButton("?", onClick = { viewModel.useHint() }, color = SecondaryAction, modifier = Modifier.width(60.dp))
                        }
                        NeonButton("SUBMIT", onClick = { viewModel.submitAnswer() }, color = PrimaryAction, modifier = Modifier.weight(1f))
                    }

                    if (uiState.hintText != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("HINT: ${uiState.hintText}", color = AccentYellow, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        }

        // --- OVERLAYS ---

        if (uiState.isGameOver) {
            // 1. Confetti (Bottom Layer) - Rendered First
            CelebrationOverlay(visible = true)
            
            // 2. Card (Top Layer) - Rendered Second
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = true) {} // Blocks clicks to game, but accepts clicks for buttons
            ) {
                NeonCard(modifier = Modifier.align(Alignment.Center)) {
                    Text("LEVEL COMPLETE!", color = SuccessGreen, fontSize = 28.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Total Score: ${uiState.score}", color = TextPrimary, fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(32.dp))
                    NeonButton("NEXT LEVEL", onClick = onNextLevel, color = PrimaryAction)
                    Spacer(modifier = Modifier.height(16.dp))
                    NeonButton("MENU", onClick = onBack, color = SecondaryAction)
                }
            }
        }

        if (uiState.isCorrect) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(enabled = false) {} 
            ) {
                CelebrationOverlay(visible = true)
                NeonCard(modifier = Modifier.align(Alignment.Center)) {
                    Text("BRILLIANT!", color = SuccessGreen, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("+${uiState.lastPointsEarned} Points", color = TextSecondary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}