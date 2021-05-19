package ru.imageella.editorium.utils

class Vector(var x: Float, var y: Float, var z: Float) {
    operator fun plus(b: Vector) = Vector(x + b.x, y + b.y, z + b.z)
    operator fun plusAssign(b: Vector) {
        x += b.x
        y += b.y
        z += b.z
    }

    operator fun minus(b: Vector) = Vector(x - b.x, y - b.y, z - b.z)
    operator fun minusAssign(b: Vector) {
        x -= b.x
        y -= b.y
        z -= b.z
    }

    operator fun times(b: Vector) = x * b.x + y * b.y + z * b.z

    operator fun times(s: Float) = Vector(s * x, s * y, s * z)
    operator fun div(s: Float) = Vector(x / s, y / s, z / s)
    operator fun divAssign(s: Float) {
        x /= s
        y /= s
        z /= s
    }

    fun cross(b: Vector) = Vector(y * b.z - z * b.y, z * b.x - x * b.z, x * b.y - y * b.x)
}

class LinAlg {

    companion object {
        fun applyMatrix(
            matrix: Array<Array<Float>>,
            vertex: Vector
        ): Vector {
            return Vector(
                matrix[0][0] * vertex.x + matrix[0][1] * vertex.y + matrix[0][2] * vertex.z,
                matrix[1][0] * vertex.x + matrix[1][1] * vertex.y + matrix[1][2] * vertex.z,
                matrix[2][0] * vertex.x + matrix[2][1] * vertex.y + matrix[2][2] * vertex.z
            )
        }

        fun prodMatrix(m1: Array<Array<Float>>, m2: Array<Array<Float>>): Array<Array<Float>> {
            val res = Array(m1.size) { Array(m2[0].size) { 0f } }
            for (i in res.indices) {
                for (j in res[i].indices) {
                    for (k in m2.indices)
                        res[i][j] += m1[i][k] * m2[k][j]
                }
            }
            return res
        }
    }
}