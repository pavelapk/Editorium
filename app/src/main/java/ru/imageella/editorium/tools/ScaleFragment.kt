package ru.imageella.editorium.tools

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.imageella.editorium.R
import ru.imageella.editorium.databinding.FragmentScaleToolBinding
import ru.imageella.editorium.interfaces.Algorithm
import ru.imageella.editorium.interfaces.ImageHandler
import kotlin.math.*

class ScaleFragment : Fragment(R.layout.fragment_scale_tool), Algorithm {

    private val binding by viewBinding(FragmentScaleToolBinding::bind, R.id.rootLayout)

    companion object {
        val TAG: String = ScaleFragment::class.java.simpleName

        fun newInstance() = ScaleFragment()
    }

    class PixelsWithSizes(
        val pixels: IntArray,
        val w: Int,
        val h: Int
    )

    private var currentRatio = 1f
    private lateinit var image: ImageHandler

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        image = activity as ImageHandler

        val seekBarChangeListener: SeekBar.OnSeekBarChangeListener = object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                currentRatio = (seekBar.progress.toFloat() + 1) / 4
                binding.ratioTV.text = currentRatio.toString()
                setPreviewRotation(currentRatio)
            }
        }

        binding.ratioSeekBar.setOnSeekBarChangeListener(seekBarChangeListener)
        binding.applyBtn.setOnClickListener {
            setPreviewRotation(1f)
            doAlgorithm()
            binding.ratioSeekBar.progress = 3
        }
    }

    override fun doAlgorithm() {

        val width = image.getBitmap().width
        val height = image.getBitmap().height
        val nw = (width * currentRatio).toInt()
        val nh = (height * currentRatio).toInt()
        val pixels = IntArray(width * height)
        val newPixels = IntArray(nw * nh)

        image.getBitmap().getPixels(pixels, 0, width, 0, 0, width, height)

//        for (nx in 0 until nw) {
//            for (ny in 0 until nh) {
//                val x = (nx / currentRatio).toInt()
//                val y = (ny / currentRatio).toInt()
//
//                val i = y * width + x
//                val ni = ny * nw + nx
//                newPixels[ni] = pixels[i]
//            }
//        }

        if (currentRatio >= 1f) {
            for (nx in 0 until nw) {
                for (ny in 0 until nh) {
                    val ni = ny * nw + nx
                    newPixels[ni] = doBilinearFilteredPixelColor(
                        pixels,
                        width,
                        height,
                        nx / currentRatio,
                        ny / currentRatio
                    )
                }
            }
        } else {
            val k = 1 / currentRatio
            var m = 1
            while (m <= k) {
                m *= 2
            }
            m /= 2

            var mPic = PixelsWithSizes(
                pixels,
                width,
                height
            )
            var curM = 1
            while (curM < m) {
                mPic = halfSize(mPic)
                curM *= 2
            }
            val m2Pic = halfSize(mPic)

            for (nx in 0 until nw) {
                for (ny in 0 until nh) {
                    val ni = ny * nw + nx
                    newPixels[ni] = doTrilinearFilteredPixelColor(
                        mPic,
                        m2Pic,
                        m,
                        k,
                        nx / currentRatio,
                        ny / currentRatio
                    )
                }
            }
        }

        image.setBitmap(
            Bitmap.createBitmap(newPixels, nw, nh, image.getBitmap().config)
        )
    }

    private fun halfSize(pic: PixelsWithSizes): PixelsWithSizes {
        val w = pic.w
        val h = pic.h
        val nw = (w) / 2
        val nh = (h) / 2
        val newPic = IntArray(nw * nh)
        for (nx in 0 until nw) {
            for (ny in 0 until nh) {
                val ni = ny * nw + nx
                newPic[ni] = average4pix(pic, nx, ny)
            }
        }
        return PixelsWithSizes(newPic, nw, nh)
    }

    private fun average4pix(pic: PixelsWithSizes, x: Int, y: Int): Int {
        val p1 = y * 2 * pic.w + x * 2
        val p2 = y * 2 * pic.w + (x * 2 + 1)
        val p3 = (y * 2 + 1) * pic.w + x * 2
        val p4 = (y * 2 + 1) * pic.w + (x * 2 + 1)
        val result = IntArray(4)
        for (c in 0 until 4) {
            result[c] = (getColorChannel(pic.pixels[p1], c) + getColorChannel(pic.pixels[p2], c) +
                    getColorChannel(pic.pixels[p3], c) + getColorChannel(pic.pixels[p4], c)) / 4
        }
        return Color.argb(result[0], result[1], result[2], result[3])
    }

    private fun setPreviewRotation(ratio: Float) {
        image.previewScale(ratio)
    }

    private fun doBilinearFilteredPixelColor(
        pixels: IntArray,
        w: Int,
        h: Int,
        x: Float,
        y: Float
    ): Int {
        val floorX = x.toInt()
        val floorY = y.toInt()
        val ceilX = (x + 1).toInt()
        val ceilY = (y + 1).toInt()
        val n1 = floorY * w + floorX
        val n2 = floorY * w + min(ceilX, w - 1)
        val n3 = min(ceilY, h - 1) * w + floorX
        val n4 = min(ceilY, h - 1) * w + min(ceilX, w - 1)

        val result = IntArray(4)
        for (c in 0 until 4) {
            val r1 = (getColorChannel(pixels[n1], c) * (ceilX - x) +
                    getColorChannel(pixels[n2], c) * (x - floorX)) * (ceilY - y)
            val r2 = (getColorChannel(pixels[n3], c) * (ceilX - x) +
                    getColorChannel(pixels[n4], c) * (x - floorX)) * (y - floorY)
            result[c] = (r1 + r2).toInt()
        }
        return Color.argb(result[0], result[1], result[2], result[3])
    }

    private fun doTrilinearFilteredPixelColor(
        mPic: PixelsWithSizes,
        m2Pic: PixelsWithSizes,
        m: Int,
        k: Float,
        x: Float,
        y: Float
    ): Int {
        val mX = (x / m).toInt()
        val mY = (y / m).toInt()
        val m2X = (x / m / 2).toInt()
        val m2Y = (y / m / 2).toInt()

        val mi = mY * mPic.w + mX
        val m2i = m2Y * m2Pic.w + m2X
        val result = IntArray(4)
        for (c in 0 until 4) {
            result[c] = ((getColorChannel(mPic.pixels[mi], c) * (2 * m - k) +
                    getColorChannel(m2Pic.pixels[m2i], c) * (k - m)) / m).toInt()
        }
        return Color.argb(result[0], result[1], result[2], result[3])
    }

    private fun getColorChannel(color: Int, channel: Int) =
        when (channel) {
            0 -> Color.alpha(color)
            1 -> Color.red(color)
            2 -> Color.green(color)
            3 -> Color.blue(color)
            else -> 255
        }

}