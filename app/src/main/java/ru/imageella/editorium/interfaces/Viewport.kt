package ru.imageella.editorium.interfaces

import android.graphics.Bitmap
import android.graphics.Path

interface Viewport {
    fun setBitmap(bitmap: Bitmap)
    fun previewRotate(angle: Float)
    fun previewScale(ratio: Float)

    fun getOverlaySize(): Pair<Int, Int>
    fun drawPoint(x: Float, y: Float, width: Float, color: Int)
    fun drawLine(x1: Float, y1: Float, x2: Float, y2: Float, width: Float, color: Int)
    fun drawPath(path: Path, isFill: Boolean, width: Float, color: Int)
    fun drawRect(l: Float, t: Float, r: Float, b: Float, width: Float, color: Int)
    fun clearOverlay()
    fun refresh()
    fun drawCanvasToImage()
}