package ru.imageella.editorium.utils

import android.graphics.Color
import ru.imageella.editorium.tools.CubeFragment

class Cube {
    companion object {

        private val vertices = arrayOf(
            Vector(-1f, -1f, -1f), // 0
            Vector(1f, -1f, -1f),
            Vector(1f, 1f, -1f),
            Vector(-1f, 1f, -1f),
            Vector(-1f, -1f, 1f), // 4
            Vector(1f, -1f, 1f),
            Vector(1f, 1f, 1f),
            Vector(-1f, 1f, 1f),
        )

        val faces = listOf(
            Face(
                arrayOf(vertices[0], vertices[1], vertices[2], vertices[3]),
                Vector(0f, 0f, -1f),
                Color.BLUE,
                Digit(6)
            ),
            Face(
                arrayOf(vertices[4], vertices[5], vertices[6], vertices[7]),
                Vector(0f, 0f, 1f),
                Color.LTGRAY,
                Digit(1)
            ),
            Face(
                arrayOf(vertices[0], vertices[1], vertices[5], vertices[4]),
                Vector(0f, -1f, 0f),
                Color.YELLOW,
                Digit(2)
            ),
            Face(
                arrayOf(vertices[2], vertices[3], vertices[7], vertices[6]),
                Vector(0f, 1f, 0f),
                Color.GREEN,
                Digit(3)
            ),
            Face(
                arrayOf(vertices[0], vertices[3], vertices[7], vertices[4]),
                Vector(-1f, 0f, 0f),
                Color.MAGENTA,
                Digit(4)
            ),
            Face(
                arrayOf(vertices[1], vertices[2], vertices[6], vertices[5]),
                Vector(1f, 0f, 0f),
                Color.RED,
                Digit(5)
            ),
        )
    }
}