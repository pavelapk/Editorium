package ru.imageella.editorium.interfaces

import android.graphics.Bitmap

interface Viewport {
    fun setBitmap(bitmap: Bitmap)
    fun previewRotate(angle: Float)

    fun drawPoint(x: Float, y: Float, width: Float, color: Int)
    fun clearOverlay()
    fun refresh()
}