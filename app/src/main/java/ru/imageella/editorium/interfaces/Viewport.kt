package ru.imageella.editorium.interfaces

import android.graphics.Bitmap

interface Viewport {
    fun setBitmap(bitmap: Bitmap)
    fun previewRotate(angle: Float)

    fun getOverlaySize(): Pair<Int, Int>
    fun drawPoint(x: Float, y: Float, width: Float, color: Int)
    fun drawLine(x1: Float, y1: Float, x2: Float, y2: Float, width: Float, color: Int)
    fun clearOverlay()
    fun refresh()
}