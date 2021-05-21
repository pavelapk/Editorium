package ru.imageella.editorium.tools

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.imageella.editorium.R
import ru.imageella.editorium.databinding.FragmentFiltersToolBinding
import ru.imageella.editorium.interfaces.Algorithm
import ru.imageella.editorium.interfaces.ImageHandler

class FiltersFragment : Fragment(R.layout.fragment_filters_tool), Algorithm {

    private val binding by viewBinding(FragmentFiltersToolBinding::bind, R.id.rootLayout)

    companion object {
        val TAG: String = FiltersFragment::class.java.simpleName

        fun newInstance() = FiltersFragment()
    }

    private var image: ImageHandler? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        image = activity as? ImageHandler

        binding.filter1.setOnClickListener {
            doAlgorithm(FilterType.SEPIA)
        }
        binding.filter2.setOnClickListener {
            doAlgorithm(FilterType.BW)
        }
        binding.filter3.setOnClickListener {
            doAlgorithm(FilterType.NEGATIVE)
        }
        binding.filter4.setOnClickListener {
            doAlgorithm(FilterType.TOTALBW)
        }
        binding.filter5.setOnClickListener {
            doAlgorithm(FilterType.BLUR)
        }
        binding.filter6.setOnClickListener {
            doAlgorithm(FilterType.RED)
        }

    }

    private enum class FilterType {
        SEPIA, BW, NEGATIVE, TOTALBW, BLUR, RED
    }

    private fun doAlgorithm(filterType: FilterType) {
        val bmp = image?.getLastBitmap() ?: return
        val w = bmp.width
        val h = bmp.height
        val pixels = IntArray(w * h)
        bmp.getPixels(pixels, 0, w, 0, 0, w, h)
        for (x in 0 until w) {
            for (y in 0 until h) {
                val i = y * w + x
                val oldPix: Int = pixels[i]
                val oldRed: Int = Color.red(oldPix)
                val oldBlue: Int = Color.blue(oldPix)
                val oldGreen: Int = Color.green(oldPix)
                val oldAlpha: Int = Color.alpha(oldPix)
                pixels[i] = when (filterType) {
                    FilterType.SEPIA -> sepiaFilter(oldAlpha, oldRed, oldGreen, oldBlue)
                    FilterType.BW -> bwFilter(oldAlpha, oldRed, oldGreen, oldBlue)
                    FilterType.NEGATIVE -> negativeFilter(oldAlpha, oldRed, oldGreen, oldBlue)
                    FilterType.TOTALBW -> totalBwFilter(oldAlpha, oldRed, oldGreen, oldBlue)
                    FilterType.BLUR -> blurFilter(pixels, x, y, w, h)
                    FilterType.RED -> redFilter(oldAlpha, oldRed)

                }
            }
        }
        image?.setBitmap(
            Bitmap.createBitmap(pixels, w, h, bmp.config)
        )
    }

    private fun sepiaFilter(a: Int, r: Int, g: Int, b: Int): Int {
        var newGreen: Int = (0.25 * r + 0.65 * g + 0.18 * b).toInt()
        var newRed: Int = (0.3 * r + 0.68 * g + 0.25 * b).toInt()
        var newBlue: Int = (0.18 * r + 0.5 * g + 0.1 * b).toInt()
        if (newRed > 255) {
            newRed = 255
        }
        if (newBlue > 255) {
            newBlue = 255
        }
        if (newGreen > 255) {
            newGreen = 255
        }

        return Color.argb(a, newRed, newGreen, newBlue)
    }

    private fun bwFilter(a: Int, r: Int, g: Int, b: Int): Int {
        val intensity: Int = (b + g + r) / 3
        val newRed: Int = intensity
        val newBlue: Int = intensity
        val newGreen: Int = intensity

        return Color.argb(a, newRed, newGreen, newBlue)
    }

    private fun negativeFilter(a: Int, r: Int, g: Int, b: Int): Int {
        val newRed: Int = 255 - r
        val newBlue: Int = 255 - b
        val newGreen: Int = 255 - g

        return Color.argb(a, newRed, newGreen, newBlue)
    }

    private fun totalBwFilter(a: Int, r: Int, g: Int, b: Int): Int {
        val intensity: Int = (b + g + r) / 3
        val intCoef = 120
        return when {
            intensity > intCoef -> {
                Color.argb(a, 255, 255, 255)
            }
            intensity > 90 -> {
                Color.argb(a, 150, 150, 150)
            }
            else -> {
                Color.argb(a, 0, 0, 0)
            }
        }
    }

    private fun redFilter(a: Int, r: Int): Int {
        return Color.argb(a, r, 0, 0)
    }

    private fun blurFilter(pixels: IntArray, x: Int, y: Int, w: Int, h: Int): Int {
        val i = y * w + x
        val pix = pixels[i]
        var red = 0
        var blue = 0
        var green = 0
        val alpha: Int = Color.alpha(pix)

        val k1 = (x - 6).coerceAtLeast(0)
        val l1 = (y - 6).coerceAtLeast(0)
        val k2 = (x + 6).coerceAtMost(w - 1)
        val l2 = (y + 6).coerceAtMost(h - 1)
        var sum = 0
        for (t in k1 until k2) {
            for (j in l1 until l2) {
                val num = j * w + t
                val oldPix: Int = pixels[num]
                val oldRed: Int = Color.red(oldPix)
                val oldBlue: Int = Color.blue(oldPix)
                val oldGreen: Int = Color.green(oldPix)

                sum += 1
                red += oldRed
                blue += oldBlue
                green += oldGreen
            }
        }

        val newRed: Int = red / sum
        val newBlue: Int = blue / sum
        val newGreen: Int = green / sum
        return Color.argb(alpha, newRed, newGreen, newBlue)
    }

}