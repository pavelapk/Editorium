package ru.imageella.editorium.utils

import ru.imageella.editorium.tools.CubeFragment

class Digit(val value: Int) {
    val points = when (value) {
        0 -> arrayOf(
            CubeFragment.Point(-1f, -2f),
            CubeFragment.Point(1f, -2f),
            CubeFragment.Point(1f, 2f),
            CubeFragment.Point(-1f, 2f)
        )
        1 -> arrayOf(
            CubeFragment.Point(-1f, 0f),
            CubeFragment.Point(1f, -2f),
            CubeFragment.Point(1f, 2f)
        )
        2 -> arrayOf(
            CubeFragment.Point(-1f, -2f),
            CubeFragment.Point(1f, -2f),
            CubeFragment.Point(1f, 0f),
            CubeFragment.Point(-1f, 2f),
            CubeFragment.Point(1f, 2f)
        )
        3 -> arrayOf(
            CubeFragment.Point(-1f, -2f),
            CubeFragment.Point(1f, -2f),
            CubeFragment.Point(-1f, 0f),
            CubeFragment.Point(1f, 0f),
            CubeFragment.Point(-1f, 2f)
        )
        4 -> arrayOf(
            CubeFragment.Point(-1f, -2f),
            CubeFragment.Point(-1f, 0f),
            CubeFragment.Point(1f, 0f),
            CubeFragment.Point(1f, -2f),
            CubeFragment.Point(1f, 2f)
        )
        5 -> arrayOf(
            CubeFragment.Point(1f, -2f),
            CubeFragment.Point(-1f, -2f),
            CubeFragment.Point(-1f, 0f),
            CubeFragment.Point(1f, 0f),
            CubeFragment.Point(1f, 2f),
            CubeFragment.Point(-1f, 2f)
        )
        6 -> arrayOf(
            CubeFragment.Point(1f, -2f),
            CubeFragment.Point(-1f, 0f),
            CubeFragment.Point(1f, 0f),
            CubeFragment.Point(1f, 2f),
            CubeFragment.Point(-1f, 2f),
            CubeFragment.Point(-1f, 0f)
        )
        7 -> arrayOf(
            CubeFragment.Point(-1f, -2f),
            CubeFragment.Point(1f, -2f),
            CubeFragment.Point(-1f, 0f),
            CubeFragment.Point(-1f, 2f)
        )
        8 -> arrayOf(
            CubeFragment.Point(-1f, 0f),
            CubeFragment.Point(-1f, -2f),
            CubeFragment.Point(1f, -2f),
            CubeFragment.Point(1f, 2f),
            CubeFragment.Point(-1f, 2f),
            CubeFragment.Point(-1f, 0f),
            CubeFragment.Point(1f, 0f)
        )
        9 -> arrayOf(
            CubeFragment.Point(1f, 0f),
            CubeFragment.Point(-1f, 0f),
            CubeFragment.Point(-1f, -2f),
            CubeFragment.Point(1f, -2f),
            CubeFragment.Point(1f, 0f),
            CubeFragment.Point(-1f, 2f),
        )
        else -> emptyArray()
    }

}