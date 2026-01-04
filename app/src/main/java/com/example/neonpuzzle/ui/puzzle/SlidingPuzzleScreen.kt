package com.example.neonpuzzle.ui.puzzle

import android.graphics.BitmapFactory
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.neonpuzzle.data.AppDatabase
import com.example.neonpuzzle.ui.components.*
import com.example.neonpuzzle.ui.theme.*
import java.io.InputStream

@SuppressLint("DiscouragedApi")
@Composable
fun SlidingPuzzleScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    
    val viewModel: SlidingPuzzleViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SlidingPuzzleViewModel(db) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()
    // State to toggle the hint visibility
    var showGhostImage by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val inputStream: InputStream? = context.contentResolver.openInputStream(it)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            viewModel.initializeGame(bitmap, 3) 
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MainBackgroundBrush)
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- HEADER ---
            Row(
                modifier = Modifier.fillMaxWidth().height(60.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeonButton("BACK", onClick = onBack, color = ErrorRed, modifier = Modifier.height(45.dp))
            }

            if (uiState.sourceImage == null) {
                // --- SETUP MODE (No Image Selected Yet) ---
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("SLIDE MASTER", color = PrimaryAction, fontSize = 32.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(32.dp))
                    NeonCard {
                        Text("SELECT SOURCE", color = TextSecondary)
                        Spacer(modifier = Modifier.height(16.dp))
                        NeonButton("GALLERY", onClick = { launcher.launch("image/*") }, color = SecondaryAction)
                        Spacer(modifier = Modifier.height(12.dp))
                        NeonButton("DEFAULT", onClick = {
                            try {
                                val resId = context.resources.getIdentifier("default_puzzle", "drawable", context.packageName)
                                val bitmap = if (resId != 0) BitmapFactory.decodeResource(context.resources, resId) 
                                             else PuzzleUtils.createPlaceholderBitmap(800, 800)
                                viewModel.initializeGame(bitmap, 3)
                            } catch (e: Exception) {
                                viewModel.initializeGame(PuzzleUtils.createPlaceholderBitmap(800, 800), 3)
                            }
                        }, color = AccentYellow)
                    }
                }
            } else {
                // --- GAME MODE ---
                
                // Score/Moves Header
                Text("MOVES: ${uiState.moves}", color = TextSecondary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                // --- THE PUZZLE BOARD ---
                Box(
                    modifier = Modifier
                        .size(340.dp) // Fixed size for the puzzle area
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackground)
                        .border(4.dp, PrimaryAction, RoundedCornerShape(12.dp))
                ) {
                    // 1. THE TILES (Game Grid) - Rendered First (Bottom Layer)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(uiState.gridSize),
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = false
                    ) {
                        items(uiState.tiles.size) { index ->
                            val tileId = uiState.tiles[index]
                            val size = uiState.gridSize
                            
                            // If this is the "empty" tile slot
                            if (tileId == (size * size) - 1) {
                                Box(modifier = Modifier.aspectRatio(1f))
                            } else {
                                Image(
                                    bitmap = uiState.imageChunks[tileId].asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .padding(1.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable { viewModel.onTileClick(index) },
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    // 2. THE HINT OVERLAY (Ghost Image) - Rendered Second (Top Layer)
                    // We use zIndex to force it above the tiles.
                    if (showGhostImage && uiState.sourceImage != null) {
                        Image(
                            bitmap = PuzzleUtils.getCroppedSquare(uiState.sourceImage!!).asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(0.4f) // Semi-transparent overlay
                                .zIndex(5f), // Force on top
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // --- CONTROLS FOOTER ---
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    
                    // The Hint Button (Now plainly visible below the puzzle)
                    NeonButton(
                        text = if (showGhostImage) "HIDE HINT" else "SHOW HINT",
                        onClick = { showGhostImage = !showGhostImage },
                        color = AccentYellow,
                        modifier = Modifier.fillMaxWidth(0.6f).height(50.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Difficulty Buttons
                    Text("DIFFICULTY", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        NeonButton("3x3", onClick = { viewModel.initializeGame(uiState.sourceImage!!, 3) }, 
                            color = if(uiState.gridSize==3) PrimaryAction else Color.LightGray, modifier = Modifier.width(80.dp).height(50.dp))
                        NeonButton("4x4", onClick = { viewModel.initializeGame(uiState.sourceImage!!, 4) }, 
                            color = if(uiState.gridSize==4) PrimaryAction else Color.LightGray, modifier = Modifier.width(80.dp).height(50.dp))
                        NeonButton("5x5", onClick = { viewModel.initializeGame(uiState.sourceImage!!, 5) }, 
                            color = if(uiState.gridSize==5) PrimaryAction else Color.LightGray, modifier = Modifier.width(80.dp).height(50.dp))
                    }
                }
            }
        }
        
        // --- WIN OVERLAY ---
        if (uiState.isSolved) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(enabled = false) {}
                    .zIndex(10f)
            )
            CelebrationOverlay(visible = true)
            NeonCard(modifier = Modifier.align(Alignment.Center).zIndex(20f)) {
                Text("SOLVED!", color = SuccessGreen, fontSize = 40.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(20.dp))
                NeonButton("PLAY AGAIN", onClick = { 
                     viewModel.initializeGame(uiState.sourceImage!!, uiState.gridSize) 
                })
            }
        }
    }
}