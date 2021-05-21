package ru.imageella.editorium.tools

import android.graphics.Color
import android.graphics.Path
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.imageella.editorium.R
import ru.imageella.editorium.databinding.FragmentCubeToolBinding
import ru.imageella.editorium.interfaces.Algorithm
import ru.imageella.editorium.interfaces.ImageHandler
import ru.imageella.editorium.utils.Cube
import ru.imageella.editorium.utils.LinAlg.Companion.applyMatrix
import ru.imageella.editorium.utils.LinAlg.Companion.prodMatrix
import ru.imageella.editorium.utils.Vector
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class CubeFragment : Fragment(R.layout.fragment_cube_tool), Algorithm {

    private val binding by viewBinding(FragmentCubeToolBinding::bind, R.id.rootLayout)

    companion object {
        val TAG: String = CubeFragment::class.java.simpleName

        fun newInstance() = CubeFragment()


        private const val cameraDistance = 4f
        private const val rotateSpeed = 2f
    }

    private lateinit var image: ImageHandler

    class Point(var x: Float, var y: Float)


    private fun toRad(deg: Int): Float = (deg * PI / 180).toFloat()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        image = activity as ImageHandler

        drawCube()
        image.refresh()

    }


    private var rotationMat = arrayOf(
        arrayOf(1f, 0f, 0f),
        arrayOf(0f, 1f, 0f),
        arrayOf(0f, 0f, 1f),
    )

    private fun updateRotations(x: Float, y: Float, z: Float) {
        val deltaX = arrayOf(
            arrayOf(1f, 0f, 0f),
            arrayOf(0f, cos(x), -sin(x)),
            arrayOf(0f, sin(x), cos(x)),
        )
        val deltaY = arrayOf(
            arrayOf(cos(y), 0f, sin(y)),
            arrayOf(0f, 1f, 0f),
            arrayOf(-sin(y), 0f, cos(y)),
        )
        val deltaZ = arrayOf(
            arrayOf(cos(z), -sin(z), 0f),
            arrayOf(sin(z), cos(z), 0f),
            arrayOf(0f, 0f, 1f),
        )
        rotationMat = prodMatrix(deltaX, rotationMat)
        rotationMat = prodMatrix(deltaY, rotationMat)
        rotationMat = prodMatrix(deltaZ, rotationMat)
    }


    private fun perspectiveProjection(v: Vector, center: Point, scale: Float): Point {
        val z = 1 / (cameraDistance - v.z)
        val projection = arrayOf(
            arrayOf(z, 0f, 0f),
            arrayOf(0f, z, 0f),
            arrayOf(0f, 0f, 1f),
        )
        val projected = applyMatrix(projection, v)
        return Point(
            projected.x * scale + center.x,
            projected.y * scale + center.y,
        )
    }

    private fun drawCube() {
        image.clearOverlay()
        val overlaySize = image.getOverlaySize()
        val center = Point(overlaySize.first * 0.5f, overlaySize.second * 0.5f)
        val scale = min(overlaySize.first, overlaySize.second) * 1f

        for (face in Cube.faces) {
            val rotatedFace = face.getRotated(rotationMat)
            val dot = (Vector(0f, 0f, cameraDistance) - rotatedFace.center) *
                    applyMatrix(rotationMat, face.normal)

            if (dot > 0) {
                val path = Path()
                var points = rotatedFace.vertices.map { perspectiveProjection(it, center, scale) }
                path.moveTo(points[0].x, points[0].y)
                for (i in 1 until points.size) {
                    path.lineTo(points[i].x, points[i].y)
                }
                image.drawPath(path, true, 5f, face.color)

                path.reset()
                points =
                    rotatedFace.picVertices.map { perspectiveProjection(it, center, scale) }
                path.moveTo(points[0].x, points[0].y)
                for (i in 1 until points.size) {
                    path.lineTo(points[i].x, points[i].y)
                }
                image.drawPath(path, false, 10f, Color.BLACK)

            }
        }

//        for (vertex in Cube.vertices) {
//            val projected = perspectiveProjection(applyMatrix(rotationMat, vertex), center, scale)
//            image.drawPoint(projected.x, projected.y, 20f, Color.DKGRAY)
//        }
    }

    private var lastX = 0f
    private var lastY = 0f

    override fun onImageTouchMove(xRaw: Float, yRaw: Float, x: Float, y: Float, isStart: Boolean) {
        if (!isStart) {
            val deltaX = (y - lastY) * -rotateSpeed
            val deltaY = (x - lastX) * rotateSpeed
            updateRotations(deltaX, deltaY, 0f)
        }
        lastX = x
        lastY = y
        drawCube()
        image.refresh()
    }

    override fun onImageRotationGesture(angle: Float) {
        updateRotations(0f, 0f, angle)
        drawCube()
        image.refresh()
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