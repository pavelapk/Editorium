package ru.imageella.editorium.interfaces

interface Algorithm {

    fun onImageClick(x: Float, y: Float) {}
    fun onImageTouchMove(xRaw:Float, yRaw:Float, x: Float, y: Float, isStart: Boolean) {}
    fun onImageRotationGesture(angle: Float) {}
}