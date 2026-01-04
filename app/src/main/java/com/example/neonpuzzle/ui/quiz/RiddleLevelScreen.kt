package com.example.neonpuzzle.ui.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.neonpuzzle.data.AppDatabase
import com.example.neonpuzzle.data.LevelProgress
import com.example.neonpuzzle.data.RiddleAssets
import com.example.neonpuzzle.ui.components.NeonButton
import com.example.neonpuzzle.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LevelViewModel(private val db: AppDatabase) : ViewModel() {
    private val _progressMap = MutableStateFlow<Map<Int, LevelProgress>>(emptyMap())
    val progressMap = _progressMap.asStateFlow()

    init {
        // Initialize Level 1 as unlocked if DB is empty
        viewModelScope.launch {
            if (db.scoreDao().getLevelStatus(1) == null) {
                db.scoreDao().updateLevelProgress(LevelProgress(1, isUnlocked = true, stars = 0))
            }
        }
        
        // Watch for updates
        viewModelScope.launch {
            db.scoreDao().getLevelProgressFlow().collect { list ->
                _progressMap.value = list.associateBy { it.levelId }
            }
        }
    }
}

@Composable
fun RiddleLevelScreen(
    onLevelSelected: (Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val viewModel: LevelViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LevelViewModel(db) as T
            }
        }
    )

    val progress by viewModel.progressMap.collectAsState()
    val levels = RiddleAssets.allLevels

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MainBackgroundBrush)
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeonButton("BACK", onClick = onBack, color = ErrorRed, modifier = Modifier.height(45.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text("RIDDLE MAP", fontSize = 28.sp, fontWeight = FontWeight.Black, color = PrimaryAction)
            }

            // Level Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(levels.size) { index ->
                    val level = levels[index]
                    val levelData = progress[level.id]
                    val isUnlocked = levelData?.isUnlocked == true || level.id == 1 // Level 1 always open
                    val stars = levelData?.stars ?: 0

                    LevelCard(
                        levelNumber = level.id,
                        title = level.title,
                        isLocked = !isUnlocked,
                        stars = stars,
                        onClick = { if (isUnlocked) onLevelSelected(level.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun LevelCard(
    levelNumber: Int,
    title: String,
    isLocked: Boolean,
    stars: Int,
    onClick: () -> Unit
) {
    // Visual Style
    val cardColor = if (isLocked) Color.Gray else CardBackground
    val contentAlpha = if (isLocked) 0.5f else 1f

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(cardColor)
            .clickable(enabled = !isLocked, onClick = onClick)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().alpha(contentAlpha),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isLocked) {
                Text("🔒", fontSize = 40.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("LOCKED", fontWeight = FontWeight.Bold, color = Color.White)
            } else {
                Text("LEVEL $levelNumber", color = SecondaryAction, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(title, color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                // Stars
                Row {
                    repeat(3) { i ->
                        Text(
                            text = "★", 
                            color = if (i < stars) AccentYellow else Color.LightGray, 
                            fontSize = 24.sp
                        )
                    }
                }
            }
        }
    }
}