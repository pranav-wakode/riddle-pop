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
    
    // Layout Tracking
    var gridPosition by remember { mutableStateOf(Offset.Zero) }
    var gridSize by remember { mutableStateOf(IntSize.Zero) }
    var trayPosition by remember { mutableStateOf(Offset.Zero) }
    var traySize by remember { mutableStateOf(IntSize.Zero) }
    var hasScattered by remember { mutableStateOf(false) } // Ensure we only scatter once per game

    // Re-scatter if source image changes (new game)
    LaunchedEffect(uiState.sourceImage) {
        hasScattered = false
    }

    // Trigger scatter when layout is ready
    LaunchedEffect(traySize, uiState.pieces) {
        if (!hasScattered && traySize.height > 0 && uiState.pieces.isNotEmpty()) {
            // Scatter pieces inside the tray area
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
            viewModel.initializeGame(scaled, "EASY") 
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MainBackgroundBrush)
            .statusBarsPadding()
    ) {
        NeonButton("EXIT", onClick = onBack, color = ErrorRed, modifier = Modifier.padding(16.dp))

        if (uiState.sourceImage == null) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("JIGSAW POP", color = PrimaryAction, fontSize = 40.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(32.dp))
                NeonCard {
                    Text("START BUILDING", color = TextSecondary)
                    Spacer(modifier = Modifier.height(16.dp))
                    NeonButton("GALLERY", onClick = { launcher.launch("image/*") }, color = SecondaryAction)
                    Spacer(modifier = Modifier.height(12.dp))
                    NeonButton("DEFAULT", onClick = { 
                         try {
                            val resId = context.resources.getIdentifier("default_puzzle", "drawable", context.packageName)
                            val bitmap = if (resId != 0) BitmapFactory.decodeResource(context.resources, resId) 
                                         else PuzzleUtils.createPlaceholderBitmap(900, 900)
                            val square = PuzzleUtils.getCroppedSquare(bitmap)
                            val scaled = android.graphics.Bitmap.createScaledBitmap(square, 900, 900, true)
                            viewModel.initializeGame(scaled, "EASY")
                        } catch (e: Exception) {
                            val bitmap = PuzzleUtils.createPlaceholderBitmap(900, 900)
                            viewModel.initializeGame(bitmap, "EASY") 
                        }
                    }, color = AccentYellow)
                }
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                // --- BOARD AREA ---
                Box(
                    modifier = Modifier.weight(0.6f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(300.dp)
                            .shadow(10.dp)
                            .background(Color.White)
                            .border(4.dp, PrimaryAction.copy(alpha=0.3f))
                            .onGloballyPositioned { coordinates ->
                                gridPosition = coordinates.positionInRoot()
                                gridSize = coordinates.size
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cellW = size.width / uiState.cols
                            val cellH = size.height / uiState.rows
                            // Draw Grid
                            for (i in 1 until uiState.cols) {
                                drawLine(Color.Gray.copy(alpha=0.3f), start = Offset(i * cellW, 0f), end = Offset(i * cellW, size.height), strokeWidth = 2f)
                            }
                            for (i in 1 until uiState.rows) {
                                drawLine(Color.Gray.copy(alpha=0.3f), start = Offset(0f, i * cellH), end = Offset(size.width, i * cellH), strokeWidth = 2f)
                            }
                        }
                    }
                }

                // --- PIECE TRAY ---
                Box(
                    modifier = Modifier
                        .weight(0.4f)
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

            // PIECES (Global Layer)
            val cellWidth = gridSize.width.toFloat() / uiState.cols
            val cellHeight = gridSize.height.toFloat() / uiState.rows
            
            if (gridSize.width > 0) {
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
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragEnd = {
                                        viewModel.onPieceRelease(piece.id, gridPosition, cellWidth, cellHeight)
                                    }
                                ) { change, dragAmount ->
                                    change.consume()
                                    viewModel.onPieceDrag(piece.id, dragAmount)
                                }
                            }
                    )
                }
            }

            if (uiState.isSolved) {
                 Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(enabled = false) {}
                )
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