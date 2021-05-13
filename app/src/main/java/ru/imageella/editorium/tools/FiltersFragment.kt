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

    private lateinit var image: ImageHandler

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        image = activity as ImageHandler

        binding.filter1.setOnClickListener {
            val w = image.getLastBitmap().width
            val h = image.getLastBitmap().height
            val pixels = IntArray(w * h)
            image.getLastBitmap().getPixels(pixels, 0, w, 0, 0, w, h)

            for (x in 0 until w) {
                for (y in 0 until h) {

                    val i = y * w + x
                    val oldPix: Int = pixels[i]
                    val oldRed: Int = Color.red(oldPix)
                    val oldBlue: Int = Color.blue(oldPix)
                    val oldGreen: Int = Color.green(oldPix)
                    val oldAlpha: Int = Color.alpha(oldPix)


                    //сепия почти
                    var newGreen: Int = (0.25 * oldRed + 0.65 * oldGreen + 0.18 * oldBlue).toInt()
                    var newRed: Int = (0.3 * oldRed + 0.68 * oldGreen + 0.25 * oldBlue).toInt()
                    var newBlue: Int = (0.18 * oldRed + 0.5 * oldGreen + 0.1 * oldBlue).toInt()
                    if (newRed > 255) {
                        newRed = 255
                    }
                    if (newBlue > 255) {
                        newBlue = 255
                    }
                    if (newGreen > 255) {
                        newGreen = 255
                    }

                    pixels[i] = Color.argb(oldAlpha, newRed, newGreen, newBlue)
                }
            }
            image.setBitmap(
                Bitmap.createBitmap(pixels, w, h, image.getLastBitmap().config)
            )

        }
        binding.filter2.setOnClickListener {
            val w = image.getLastBitmap().width
            val h = image.getLastBitmap().height
            val pixels = IntArray(w * h)
            image.getLastBitmap().getPixels(pixels, 0, w, 0, 0, w, h)

            for (x in 0 until w) {
                for (y in 0 until h) {
                    val i = y * w + x
                    val oldPix: Int = pixels[i]
                    val oldRed: Int = Color.red(oldPix)
                    val oldBlue: Int = Color.blue(oldPix)
                    val oldGreen: Int = Color.green(oldPix)
                    val oldAlpha: Int = Color.alpha(oldPix)

                    //фильтр чб
                    val intensity: Int = (oldBlue + oldGreen + oldRed) / 3
                    val newRed: Int = intensity
                    val newBlue: Int = intensity
                    val newGreen: Int = intensity

                    pixels[i] = Color.argb(oldAlpha, newRed, newGreen, newBlue)
                }
            }
            image.setBitmap(
                Bitmap.createBitmap(pixels, w, h, image.getLastBitmap().config)
            )
        }
        binding.filter3.setOnClickListener {
            val w = image.getLastBitmap().width
            val h = image.getLastBitmap().height
            val pixels = IntArray(w * h)
            image.getLastBitmap().getPixels(pixels, 0, w, 0, 0, w, h)
            for (x in 0 until w) {
                for (y in 0 until h) {
                    val i = y * w + x
                    val oldPix: Int = pixels[i]
                    val oldRed: Int = Color.red(oldPix)
                    val oldBlue: Int = Color.blue(oldPix)
                    val oldGreen: Int = Color.green(oldPix)
                    val oldAlpha: Int = Color.alpha(oldPix)

                    val newRed: Int = 255 - oldRed
                    val newBlue: Int = 255 - oldBlue
                    val newGreen: Int = 255 - oldGreen

                    pixels[i] = Color.argb(oldAlpha, newRed, newGreen, newBlue)

                }
            }
            image.setBitmap(
                Bitmap.createBitmap(pixels, w, h, image.getLastBitmap().config)
            )
        }
        binding.filter4.setOnClickListener {
            val w = image.getLastBitmap().width
            val h = image.getLastBitmap().height
            val pixels = IntArray(w * h)
            image.getLastBitmap().getPixels(pixels, 0, w, 0, 0, w, h)

            for (x in 0 until w) {
                for (y in 0 until h) {
                    val i = y * w + x
                    val oldPix: Int = pixels[i]
                    val oldRed: Int = Color.red(oldPix)
                    val oldBlue: Int = Color.blue(oldPix)
                    val oldGreen: Int = Color.green(oldPix)
                    val oldAlpha: Int = Color.alpha(oldPix)

                    val intensity: Int = (oldBlue + oldGreen + oldRed) / 3
                    val intCoef = 120
                    when {
                        intensity > intCoef -> {
                            pixels[i] = Color.argb(oldAlpha, 255, 255, 255)
                        }
                        intensity > 90 -> {
                            pixels[i] = Color.argb(oldAlpha, 150, 150, 150)
                        }
                        else -> {
                            pixels[i] = Color.argb(oldAlpha, 0, 0, 0)
                        }
                    }
                }
            }
            image.setBitmap(
                Bitmap.createBitmap(pixels, w, h, image.getLastBitmap().config)
            )
        }
        binding.filter5.setOnClickListener {
            val w = image.getLastBitmap().width
            val h = image.getLastBitmap().height
            val pixels = IntArray(w * h)
            image.getLastBitmap().getPixels(pixels, 0, w, 0, 0, w, h)

            for (x in 0 until w) {
                for (y in 0 until h) {

                    val i = y * w + x
                    val pix = pixels[i]
                    var red = 0
                    var blue = 0
                    var green = 0
                    val alpha: Int = Color.alpha(pix)

                    val k1 = if (x - 6 < 0) {
                        0
                    } else {
                        x - 6
                    }
                    val l1 = if (y - 6 < 0) {
                        0
                    } else {
                        y - 6
                    }
                    val k2 = if (x + 6 >= w) {
                        w
                    } else {
                        x + 6
                    }
                    val l2 = if (y + 6 >= h) {
                        h
                    } else {
                        y + 6
                    }
                    var sum = 0
                    for (t in k1 until k2) {
                        for (j in l1 until l2) {
                            val num = j * w + t
                            val oldPix: Int = pixels[num]
                            val oldRed: Int = Color.red(oldPix)
                            val oldBlue: Int = Color.blue(oldPix)
                            val oldGreen: Int = Color.green(oldPix)
                            sum++

                            red += oldRed
                            blue += oldBlue
                            green += oldGreen
                        }
                    }

                    val newRed: Int = red / sum
                    val newBlue: Int = blue / sum
                    val newGreen: Int = green / sum
                    pixels[i] = Color.argb(alpha, newRed, newGreen, newBlue)
                }
            }
            image.setBitmap(
                Bitmap.createBitmap(pixels, w, h, image.getLastBitmap().config)
            )
        }
        binding.filter6.setOnClickListener {
            val w = image.getLastBitmap().width
            val h = image.getLastBitmap().height
            val pixels = IntArray(w * h)
            image.getLastBitmap().getPixels(pixels, 0, w, 0, 0, w, h)

            for (x in 0 until w) {
                for (y in 0 until h) {
                    val i = y * w + x
                    val oldPix: Int = pixels[i]
                    val oldRed: Int = Color.red(oldPix)
                    val oldAlpha: Int = Color.alpha(oldPix)
                    pixels[i] = Color.argb(oldAlpha, oldRed, 0, 0)
                }
            }
            image.setBitmap(
                Bitmap.createBitmap(pixels, w, h, image.getLastBitmap().config)
            )
        }

    }


}