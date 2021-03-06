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
import ru.imageella.editorium.R
import ru.imageella.editorium.databinding.FragmentRotateToolBinding
import ru.imageella.editorium.interfaces.Algorithm
import ru.imageella.editorium.interfaces.ImageHandler
import ru.imageella.editorium.utils.PixelsWithSizes
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin


class RotateFragment : Fragment(R.layout.fragment_rotate_tool), Algorithm {

    private val binding by viewBinding(FragmentRotateToolBinding::bind, R.id.rootLayout)

    companion object {
        val TAG: String = RotateFragment::class.java.simpleName

        fun newInstance() = RotateFragment()
    }

    private var rotation = 0
    private var image: ImageHandler? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        image = activity as? ImageHandler

        binding.angleSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                setPreviewRotation(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.rotate90Btn.setOnClickListener {
            binding.angleSeekBar.progress =
                (rotation + 359) / 90 * 90 % 360 // уменьшить угол до ближайшего кратного 90
        }

        binding.applyBtn.setOnClickListener {
            if (rotation % 360 == 0) return@setOnClickListener
            viewLifecycleOwner.lifecycleScope.launch {
                image?.progressIndicator(binding.root, true)
                rotate()
                image?.progressIndicator(binding.root, false)
            }
        }
    }

    private suspend fun rotate() {
        val bmp = image?.getBitmap() ?: return
        val width = bmp.width
        val height = bmp.height
        val pixels = IntArray(width * height)
        bmp.getPixels(pixels, 0, width, 0, 0, width, height)

        val curPic = PixelsWithSizes(
            pixels,
            width,
            height
        )

        val angle = Math.toRadians(rotation.toDouble())
        val newPic = rotatePic(curPic, angle)
        if (newPic == null) {
            Toast.makeText(context, "Слишком большое разрешение", Toast.LENGTH_SHORT).show()
            return
        } else {
            binding.angleSeekBar.progress = 0
            image?.setBitmap(
                Bitmap.createBitmap(newPic.pixels, newPic.w, newPic.h, bmp.config)
            )
        }
    }

    private fun roundCoord(xy: Pair<Double, Double>): Pair<Int, Int> =
        Pair(xy.first.roundToInt(), xy.second.roundToInt())


    private fun applyRotation(xy: Pair<Int, Int>, s: Double, c: Double): Pair<Int, Int> {
        val x = xy.first
        val y = xy.second
        val nx = x * c - y * s
        val ny = x * s + y * c
        return roundCoord(Pair(nx, ny))
    }

    private fun calcNewSizes(w: Int, h: Int, angle: Double): Array<Int> {
        val s = sin(angle)
        val c = cos(angle)

        val p0 = applyRotation(Pair(0, 0), s, c)
        val p1 = applyRotation(Pair(w - 1, 0), s, c)
        val p2 = applyRotation(Pair(w - 1, h - 1), s, c)
        val p3 = applyRotation(Pair(0, h - 1), s, c)

        val l = minOf(p0.first, p1.first, p2.first, p3.first)
        val r = maxOf(p0.first, p1.first, p2.first, p3.first)
        val t = minOf(p0.second, p1.second, p2.second, p3.second)
        val b = maxOf(p0.second, p1.second, p2.second, p3.second)

        val nw = r - l + 1
        val nh = b - t + 1
        return arrayOf(nw, nh, l, t)
    }

    private suspend fun rotatePic(pic: PixelsWithSizes, angle: Double): PixelsWithSizes? =
        withContext(Dispatchers.Default) {
            val w = pic.w
            val h = pic.h
            val (nw, nh, l, t) = calcNewSizes(w, h, angle)

            if (max(nw, nh) > 8000) {
                return@withContext null
            }

            val newPic = IntArray(nw * nh)
            val s = sin(-angle)
            val c = cos(-angle)
            for (nx in 0 until nw) {
                for (ny in 0 until nh) {
                    val (x, y) = applyRotation(Pair(nx + l, ny + t), s, c)

                    if (x in 0 until w && y in 0 until h) {
                        val i = y * w + x
                        val ni = ny * nw + nx
                        newPic[ni] = pic.pixels[i]
                    }
                }
            }

            return@withContext PixelsWithSizes(newPic, nw, nh)
        }


    private fun setPreviewRotation(angle: Int) {
        image?.previewRotate(angle.toFloat())
        binding.angleTV.text = angle.toString()
        rotation = angle
    }
}