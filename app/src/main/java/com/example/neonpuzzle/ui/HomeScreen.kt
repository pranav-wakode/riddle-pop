package com.example.neonpuzzle.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.neonpuzzle.ui.components.NeonButton
import com.example.neonpuzzle.ui.components.NeonCard
import com.example.neonpuzzle.ui.theme.*

@Composable
fun HomeScreen(
    onNavigateToQuiz: () -> Unit,
    onNavigateToSliding: () -> Unit,
    onNavigateToJigsaw: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MainBackgroundBrush), // The new gradient
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "RIDDLE POP", // New Name
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
            
            Spacer(modifier = Modifier.height(48.dp))

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