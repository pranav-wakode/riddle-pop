package com.example.neonpuzzle.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.neonpuzzle.data.AppDatabase
import com.example.neonpuzzle.ui.components.NeonButton
import com.example.neonpuzzle.ui.components.NeonCard
import com.example.neonpuzzle.ui.theme.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(private val db: AppDatabase) : ViewModel() {
    // Automatically updates when DB changes
    val totalScore = db.scoreDao().getAllScoresFlow()
        .map { scores -> scores.sumOf { it.score } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
}

@Composable
fun HomeScreen(
    onNavigateToQuiz: () -> Unit,
    onNavigateToSliding: () -> Unit,
    onNavigateToJigsaw: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val viewModel: HomeViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(db) as T
            }
        }
    )
    
    val score by viewModel.totalScore.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MainBackgroundBrush),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "RIDDLE POP", 
                color = PrimaryAction,
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Text(
                text = "BRAIN TEASERS & PUZZLES",
                color = TextSecondary,
                fontSize = 16.sp,
                letterSpacing = 4.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            NeonCard {
                Text("TOTAL IQ SCORE", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("$score", color = SuccessGreen, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            NeonCard {
                NeonButton(
                    text = "SOLVE RIDDLES",
                    onClick = onNavigateToQuiz,
                    color = PrimaryAction,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                NeonButton(
                    text = "SLIDING PUZZLE",
                    onClick = onNavigateToSliding,
                    color = SecondaryAction,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                NeonButton(
                    text = "JIGSAW PUZZLE",
                    onClick = onNavigateToJigsaw,
                    color = AccentYellow,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
            }
        }
    }
}