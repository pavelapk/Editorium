package ru.imageella.editorium.interfaces

import android.graphics.Bitmap

interface Canvas {
    fun setBitmap(bitmap: Bitmap)
    fun previewRotate(angle: Float)
}