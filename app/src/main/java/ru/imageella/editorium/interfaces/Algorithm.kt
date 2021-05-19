package ru.imageella.editorium.interfaces

interface Algorithm {

    fun doAlgorithm() {}
    fun onImageClick(x: Float, y: Float) {}
    fun onImageTouchMove(x: Float, y: Float, isStart: Boolean) {}
    fun onImageRotationGesture(angle: Float) {}

}