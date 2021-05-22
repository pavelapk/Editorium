package ru.imageella.editorium.tools

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.imageella.editorium.R
import ru.imageella.editorium.databinding.FragmentSplineToolBinding
import ru.imageella.editorium.interfaces.Algorithm
import ru.imageella.editorium.interfaces.ImageHandler
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round

class SplineFragment : Fragment(R.layout.fragment_spline_tool), Algorithm {

    private val binding by viewBinding(FragmentSplineToolBinding::bind, R.id.rootLayout)

    companion object {
        val TAG: String = SplineFragment::class.java.simpleName

        fun newInstance() = SplineFragment()
    }

    private class Point(
        var x: Float,
        var y: Float,
        var ratio: Float,
        var ratioElongationLeft: Float,
        var ratioElongationRight: Float
    )

    private class Line(var point1: Point, var point2: Point)

    private var image: ImageHandler? = null

    private var currentRatio = 0.5f
    private var currentRatioLeft = 1f
    private var currentRatioRight = 1f
    private var indexPoint = -1
    private var state = 1
    private var checkEditBtn = false
    private var allPoints = mutableListOf<Point>()
    private var allIntermediateLines = mutableListOf<Line>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        image = activity as? ImageHandler

        binding.addBtn.setOnClickListener {
            state = 1
            if (checkEditBtn) {
                checkEditBtn = !checkEditBtn
                binding.editGroup.visibility = View.INVISIBLE
            }
        }
        binding.deleteBtn.setOnClickListener {
            state = 0
            if (checkEditBtn) {
                checkEditBtn = !checkEditBtn
                binding.editGroup.visibility = View.INVISIBLE
            }
        }
        binding.editBtn.setOnClickListener {
            checkEditBtn = !checkEditBtn
            if (checkEditBtn) {
                binding.editGroup.visibility = View.VISIBLE
            } else {
                binding.editGroup.visibility = View.INVISIBLE
                indexPoint = -1
                state = 1
            }
        }
        binding.movingBtn.setOnClickListener {
            val seekBarChangeListener: SeekBar.OnSeekBarChangeListener = object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    currentRatio = (progress.toFloat()) / 10
                    allPoints[indexPoint].ratio = currentRatio
                    drawPoints(allPoints)
                    image?.drawLine(
                        allIntermediateLines[indexPoint - 1].point1.x,
                        allIntermediateLines[indexPoint - 1].point1.y,
                        allIntermediateLines[indexPoint - 1].point2.x,
                        allIntermediateLines[indexPoint - 1].point2.y,
                        5f,
                        Color.YELLOW
                    )
                    doAlgorithm()
                    image?.refresh()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {
                }
            }
            val leftSeekBarChangeListener: SeekBar.OnSeekBarChangeListener = object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    currentRatioLeft = (progress.toFloat()) / 25
                    allPoints[indexPoint].ratioElongationLeft = currentRatioLeft
                    drawPoints(allPoints)
                    image?.drawLine(
                        allIntermediateLines[indexPoint - 1].point1.x,
                        allIntermediateLines[indexPoint - 1].point1.y,
                        allIntermediateLines[indexPoint - 1].point2.x,
                        allIntermediateLines[indexPoint - 1].point2.y,
                        5f,
                        Color.YELLOW
                    )
                    doAlgorithm()
                    image?.refresh()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {
                }
            }
            val rightSeekBarChangeListener: SeekBar.OnSeekBarChangeListener = object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    currentRatioRight = (progress.toFloat()) / 25
                    allPoints[indexPoint].ratioElongationRight = currentRatioRight
                    drawPoints(allPoints)
                    image?.drawLine(
                        allIntermediateLines[indexPoint - 1].point1.x,
                        allIntermediateLines[indexPoint - 1].point1.y,
                        allIntermediateLines[indexPoint - 1].point2.x,
                        allIntermediateLines[indexPoint - 1].point2.y,
                        5f,
                        Color.YELLOW
                    )
                    doAlgorithm()
                    image?.refresh()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {
                }
            }
            if (checkEditBtn) {
                state = 2
                binding.rotateSeekBar.setOnSeekBarChangeListener(seekBarChangeListener)
                binding.elongationLeftSeekBar.setOnSeekBarChangeListener(leftSeekBarChangeListener)
                binding.elongationRightSeekBar.setOnSeekBarChangeListener(rightSeekBarChangeListener)
            }
        }
        binding.clearBtn.setOnClickListener {
            allPoints.clear()
            allIntermediateLines.clear()
            state = 1
            indexPoint = -1
            image?.clearOverlay()
            image?.refresh()
            if (checkEditBtn) {
                checkEditBtn = !checkEditBtn
                binding.editGroup.visibility = View.INVISIBLE
            }
        }
        binding.applyBtn.setOnClickListener {
            image?.clearOverlay()
            doAlgorithm()
            image?.drawCanvasToImage()
            allPoints.clear()
            allIntermediateLines.clear()
            state = 1
            if (checkEditBtn) {
                checkEditBtn = !checkEditBtn
                binding.editGroup.visibility = View.INVISIBLE
            }
        }
    }

    override fun onImageTouchMove(xRaw: Float, yRaw: Float, x: Float, y: Float, isStart: Boolean) {
        if (state == 2) {
            movingBtn(xRaw.toInt(), yRaw.toInt())
        }
    }

    private fun movingBtn(x: Int, y: Int) {
        if (indexPoint != -1) {
            allPoints[indexPoint].x = x.toFloat()
            allPoints[indexPoint].y = y.toFloat()
            drawPoints(allPoints)
            doAlgorithm()
            if (indexPoint != 0 && indexPoint != allPoints.size - 1) {
                image?.drawLine(
                    allIntermediateLines[indexPoint - 1].point1.x,
                    allIntermediateLines[indexPoint - 1].point1.y,
                    allIntermediateLines[indexPoint - 1].point2.x,
                    allIntermediateLines[indexPoint - 1].point2.y,
                    5f,
                    Color.YELLOW
                )
                binding.rotateSeekBar.progress = 50
                binding.elongationLeftSeekBar.progress = 50
                binding.elongationRightSeekBar.progress = 50
            }
            image?.refresh()
        }

    }

    private fun drawPoints(points: MutableList<Point>) {
        image?.clearOverlay()
        var lastPoint: Point? = null
        for (point in points) {
            if (point.x >= 0 && point.y >= 0) {
                image?.drawPoint(point.x, point.y, 25f, Color.RED)
            }

            if (points.size > 1 && lastPoint != null) {
                image?.drawLine(lastPoint.x, lastPoint.y, point.x, point.y, 5f, Color.GREEN)
            }
            lastPoint = point
        }
    }

    override fun onImageClick(x: Float, y: Float) {
        when (state) {
            0 -> {
                if (allPoints.size != 0) {
                    for (point in allPoints) {
                        if (abs(point.x - x) < 25 && abs(point.y - y) < 25) {
                            allPoints.remove(point)
                            drawPoints(allPoints)
                            break
                        }
                    }
                }
            }
            1 -> {
                val currentPoint = Point(x, y, 0.5f, 1f, 1f)
                allPoints.add(currentPoint)
                drawPoints(allPoints)
            }
            2 -> {
                for (point in allPoints) {
                    if (abs(point.x - x) < 25 && abs(point.y - y) < 25) {
                        indexPoint = allPoints.indexOf(point)
                        break
                    }
                }
            }
        }
        doAlgorithm()
        image?.refresh()
    }

    private fun getCoordPoint(point1: Point, point2: Point, k: Float): Point {
        val coordVectorX = (point2.x - point1.x) * k
        val coordVectorY = (point2.y - point1.y) * k
        return Point(coordVectorX + point1.x, coordVectorY + point1.y, 0.5f, 1f, 1f)
    }

    private fun getIntermediateLines(
        point: Point,
        point1: Point,
        point2: Point,
        ratio: Float,
        ratioElongationLeft: Float,
        ratioElongationRight: Float
    ): Line {
        val line1 = ((point1.x - point.x).pow(2) + (point1.y - point.y).pow(2)).pow(1 / 2)
        val line2 = ((point2.x - point.x).pow(2) + (point2.y - point.y).pow(2)).pow(1 / 2)
        var centreLine1 = Point(
            (point1.x + point.x * ratio) / (1 + ratio),
            (point1.y + point.y * ratio) / (1 + ratio),
            0.5f,
            1f,
            1f
        )
        var centreLine2 = Point(
            (point2.x * ratio + point.x) / (1 + ratio),
            (point2.y * ratio + point.y) / (1 + ratio),
            0.5f,
            1f,
            1f
        )
        val k = line1 / line2
        val pointB = Point(
            ((centreLine1.x + k * centreLine2.x) / (1 + k)),
            (centreLine1.y + k * centreLine2.y) / (1 + k),
            0.5f,
            1f,
            1f
        )
        val a = point.x - pointB.x
        val b = point.y - pointB.y
        centreLine1.x = (centreLine1.x + a)
        centreLine1.y = (centreLine1.y + b)
        centreLine2.x = centreLine2.x + a
        centreLine2.y = centreLine2.y + b
        centreLine1 = getCoordPoint(point, centreLine1, ratioElongationLeft)
        centreLine2 = getCoordPoint(point, centreLine2, ratioElongationRight)
//       image.drawLine(intermediateLine.point1.x, intermediateLine.point1.y, intermediateLine.point2.x, intermediateLine.point2.y, 5f, Color.YELLOW)
//       image.refresh()
        return Line(centreLine1, centreLine2)
    }

    private fun doAlgorithm() {
        var currentLine: Line
        var lastLine: Line? = null
        allIntermediateLines.clear()

        if (allPoints.size > 2) {
            for (i in 1 until allPoints.size - 1) {
                currentLine = getIntermediateLines(
                    allPoints.elementAt(i),
                    allPoints.elementAt(i - 1),
                    allPoints.elementAt(i + 1),
                    allPoints.elementAt(i).ratio,
                    allPoints.elementAt(i).ratioElongationLeft,
                    allPoints.elementAt(i).ratioElongationRight
                )
                allIntermediateLines.add(currentLine)
                if (lastLine != null) {
                    doBezierCurve(
                        allPoints.elementAt(i - 1),
                        lastLine.point2,
                        currentLine.point1,
                        allPoints.elementAt(i)
                    )
                }
                lastLine = currentLine

            }
        }
        image?.refresh()
    }

    private fun doBezierCurve(p1: Point, p2: Point, p3: Point, p4: Point) {

//        val m = 10 //
        val deltaT = 0.1f
        var t = 0f

//        val mX = -3 * ((1 - t).pow(2)) * p1.x + 3 * (1 - 4 * t + 3 * (t.pow(2))) * p2.x +3 * t * (2 - 3 * t) * p3.x + 3 * (t.pow(2)) * p4.x
//        val mY = -3 * ((1 - t).pow(2)) * p1.y + 3 * (1 - 4 * t + 3 * (t.pow(2))) * p2.y +3 * t * (2 - 3 * t) * p3.y + 3 * (t.pow(2)) * p4.y

        val currentPoint =
            Point(p1.x, p1.y, p1.ratio, p1.ratioElongationLeft, p1.ratioElongationRight)
        var lastPoint: Point

        while (t <= 1f) {
            lastPoint = Point(
                currentPoint.x,
                currentPoint.y,
                currentPoint.ratio,
                currentPoint.ratioElongationLeft,
                currentPoint.ratioElongationRight
            )

            currentPoint.x = round(
                (1 - t).pow(3) * p1.x + 3 * t * ((1 - t).pow(2)) * p2.x + 3 * (t.pow(2)) * (1 - t) * p3.x + (t.pow(
                    3
                )) * p4.x
            )
            currentPoint.y = round(
                (1 - t).pow(3) * p1.y + 3 * t * ((1 - t).pow(2)) * p2.y + 3 * (t.pow(2)) * (1 - t) * p3.y + (t.pow(
                    3
                )) * p4.y
            )
            image?.drawLine(
                lastPoint.x,
                lastPoint.y,
                currentPoint.x,
                currentPoint.y,
                5f,
                Color.BLUE
            )
            t += deltaT
        }
        image?.drawLine(currentPoint.x, currentPoint.y, p4.x, p4.y, 5f, Color.BLUE)
        image?.refresh()
    }
}