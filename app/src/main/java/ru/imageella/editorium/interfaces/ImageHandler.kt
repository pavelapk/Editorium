package ru.imageella.editorium.interfaces

import android.graphics.Bitmap

interface ImageHandler : Viewport {
    fun getBitmap(): Bitmap
    fun onImageClick(x: Float, y: Float)
    fun onImageTouchMove(x: Float, y: Float, isStart: Boolean)
    fun getLastBitmap(): Bitmap
}