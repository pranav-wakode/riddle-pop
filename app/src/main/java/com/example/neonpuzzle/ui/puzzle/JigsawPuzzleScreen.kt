package com.example.neonpuzzle.ui.puzzle

import android.graphics.BitmapFactory
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
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
fun JigsawPuzzleScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    
    val viewModel: JigsawPuzzleViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return JigsawPuzzleViewModel(db) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()
    
    var gridPosition by remember { mutableStateOf(Offset.Zero) }
    var gridSize by remember { mutableStateOf(IntSize.Zero) }
    var trayPosition by remember { mutableStateOf(Offset.Zero) }
    var traySize by remember { mutableStateOf(IntSize.Zero) }
    var hasScattered by remember { mutableStateOf(false) }
    var showHint by remember { mutableStateOf(false) }
    var selectedDifficulty by remember { mutableStateOf("EASY") } // Default

    // Logic: If difficulty changes, and we have an image, RESTART GAME
    LaunchedEffect(selectedDifficulty) {
        if (uiState.sourceImage != null) {
            viewModel.initializeGame(uiState.sourceImage!!, selectedDifficulty)
            hasScattered = false // Force re-scatter
        }
    }

    LaunchedEffect(traySize, uiState.pieces) {
        if (!hasScattered && traySize.height > 0 && uiState.pieces.isNotEmpty()) {
            viewModel.scatterPiecesInTray(
                trayTop = trayPosition.y,
                trayBottom = trayPosition.y + traySize.height,
                screenWidth = traySize.width.toFloat()
            )
            hasScattered = true
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val inputStream: InputStream? = context.contentResolver.openInputStream(it)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val square = PuzzleUtils.getCroppedSquare(bitmap)
            val scaled = android.graphics.Bitmap.createScaledBitmap(square, 900, 900, true)
            viewModel.initializeGame(scaled, selectedDifficulty) 
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MainBackgroundBrush)
            .statusBarsPadding()
    ) {
        // --- CONTENT ---
        Column(Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeonButton("EXIT", onClick = onBack, color = ErrorRed, modifier = Modifier.height(45.dp))
                if (uiState.sourceImage != null) {
                    NeonButton(
                        text = if(showHint) "HIDE HINT" else "HINT",
                        onClick = { showHint = !showHint },
                        color = AccentYellow,
                        modifier = Modifier.height(45.dp)
                    )
                }
            }

            if (uiState.sourceImage == null) {
                // SETUP SCREEN
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("JIGSAW POP", color = PrimaryAction, fontSize = 40.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Difficulty UI
                    Text("DIFFICULTY: $selectedDifficulty", color = TextSecondary, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NeonButton("EASY", onClick = { selectedDifficulty = "EASY" }, 
                            color = if(selectedDifficulty=="EASY") SuccessGreen else Color.LightGray, modifier = Modifier.height(40.dp))
                        NeonButton("MED", onClick = { selectedDifficulty = "MEDIUM" }, 
                            color = if(selectedDifficulty=="MEDIUM") AccentYellow else Color.LightGray, modifier = Modifier.height(40.dp))
                        NeonButton("HARD", onClick = { selectedDifficulty = "HARD" }, 
                            color = if(selectedDifficulty=="HARD") ErrorRed else Color.LightGray, modifier = Modifier.height(40.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    NeonCard {
                        NeonButton("GALLERY", onClick = { launcher.launch("image/*") }, color = SecondaryAction)
                        Spacer(modifier = Modifier.height(12.dp))
                        NeonButton("DEFAULT", onClick = { 
                             try {
                                val resId = context.resources.getIdentifier("default_puzzle", "drawable", context.packageName)
                                val bitmap = if (resId != 0) BitmapFactory.decodeResource(context.resources, resId) 
                                             else PuzzleUtils.createPlaceholderBitmap(900, 900)
                                val square = PuzzleUtils.getCroppedSquare(bitmap)
                                val scaled = android.graphics.Bitmap.createScaledBitmap(square, 900, 900, true)
                                viewModel.initializeGame(scaled, selectedDifficulty)
                            } catch (e: Exception) {
                                val bitmap = PuzzleUtils.createPlaceholderBitmap(900, 900)
                                viewModel.initializeGame(bitmap, selectedDifficulty) 
                            }
                        }, color = AccentYellow)
                    }
                }
            } else {
                // GAME SCREEN
                Column(Modifier.fillMaxSize()) {
                    // Board
                    Box(
                        modifier = Modifier.weight(0.65f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(320.dp)
                                .shadow(10.dp)
                                .background(Color.White)
                                .border(4.dp, PrimaryAction.copy(alpha=0.3f))
                                .onGloballyPositioned { coordinates ->
                                    gridPosition = coordinates.positionInRoot()
                                    gridSize = coordinates.size
                                }
                        ) {
                            if (showHint && uiState.sourceImage != null) {
                                Image(
                                    bitmap = uiState.sourceImage!!.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().alpha(0.3f),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val cellW = size.width / uiState.cols
                                val cellH = size.height / uiState.rows
                                for (i in 1 until uiState.cols) {
                                    drawLine(Color.Gray.copy(alpha=0.3f), start = Offset(i * cellW, 0f), end = Offset(i * cellW, size.height), strokeWidth = 2f)
                                }
                                for (i in 1 until uiState.rows) {
                                    drawLine(Color.Gray.copy(alpha=0.3f), start = Offset(0f, i * cellH), end = Offset(size.width, i * cellH), strokeWidth = 2f)
                                }
                            }
                        }
                    }

                    // Tray
                    Box(
                        modifier = Modifier
                            .weight(0.35f)
                            .fillMaxWidth()
                            .shadow(elevation = 20.dp, shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                            .background(Color(0xFFF0F4C3))
                            .onGloballyPositioned { coordinates ->
                                trayPosition = coordinates.positionInRoot()
                                traySize = coordinates.size
                            }
                    ) {
                        Text("PIECE TRAY", color = TextSecondary, modifier = Modifier.padding(24.dp).align(Alignment.TopCenter), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        // --- PIECES LAYER (Global) ---
        // This is OUTSIDE the column to ensure it floats above everything
        val cellWidth = gridSize.width.toFloat() / uiState.cols
        val cellHeight = gridSize.height.toFloat() / uiState.rows
        
        if (uiState.sourceImage != null && gridSize.width > 0) {
            uiState.pieces.forEach { piece ->
                val density = context.resources.displayMetrics.density
                val widthDp = (cellWidth / density).dp
                val heightDp = (cellHeight / density).dp

                Image(
                    bitmap = piece.bitmap.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .offset { 
                            androidx.compose.ui.unit.IntOffset(
                                piece.currentOffset.x.toInt(),
                                piece.currentOffset.y.toInt()
                            ) 
                        }
                        .size(widthDp, heightDp)
                        .shadow(if(piece.isSnapped) 0.dp else 4.dp)
                        .border(1.dp, if(piece.isSnapped) SuccessGreen else Color.White)
                        .zIndex(if(piece.isSnapped) 0f else 10f) // Unsnapped pieces float higher
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { viewModel.onPieceDragStart(piece.id) },
                                onDragEnd = { viewModel.onPieceRelease(piece.id, gridPosition, cellWidth, cellHeight) }
                            ) { change, dragAmount ->
                                change.consume()
                                viewModel.onPieceDrag(piece.id, dragAmount)
                            }
                        }
                )
            }
        }

        // --- CELEBRATION OVERLAY (Max Z-Index) ---
        if (uiState.isSolved) {
             Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = false) {}
                    .zIndex(100f) // Top layer
            ) {
                CelebrationOverlay(visible = true)
                NeonCard(modifier = Modifier.align(Alignment.Center)) {
                    Text("MASTER BUILDER!", color = SuccessGreen, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    NeonButton("BACK", onClick = onBack)
                }
            }
        }
    }
}