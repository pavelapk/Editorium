package ru.imageella.editorium.tools

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.imageella.editorium.Filtering.Companion.doBilinearFilteredPixelColor
import ru.imageella.editorium.Filtering.Companion.doTrilinearFilteredPixelColor
import ru.imageella.editorium.Filtering.Companion.halfSize
import ru.imageella.editorium.R
import ru.imageella.editorium.databinding.FragmentScaleToolBinding
import ru.imageella.editorium.interfaces.Algorithm
import ru.imageella.editorium.interfaces.ImageHandler
import ru.imageella.editorium.utils.PixelsWithSizes
import kotlin.math.ceil
import kotlin.math.max

class ScaleFragment : Fragment(R.layout.fragment_scale_tool), Algorithm {

    private val binding by viewBinding(FragmentScaleToolBinding::bind, R.id.rootLayout)

    companion object {
        val TAG: String = ScaleFragment::class.java.simpleName

        fun newInstance() = ScaleFragment()
    }


    private var currentRatio = 1f
    private var image: ImageHandler? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        image = activity as? ImageHandler

        val seekBarChangeListener: SeekBar.OnSeekBarChangeListener = object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                setPreviewScale(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        }

        binding.ratioSeekBar.setOnSeekBarChangeListener(seekBarChangeListener)
        binding.applyBtn.setOnClickListener {
            if (currentRatio == 1f) return@setOnClickListener
            viewLifecycleOwner.lifecycleScope.launch {
                image?.progressIndicator(binding.root, true)
                scale()
                image?.progressIndicator(binding.root, false)
            }
        }
    }


    private suspend fun scale() {
        val bmp = image?.getBitmap() ?: return
        val width = bmp.width
        val height = bmp.height
        val nw = ceil(width * currentRatio).toInt()
        val nh = ceil(height * currentRatio).toInt()
        if (max(nw, nh) > 8000) {
            Toast.makeText(context, "Слишком большое разрешение", Toast.LENGTH_SHORT).show()
            return
        }
        val pixels = IntArray(width * height)
        val newPixels = IntArray(nw * nh)

        withContext(Dispatchers.Default) {
            bmp.getPixels(pixels, 0, width, 0, 0, width, height)

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
        }

        image?.setBitmap(
            Bitmap.createBitmap(newPixels, nw, nh, bmp.config)
        )
        binding.ratioSeekBar.progress = 7 // 1.0x
    }


    private fun setPreviewScale(ratio: Float) {
        currentRatio = (ratio + 1) / 8
        image?.previewScale(currentRatio)
        binding.ratioTV.text = currentRatio.toString()
    }


}