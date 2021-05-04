package ru.imageella.editorium.interfaces

import android.graphics.Bitmap

interface Viewport {
    fun setBitmap(bitmap: Bitmap)
    fun previewRotate(angle: Float)
    fun previewScale(ratio: Float)
}