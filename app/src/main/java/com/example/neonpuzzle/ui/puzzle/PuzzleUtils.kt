package com.example.neonpuzzle.ui.puzzle

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

object PuzzleUtils {

    /**
     * Splits a bitmap into rows x cols pieces.
     * Ensures the image is square (Center Cropped) before splitting.
     */
    fun splitBitmap(source: Bitmap, rows: Int, cols: Int): List<Bitmap> {
        val pieces = mutableListOf<Bitmap>()
        
        // 1. Center Crop logic: Make the image square
        val dimension = kotlin.math.min(source.width, source.height)
        val xOffset = (source.width - dimension) / 2
        val yOffset = (source.height - dimension) / 2
        
        val squaredBitmap = Bitmap.createBitmap(source, xOffset, yOffset, dimension, dimension)
        
        // 2. Scale to a standard manageable size (e.g., 1000x1000) for performance
        val scaledBitmap = Bitmap.createScaledBitmap(squaredBitmap, 1000, 1000, true)

        val pieceWidth = scaledBitmap.width / cols
        val pieceHeight = scaledBitmap.height / rows

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val x = c * pieceWidth
                val y = r * pieceHeight
                val piece = Bitmap.createBitmap(scaledBitmap, x, y, pieceWidth, pieceHeight)
                pieces.add(piece)
            }
        }
        return pieces
    }
    
    // Helper to get the cropped square bitmap for the "Ghost" preview
    fun getCroppedSquare(source: Bitmap): Bitmap {
        val dimension = kotlin.math.min(source.width, source.height)
        val xOffset = (source.width - dimension) / 2
        val yOffset = (source.height - dimension) / 2
        return Bitmap.createBitmap(source, xOffset, yOffset, dimension, dimension)
    }

    fun createPlaceholderBitmap(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            color = Color.parseColor("#BC13FE") // Neon Purple
            style = Paint.Style.STROKE
            strokeWidth = 10f
        }
        canvas.drawColor(Color.parseColor("#151520")) // Dark fill
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        return bitmap
    }
}