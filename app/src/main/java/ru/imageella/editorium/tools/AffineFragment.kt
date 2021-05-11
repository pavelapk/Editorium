package ru.imageella.editorium.tools

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.imageella.editorium.R
import ru.imageella.editorium.databinding.FragmentAffineToolBinding
import ru.imageella.editorium.interfaces.Algorithm
import ru.imageella.editorium.interfaces.ImageHandler
import kotlin.math.roundToInt
import kotlin.math.sqrt

class AffineFragment : Fragment(R.layout.fragment_affine_tool), Algorithm {

    private val binding by viewBinding(FragmentAffineToolBinding::bind, R.id.rootLayout)

    companion object {
        val TAG: String = AffineFragment::class.java.simpleName

        fun newInstance() = AffineFragment()
    }

    class PixelsWithSizes(
        val pixels: IntArray,
        val w: Int,
        val h: Int
    )

    private lateinit var image: ImageHandler

    enum class State {
        START, END, NONE
    }

    private var state = State.NONE

    private class Point(var x: Float, var y: Float, val color: Int)

    private val startPoints = arrayOf(
        Point(-1f, -1f, Color.RED),
        Point(-1f, -1f, Color.GREEN),
        Point(-1f, -1f, Color.BLUE)
    )
    private val endPoints = arrayOf(
        Point(-1f, -1f, Color.RED),
        Point(-1f, -1f, Color.GREEN),
        Point(-1f, -1f, Color.BLUE)
    )

    private var curStart = 0
    private var curEnd = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        image = activity as ImageHandler

        binding.startPointsToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switchState(State.START)
            } else {
                switchState(State.NONE)
            }
        }

        binding.endPointsToggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switchState(State.END)
            } else {
                switchState(State.NONE)
            }
        }

        binding.transformBtn.setOnClickListener {
            doAlgorithm()
        }

        val matrix = AffineMatrix.calcMatrix(
            arrayOf(
                Point(1f, 0f, Color.RED),
                Point(0f, sqrt(3f), Color.GREEN),
                Point(-1f, 0f, Color.BLUE)
            ), arrayOf(
                Point((3 + 2 * sqrt(3f)) / 2, 2.5f, Color.RED),
                Point(1.5f, 3.5f, Color.GREEN),
                Point(1.5f, 1.5f, Color.BLUE)
            )
        )
        Log.d("DAROVA", "${matrix.m0}, ${matrix.m1}, ${matrix.m2}, ${matrix.m3}")
    }

    private fun switchState(newState: State) {
        state = newState
        image.clearOverlay()
        when (state) {
            State.START -> {
                binding.endPointsToggle.isEnabled = false
                drawPoints(startPoints)
            }
            State.END -> {
                binding.startPointsToggle.isEnabled = false
                drawPoints(endPoints)
            }
            State.NONE -> {
                binding.startPointsToggle.isEnabled = true
                binding.endPointsToggle.isEnabled = true
            }
        }
        binding.transformBtn.isEnabled = state == State.NONE
        image.refresh()
    }

    private fun drawPoints(points: Array<Point>) {
        image.clearOverlay()
        for (point in points) {
            if (point.x >= 0 && point.y >= 0)
                image.drawPoint(point.x, point.y, 25f, point.color)
        }
    }

    override fun onImageClick(x: Float, y: Float) {
        //image.drawPoint(x, y, 20f, Color.GREEN)
        when (state) {
            State.START -> {
                startPoints[curStart].x = x
                startPoints[curStart].y = y
                curStart = (curStart + 1) % 3
                drawPoints(startPoints)
            }
            State.END -> {
                endPoints[curEnd].x = x
                endPoints[curEnd].y = y
                curEnd = (curEnd + 1) % 3
                drawPoints(endPoints)
            }
            else -> return
        }
        image.refresh()

    }

    private class AffineMatrix(var m0: Float, var m1: Float, var m2: Float, var m3: Float) {
        companion object {
            fun calcMatrix(s: Array<Point>, e: Array<Point>): AffineMatrix {
                val det =
                    s[0].x * s[1].y - s[2].y * s[0].x + s[2].x * s[0].y - s[2].x * s[1].y - s[1].x * s[0].y + s[2].y * s[1].x
                return AffineMatrix(
                    (-s[0].y * e[1].x + s[0].y * e[2].x + s[1].y * e[0].x - s[1].y * e[2].x - s[2].y * e[0].x + s[2].y * e[1].x) / det,
                    -(-s[0].y * e[1].y + s[0].y * e[2].y + s[1].y * e[0].y - s[1].y * e[2].y - s[2].y * e[0].y + s[2].y * e[1].y) / det,
                    -(s[0].x * e[1].x - s[0].x * e[2].x + s[2].x * e[0].x - s[2].x * e[1].x - s[1].x * e[0].x + s[1].x * e[2].x) / det,
                    (s[0].x * e[1].y - s[0].x * e[2].y + s[2].x * e[0].y - s[2].x * e[1].y - s[1].x * e[0].y + s[1].x * e[2].y) / det
                )
            }
        }
    }


    override fun doAlgorithm() {
        val width = image.getBitmap().width
        val height = image.getBitmap().height
        val pixels = IntArray(width * height)
        image.getBitmap().getPixels(pixels, 0, width, 0, 0, width, height)

        var curPic = PixelsWithSizes(pixels, width, height)

        curPic = transformPic(curPic)
        image.setBitmap(
            Bitmap.createBitmap(curPic.pixels, curPic.w, curPic.h, image.getBitmap().config)
        )
    }


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
    }

}