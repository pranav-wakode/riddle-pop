package com.example.neonpuzzle.ui.puzzle

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neonpuzzle.data.AppDatabase
import com.example.neonpuzzle.data.UserScore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class JigsawPiece(
    val id: Int,
    val bitmap: Bitmap,
    val correctIndex: Int,
    val currentOffset: Offset,
    val isSnapped: Boolean = false
)

data class JigsawState(
    val pieces: List<JigsawPiece> = emptyList(),
    val isSolved: Boolean = false,
    val sourceImage: Bitmap? = null,
    val rows: Int = 3,
    val cols: Int = 2
)

class JigsawPuzzleViewModel(private val database: AppDatabase) : ViewModel() {

    private val _uiState = MutableStateFlow(JigsawState())
    val uiState = _uiState.asStateFlow()

    fun initializeGame(image: Bitmap, level: String) {
        val (rows, cols) = when(level) {
            "EASY" -> Pair(3, 2)
            "MEDIUM" -> Pair(4, 3)
            "HARD" -> Pair(5, 4)
            else -> Pair(3, 2)
        }

        val rawPieces = PuzzleUtils.splitBitmap(image, rows, cols)
        val jigsawPieces = rawPieces.mapIndexed { index, bmp ->
            JigsawPiece(
                id = index,
                bitmap = bmp,
                correctIndex = index,
                currentOffset = Offset.Zero, 
                isSnapped = false
            )
        }

        _uiState.update {
            it.copy(
                pieces = jigsawPieces,
                sourceImage = image,
                rows = rows,
                cols = cols,
                isSolved = false
            )
        }
    }

    fun scatterPiecesInTray(trayTop: Float, trayBottom: Float, screenWidth: Float) {
        val currentPieces = _uiState.value.pieces.map { piece ->
            if (piece.isSnapped) piece else piece.copy(
                currentOffset = Offset(
                    x = Random.nextFloat() * (screenWidth - 150f), 
                    y = trayTop + Random.nextFloat() * (trayBottom - trayTop - 150f)
                )
            )
        }
        _uiState.update { it.copy(pieces = currentPieces) }
    }

    fun onPieceDragStart(id: Int) {
        // Bring piece to front (Z-Index logic) by moving to end of list
        val list = _uiState.value.pieces.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index != -1) {
            val piece = list.removeAt(index)
            list.add(piece) // Add to end
            _uiState.update { it.copy(pieces = list) }
        }
    }

    fun onPieceDrag(id: Int, dragAmount: Offset) {
        val list = _uiState.value.pieces.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index != -1 && !list[index].isSnapped) {
            val piece = list[index]
            list[index] = piece.copy(currentOffset = piece.currentOffset + dragAmount)
            _uiState.update { it.copy(pieces = list) }
        }
    }

    fun onPieceRelease(id: Int, targetGridOrigin: Offset, cellWidth: Float, cellHeight: Float) {
        val list = _uiState.value.pieces.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index == -1) return

        val piece = list[index]
        
        val row = piece.correctIndex / _uiState.value.cols
        val col = piece.correctIndex % _uiState.value.cols
        val targetX = targetGridOrigin.x + (col * cellWidth)
        val targetY = targetGridOrigin.y + (row * cellHeight)

        val diffX = kotlin.math.abs(piece.currentOffset.x - targetX)
        val diffY = kotlin.math.abs(piece.currentOffset.y - targetY)

        if (diffX < 60f && diffY < 60f) {
            list[index] = piece.copy(currentOffset = Offset(targetX, targetY), isSnapped = true)
            // Snap piece to "background" layer (start of list) so unsnapped pieces float over it? 
            // Actually, kept at end is fine, usually solved pieces stay flat.
            _uiState.update { it.copy(pieces = list) }
            checkWin()
        }
    }

    private fun checkWin() {
        if (_uiState.value.pieces.all { it.isSnapped }) {
            _uiState.update { it.copy(isSolved = true) }
            saveScore()
        }
    }

    private fun saveScore() {
        viewModelScope.launch {
            database.scoreDao().insertScore(UserScore(gameType = "JIGSAW", score = 500))
        }
    }
}