package ru.imageella.editorium.interfaces

import android.graphics.Bitmap

interface ImageHandler {
    fun setBitmap(bitmap: Bitmap)
    fun getBitmap(): Bitmap
    fun previewRotate(angle: Float)
}