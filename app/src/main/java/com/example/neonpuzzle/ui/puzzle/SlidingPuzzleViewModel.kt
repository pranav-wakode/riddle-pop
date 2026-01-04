package com.example.neonpuzzle.ui.puzzle

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neonpuzzle.data.AppDatabase
import com.example.neonpuzzle.data.UserScore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

data class SlidingPuzzleState(
    val gridSize: Int = 3, // 3x3 default
    val tiles: List<Int> = emptyList(), // Indices of the image chunks
    val emptySlotIndex: Int = 8,
    val moves: Int = 0,
    val isSolved: Boolean = false,
    val imageChunks: List<Bitmap> = emptyList(),
    val sourceImage: Bitmap? = null
)

class SlidingPuzzleViewModel(private val database: AppDatabase) : ViewModel() {

    private val _uiState = MutableStateFlow(SlidingPuzzleState())
    val uiState = _uiState.asStateFlow()

    fun initializeGame(image: Bitmap, gridSize: Int) {
        val chunks = PuzzleUtils.splitBitmap(image, gridSize, gridSize)
        // Standard solved state: 0, 1, 2, ... last-1 (last is empty)
        val solvedOrder = List(gridSize * gridSize) { it }
        
        _uiState.update {
            it.copy(
                gridSize = gridSize,
                imageChunks = chunks,
                sourceImage = image,
                tiles = solvedOrder, // Start solved, then shuffle
                emptySlotIndex = solvedOrder.last(),
                moves = 0,
                isSolved = false
            )
        }
        shuffleTiles()
    }

    private fun shuffleTiles() {
        // Simple shuffle: make random valid moves from solved state
        // This ensures the puzzle is always solvable
        var currentState = _uiState.value.tiles.toMutableList()
        var emptyIdx = _uiState.value.emptySlotIndex
        val size = _uiState.value.gridSize
        val movesToShuffle = 100

        for (i in 0 until movesToShuffle) {
            val neighbors = getNeighbors(emptyIdx, size)
            val randomNeighbor = neighbors.random()
            
            // Swap
            val temp = currentState[emptyIdx]
            currentState[emptyIdx] = currentState[randomNeighbor]
            currentState[randomNeighbor] = temp
            emptyIdx = randomNeighbor
        }

        _uiState.update {
            it.copy(tiles = currentState, emptySlotIndex = emptyIdx, isSolved = false)
        }
    }

    fun onTileClick(index: Int) {
        if (_uiState.value.isSolved) return

        val state = _uiState.value
        val emptyIdx = state.emptySlotIndex
        
        if (isAdjacent(index, emptyIdx, state.gridSize)) {
            // Swap logic
            val newTiles = state.tiles.toMutableList()
            newTiles[emptyIdx] = newTiles[index]
            newTiles[index] = state.gridSize * state.gridSize - 1 // The empty tile ID
            
            _uiState.update {
                it.copy(
                    tiles = newTiles,
                    emptySlotIndex = index,
                    moves = it.moves + 1
                )
            }
            checkWinCondition()
        }
    }

    private fun checkWinCondition() {
        val currentTiles = _uiState.value.tiles
        // Check if tiles are in order: 0, 1, 2...
        // Note: The empty tile (last ID) should be at the last position
        val isSorted = currentTiles.mapIndexed { index, value -> index == value }.all { it }
        
        if (isSorted) {
            _uiState.update { it.copy(isSolved = true) }
            saveScore()
        }
    }

    private fun saveScore() {
        val points = when(_uiState.value.gridSize) {
            3 -> 100
            4 -> 200
            5 -> 300
            else -> 50
        }
        viewModelScope.launch {
            database.scoreDao().insertScore(
                UserScore(gameType = "SLIDING", score = points)
            )
        }
    }

    private fun getNeighbors(index: Int, size: Int): List<Int> {
        val neighbors = mutableListOf<Int>()
        val row = index / size
        val col = index % size

        if (row > 0) neighbors.add(index - size) // Top
        if (row < size - 1) neighbors.add(index + size) // Bottom
        if (col > 0) neighbors.add(index - 1) // Left
        if (col < size - 1) neighbors.add(index + 1) // Right
        return neighbors
    }

    private fun isAdjacent(idx1: Int, idx2: Int, size: Int): Boolean {
        val r1 = idx1 / size
        val c1 = idx1 % size
        val r2 = idx2 / size
        val c2 = idx2 % size
        return (kotlin.math.abs(r1 - r2) + kotlin.math.abs(c1 - c2)) == 1
    }
}