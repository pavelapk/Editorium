package ru.imageella.editorium.interfaces

import android.graphics.Bitmap
import android.view.ViewGroup

interface ImageHandler : Viewport, Algorithm {
    fun getBitmap(): Bitmap
    fun getLastBitmap(): Bitmap
    fun progressIndicator(toolLayout: ViewGroup?, isEnabled: Boolean)
}