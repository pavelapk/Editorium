package ru.imageella.editorium.tools

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.imageella.editorium.Filtering.Companion.doBilinearFilteredPixelColor
import ru.imageella.editorium.Filtering.Companion.doTrilinearFilteredPixelColor
import ru.imageella.editorium.Filtering.Companion.halfSize
import ru.imageella.editorium.PixelsWithSizes
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
            val pic = PixelsWithSizes(
                pixels,
                width,
                height
            )
            for (nx in 0 until nw) {
                for (ny in 0 until nh) {
                    val ni = ny * nw + nx
                    newPixels[ni] = doBilinearFilteredPixelColor(
                        pic,
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


    private fun setPreviewRotation(ratio: Float) {
        image.previewScale(ratio)
    }


}