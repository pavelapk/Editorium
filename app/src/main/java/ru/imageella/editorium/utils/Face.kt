package ru.imageella.editorium.utils

class Face(
    val vertices: Array<Vector>,
    val normal: Vector,
    val color: Int,
    private val digit: Digit
) {
    val center: Vector
    val picVertices: Array<Vector>

    init {
        val sum = Vector(0f, 0f, 0f)
        for (vertex in vertices) {
            sum += vertex
        }
        center = sum / vertices.size.toFloat()

        val i = (vertices[1] - vertices[0]).cross(normal)
        val j = vertices[1] - vertices[0]
        picVertices = digit.points.map { center + (i * it.x + j * it.y) / 12f }.toTypedArray()
    }
    fun getRotated(rotationMat: Array<Array<Float>>): Face {
        val rotVertices = vertices.copyOf()
        for (i in rotVertices.indices) {
            rotVertices[i] = LinAlg.applyMatrix(rotationMat, rotVertices[i])
        }

        return Face(rotVertices, LinAlg.applyMatrix(rotationMat, normal), color, digit)
    }
}
