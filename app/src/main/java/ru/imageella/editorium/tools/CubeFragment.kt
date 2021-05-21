package ru.imageella.editorium.tools

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.imageella.editorium.R
import ru.imageella.editorium.databinding.FragmentCubeToolBinding
import ru.imageella.editorium.interfaces.Algorithm
import ru.imageella.editorium.interfaces.ImageHandler
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class CubeFragment : Fragment(R.layout.fragment_cube_tool), Algorithm {

    private val binding by viewBinding(FragmentCubeToolBinding::bind, R.id.rootLayout)

    companion object {
        val TAG: String = CubeFragment::class.java.simpleName

        fun newInstance() = CubeFragment()
    }

    private lateinit var image: ImageHandler
    private var angleX = 0f
    private var angleY = 0f
    private var angleZ = 0f

    private class Point(var x: Float, var y: Float)
    private class Vertex(var x: Float, var y: Float, var z: Float)

    private class Cube {
        companion object {
            val points = arrayOf(
                Vertex(-1f, -1f, -1f),
                Vertex(1f, -1f, -1f),
                Vertex(1f, 1f, -1f),
                Vertex(-1f, 1f, -1f),
                Vertex(-1f, -1f, 1f),
                Vertex(1f, -1f, 1f),
                Vertex(1f, 1f, 1f),
                Vertex(-1f, 1f, 1f),
                Vertex(-0.3f, -0.6f, 1f),
                Vertex(0f, 0.6f, 1f),
                Vertex(0.3f, -0.6f, 1f),
            )

            fun getLines(): MutableList<Pair<Int, Int>> {
                val lines = mutableListOf(8 to 9, 9 to 10)
                for (i in 0..3) {
                    lines.add(i to (i + 1) % 4)
                    lines.add(i + 4 to (i + 1) % 4 + 4)
                    lines.add(i to i + 4)
                }
                return lines
            }
        }
    }

    private fun toRad(deg: Int): Float = (deg * PI / 180).toFloat()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        image = activity as ImageHandler

        drawCube(Cube.points)
        image.refresh()

        binding.angleZSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                angleZ = toRad(progress)
                drawCube(Cube.points)
                image.refresh()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun applyMatrix(matrix: Array<Array<Float>>, vertex: Vertex): Vertex {
        return Vertex(
            matrix[0][0] * vertex.x + matrix[0][1] * vertex.y + matrix[0][2] * vertex.z,
            matrix[1][0] * vertex.x + matrix[1][1] * vertex.y + matrix[1][2] * vertex.z,
            matrix[2][0] * vertex.x + matrix[2][1] * vertex.y + matrix[2][2] * vertex.z
        )
    }


    private fun drawCube(vertices: Array<Vertex>) {
        image.clearOverlay()
        val overlaySize = image.getOverlaySize()
        val centerX = overlaySize.first * 0.5f
        val centerY = overlaySize.second * 0.5f
        val scale = min(overlaySize.first, overlaySize.second) * 1f
        val rotationX = arrayOf(
            arrayOf(1f, 0f, 0f),
            arrayOf(0f, cos(angleX), -sin(angleX)),
            arrayOf(0f, sin(angleX), cos(angleX)),
        )
        val rotationY = arrayOf(
            arrayOf(cos(angleY), 0f, sin(angleY)),
            arrayOf(0f, 1f, 0f),
            arrayOf(-sin(angleY), 0f, cos(angleY)),
        )
        val rotationZ = arrayOf(
            arrayOf(cos(angleZ), -sin(angleZ), 0f),
            arrayOf(sin(angleZ), cos(angleZ), 0f),
            arrayOf(0f, 0f, 1f),
        )

        val points = vertices.map {
            var projected = applyMatrix(rotationX, it)
            projected = applyMatrix(rotationY, projected)
            projected = applyMatrix(rotationZ, projected)

            val z = 1 / (4 - projected.z)
            val projection = arrayOf(
                arrayOf(z, 0f, 0f),
                arrayOf(0f, z, 0f),
                arrayOf(0f, 0f, 0f),
            )
            projected = applyMatrix(projection, projected)
            Point(
                projected.x * scale + centerX,
                projected.y * scale + centerY,
            )
        }
        for (line in Cube.getLines()) {
            image.drawLine(
                points[line.first].x,
                points[line.first].y,
                points[line.second].x,
                points[line.second].y,
                15f,
                Color.RED
            )
        }
    }


    private var startX = 0f
    private var startY = 0f
    private var startAngleX = 0f
    private var startAngleY = 0f

    override fun onImageTouchMove(xRaw: Float, yRaw: Float, x: Float, y: Float, isStart: Boolean) {
        if (isStart) {
            startX = x
            startY = y
            startAngleX = angleX
            startAngleY = angleY
        } else {
            angleX = startAngleX + (y - startY) * -2
            angleY = startAngleY + (x - startX) * 2
        }
        drawCube(Cube.points)
        image.refresh()
    }


    override fun doAlgorithm() {

    }

/*
    private fun roundCoord(xy: Pair<Float, Float>): Pair<Int, Int> =
        Pair(xy.first.roundToInt(), xy.second.roundToInt())


    private fun applyMatrix(xy: Pair<Int, Int>, matrix: AffineMatrix): Pair<Int, Int> {
        val x = xy.first
        val y = xy.second
        val nx = x * matrix.m0 + y * matrix.m1
        val ny = x * matrix.m2 + y * matrix.m3
        return roundCoord(Pair(nx, ny))
    }

    private fun calcNewSizes(w: Int, h: Int): Array<Int> {
        val matrix = AffineMatrix.calcMatrix(startPoints, endPoints)
        Log.d("DAROVA", "1 ${matrix.m0}, ${matrix.m1}, ${matrix.m2}, ${matrix.m3}")
        val p0 = applyMatrix(Pair(0, 0), matrix)
        val p1 = applyMatrix(Pair(w - 1, 0), matrix)
        val p2 = applyMatrix(Pair(w - 1, h - 1), matrix)
        val p3 = applyMatrix(Pair(0, h - 1), matrix)

        val l = minOf(p0.first, p1.first, p2.first, p3.first)
        val r = maxOf(p0.first, p1.first, p2.first, p3.first)
        val t = minOf(p0.second, p1.second, p2.second, p3.second)
        val b = maxOf(p0.second, p1.second, p2.second, p3.second)

        val nw = r - l + 1
        val nh = b - t + 1
        return arrayOf(nw, nh, l, t)
    }

    private fun transformPic(pic: PixelsWithSizes): PixelsWithSizes {
        val w = pic.w
        val h = pic.h
        val (nw, nh, l, t) = calcNewSizes(w, h)
        val matrix = AffineMatrix.calcMatrix(endPoints, startPoints)
        Log.d("DAROVA", "2 ${matrix.m0}, ${matrix.m1}, ${matrix.m2}, ${matrix.m3}")
        val newPic = IntArray(nw * nh)
        for (nx in 0 until nw) {
            for (ny in 0 until nh) {
                val (x, y) = applyMatrix(Pair(nx + l, ny + t), matrix)

                if (x in 0 until w && y in 0 until h) {
                    val i = y * w + x
                    val ni = ny * nw + nx
                    newPic[ni] = pic.pixels[i]
                }
            }
        }

        return PixelsWithSizes(newPic, nw, nh)
    }*/

}