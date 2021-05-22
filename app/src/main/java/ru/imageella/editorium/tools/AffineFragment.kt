package ru.imageella.editorium.tools

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.imageella.editorium.Filtering
import ru.imageella.editorium.R
import ru.imageella.editorium.databinding.FragmentAffineToolBinding
import ru.imageella.editorium.interfaces.Algorithm
import ru.imageella.editorium.interfaces.ImageHandler
import ru.imageella.editorium.utils.PixelsWithSizes
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt

class AffineFragment : Fragment(R.layout.fragment_affine_tool), Algorithm {

    private val binding by viewBinding(FragmentAffineToolBinding::bind, R.id.rootLayout)

    companion object {
        val TAG: String = AffineFragment::class.java.simpleName

        fun newInstance() = AffineFragment()
    }

    private var image: ImageHandler? = null

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
        Point(-1f, -1f, Color.MAGENTA),
        Point(-1f, -1f, Color.YELLOW),
        Point(-1f, -1f, Color.CYAN)
    )

    private var curStart = 0
    private var curEnd = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        image = activity as? ImageHandler

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
            viewLifecycleOwner.lifecycleScope.launch {
                image?.progressIndicator(binding.root, true)
                doAlgorithm()
                image?.progressIndicator(binding.root, false)
            }
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
        when (state) {
            State.START -> {
                binding.endPointsToggle.isEnabled = false
                drawAllPoints()
            }
            State.END -> {
                binding.startPointsToggle.isEnabled = false
                drawAllPoints()
            }
            State.NONE -> {
                binding.startPointsToggle.isEnabled = true
                binding.endPointsToggle.isEnabled = true
            }
        }
        binding.transformBtn.isEnabled = state == State.NONE
        image?.refresh()
    }

    private fun drawAllPoints() {
        image?.clearOverlay()
        for (i in 0..2) {
            if (startPoints[i].x >= 0 && startPoints[i].y >= 0 && endPoints[i].x >= 0 && endPoints[i].y >= 0) {
                image?.drawLine(
                    startPoints[i].x,
                    startPoints[i].y,
                    endPoints[i].x,
                    endPoints[i].y,
                    10f,
                    Color.GRAY
                )
            }
        }
        for (point in startPoints) {
            if (point.x >= 0 && point.y >= 0)
                image?.drawPoint(point.x, point.y, 25f, point.color)
        }
        for (point in endPoints) {
            if (point.x >= 0 && point.y >= 0)
                image?.drawPoint(point.x, point.y, 25f, point.color)
        }
    }

    override fun onImageClick(x: Float, y: Float) {
        when (state) {
            State.START -> {
                startPoints[curStart].x = x
                startPoints[curStart].y = y
                curStart = (curStart + 1) % 3
                drawAllPoints()
            }
            State.END -> {
                endPoints[curEnd].x = x
                endPoints[curEnd].y = y
                curEnd = (curEnd + 1) % 3
                drawAllPoints()
            }
            else -> return
        }
        image?.refresh()
    }

    private class AffineMatrix(var m0: Float, var m1: Float, var m2: Float, var m3: Float) {
        companion object {
            fun calcMatrix(s: Array<Point>, e: Array<Point>): AffineMatrix {
                val det =
                    s[0].x * s[1].y - s[2].y * s[0].x + s[2].x * s[0].y - s[2].x * s[1].y - s[1].x * s[0].y + s[2].y * s[1].x
                return AffineMatrix(
                    (-s[0].y * e[1].x + s[0].y * e[2].x + s[1].y * e[0].x - s[1].y * e[2].x - s[2].y * e[0].x + s[2].y * e[1].x) / det,
                    (-s[0].y * e[1].y + s[0].y * e[2].y + s[1].y * e[0].y - s[1].y * e[2].y - s[2].y * e[0].y + s[2].y * e[1].y) / det,
                    (s[0].x * e[1].x - s[0].x * e[2].x + s[2].x * e[0].x - s[2].x * e[1].x - s[1].x * e[0].x + s[1].x * e[2].x) / det,
                    (s[0].x * e[1].y - s[0].x * e[2].y + s[2].x * e[0].y - s[2].x * e[1].y - s[1].x * e[0].y + s[1].x * e[2].y) / det
                )
            }
        }
    }

    private fun checkPointsExistence() =
        startPoints.all { it.x >= 0 && it.y >= 0 } && endPoints.all { it.x >= 0 && it.y >= 0 }

    private fun resetAllPoint() {
        for (i in 0..2) {
            startPoints[i].x = -1f
            startPoints[i].y = -1f
            endPoints[i].x = -1f
            endPoints[i].y = -1f
        }
    }

    private suspend fun doAlgorithm() {
        if (!checkPointsExistence()) {
            Toast.makeText(context, getString(R.string.placeAllPoint), Toast.LENGTH_SHORT).show()
            return
        }
        val bmp = image?.getBitmap() ?: return
        val width = bmp.width
        val height = bmp.height
        val pixels = IntArray(width * height)
        bmp.getPixels(pixels, 0, width, 0, 0, width, height)

        val curPic = PixelsWithSizes(pixels, width, height)

        val newPic = transformPic(curPic)
        if (newPic == null) {
            Toast.makeText(context, "Слишком большое разрешение", Toast.LENGTH_SHORT).show()
            return
        } else {
            image?.setBitmap(
                Bitmap.createBitmap(newPic.pixels, newPic.w, newPic.h, bmp.config)
            )
        }
        resetAllPoint()
    }

    private fun triangleArea(a: Point, b: Point, c: Point): Float {
        return abs((a.x - c.x) * (b.y - c.y) - (b.x - c.x) * (a.y - c.y)) / 2
    }

    private fun applyMatrix(xy: Pair<Int, Int>, matrix: AffineMatrix): Pair<Float, Float> {
        val x = xy.first
        val y = xy.second
        val nx = x * matrix.m0 + y * matrix.m2
        val ny = x * matrix.m1 + y * matrix.m3
        return Pair(nx, ny)
    }

    private fun calcNewSizes(w: Int, h: Int): Array<Int> {
        val matrix = AffineMatrix.calcMatrix(startPoints, endPoints)
        Log.d("DAROVA", "1 ${matrix.m0}, ${matrix.m1}, ${matrix.m2}, ${matrix.m3}")
        val p0 = applyMatrix(Pair(0, 0), matrix)
        val p1 = applyMatrix(Pair(w - 1, 0), matrix)
        val p2 = applyMatrix(Pair(w - 1, h - 1), matrix)
        val p3 = applyMatrix(Pair(0, h - 1), matrix)

        val l = minOf(p0.first, p1.first, p2.first, p3.first).roundToInt()
        val r = maxOf(p0.first, p1.first, p2.first, p3.first).roundToInt()
        val t = minOf(p0.second, p1.second, p2.second, p3.second).roundToInt()
        val b = maxOf(p0.second, p1.second, p2.second, p3.second).roundToInt()

        val nw = r - l + 1
        val nh = b - t + 1
        return arrayOf(nw, nh, l, t)
    }

    private suspend fun transformPic(pic: PixelsWithSizes): PixelsWithSizes? =
        withContext(Dispatchers.Default) {
            val w = pic.w
            val h = pic.h
            val (nw, nh, l, t) = calcNewSizes(w, h)
            if (max(nw, nh) > 8000) {
                return@withContext null
            }
            val matrix = AffineMatrix.calcMatrix(endPoints, startPoints)
            val oldArea = triangleArea(startPoints[0], startPoints[1], startPoints[2])
            val newArea = triangleArea(endPoints[0], endPoints[1], endPoints[2])

            val newPic = IntArray(nw * nh)

            if (newArea / oldArea >= 1f) {
                for (nx in 0 until nw) {
                    for (ny in 0 until nh) {
                        val ni = ny * nw + nx
                        val (x, y) = applyMatrix(Pair(nx + l, ny + t), matrix)
                        if (0 <= x && x < w && 0 <= y && y < h) {
                            newPic[ni] = Filtering.doBilinearFilteredPixelColor(
                                pic,
                                x,
                                y
                            )
                        }
                    }
                }
            } else {
                val k = oldArea / newArea
                var m = 1
                while (m <= k) {
                    m *= 2
                }
                m /= 2

                var mPic = pic
                var curM = 1
                while (curM < m) {
                    mPic = Filtering.halfSize(mPic)
                    curM *= 2
                }
                val m2Pic = Filtering.halfSize(mPic)

                for (nx in 0 until nw) {
                    for (ny in 0 until nh) {
                        val ni = ny * nw + nx
                        val (x, y) = applyMatrix(Pair(nx + l, ny + t), matrix)
                        if (0 <= x && x < w && 0 <= y && y < h) {
                            newPic[ni] = Filtering.doTrilinearFilteredPixelColor(
                                mPic,
                                m2Pic,
                                m,
                                k,
                                x,
                                y
                            )
                        }
                    }
                }
            }

            return@withContext PixelsWithSizes(newPic, nw, nh)
        }

}