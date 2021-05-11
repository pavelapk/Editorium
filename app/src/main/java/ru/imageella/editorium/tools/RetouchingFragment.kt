package ru.imageella.editorium.tools

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.imageella.editorium.R
import ru.imageella.editorium.databinding.FragmentRetouchingToolBinding
import ru.imageella.editorium.interfaces.Algorithm
import ru.imageella.editorium.interfaces.ImageHandler
import kotlin.math.exp

class RetouchingFragment : Fragment(R.layout.fragment_retouching_tool), Algorithm {

    private val binding by viewBinding(FragmentRetouchingToolBinding::bind, R.id.rootLayout)

    companion object {
        val TAG: String = RetouchingFragment::class.java.simpleName

        fun newInstance() = RetouchingFragment()
    }

    private lateinit var image: ImageHandler
    private var xRet = 0
    private var yRet = 0
    private var sigma = 3.0
    private var radius = 10

    override fun onImageTouchMove(x: Float, y: Float, isStart: Boolean) {
        xRet = (x * image.getLastBitmap().width).toInt()
        yRet = (y * image.getLastBitmap().height).toInt()
        algorithm()
    }

    private fun algorithm() {

        val w = image.getLastBitmap().width
        val h = image.getLastBitmap().height
        val pixels = IntArray(w * h)
        image.getBitmap().getPixels(pixels, 0, w, 0, 0, w, h)
        val pixelsNew = pixels.copyOf()

        val sig2 = 2 * sigma * sigma
        val sizeWin = (3 * sigma).toInt()
        val window = DoubleArray(2 * sizeWin + 1)
        var fl = 1
        window[sizeWin] = 1.0
        for (i in sizeWin - 1 downTo 0) {
            window[i] = exp(-fl * fl / sig2)
            window[2 * sizeWin - i] = window[i]
            fl++
        }
        val startX = if (xRet - radius >= 0) {
            xRet - radius
        } else {
            0
        }
        val startY = if (yRet - radius >= 0) {
            yRet - radius
        } else {
            0
        }
        val endX = if (xRet + radius < w) {
            xRet + radius
        } else {
            w
        }
        val endY = if (yRet + radius < h) {
            yRet + radius
        } else {
            h
        }

        for (y in startY until endY) {
            for (x in startX until endX) {
                var sum = 0.0
                var red = 0.0
                var blue = 0.0
                var green = 0.0
                val i = y * w + x
                val pix: Int = pixels[i]
                val alpha: Int = Color.alpha(pix)

                for (k in 0 until 2 * sizeWin + 1) {
                    val l = x + k - sizeWin
                    if ((l >= startX) && (l < endX)) {
                        val helpPix: Int = pixels[y * w + l]
                        red += Color.red(helpPix) * window[k]
                        green += Color.green(helpPix) * window[k]
                        blue += Color.blue(helpPix) * window[k]
                        sum += window[k]
                    }
                }
                val newRed: Int = (red / sum).toInt()
                val newBlue: Int = (blue / sum).toInt()
                val newGreen: Int = (green / sum).toInt()
                pixelsNew[i] = Color.argb(alpha, newRed, newGreen, newBlue)
            }
        }
        val newPixelsArr = pixelsNew.copyOf()

        for (x in startX until endX) {
            for (y in startY until endY) {
                var sum = 0.0
                var red = 0.0
                var blue = 0.0
                var green = 0.0
                val i = y * w + x
                val pix: Int = newPixelsArr[i]
                val alpha: Int = Color.alpha(pix)

                for (k in 0 until 2 * sizeWin + 1) {
                    val l = y + k - sizeWin
                    if ((l >= startY) && (l < endY)) {
                        val helpPix: Int = newPixelsArr[l * w + x]
                        red += Color.red(helpPix) * window[k]
                        green += Color.green(helpPix) * window[k]
                        blue += Color.blue(helpPix) * window[k]
                        sum += window[k]
                    }
                }
                val newRed: Int = (red / sum).toInt()
                val newBlue: Int = (blue / sum).toInt()
                val newGreen: Int = (green / sum).toInt()
                pixelsNew[i] = Color.argb(alpha, newRed, newGreen, newBlue)
            }
        }
        image.setBitmap(
            Bitmap.createBitmap(pixelsNew, w, h, image.getLastBitmap().config)
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        image = activity as ImageHandler

        binding.sigmaSeekBar2.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.textViewForSigma.text = progress.toString()
                sigma = progress.toDouble()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        binding.radiusSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.radiusText.text = progress.toString()
                radius = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        /*binding.maskBtn.setOnClickListener {
            val w = image.getLastBitmap().width
            val h = image.getLastBitmap().height
            val pixels = IntArray(w * h)
            val pixelsNew = pixels.copyOf()
            image.getLastBitmap().getPixels(pixels, 0, w, 0, 0, w, h)

            val sig2 = 2*sigma*sigma
            val sizeWin = (3*sigma).toInt()
            val window = DoubleArray(2*sizeWin + 1)
            var fl = 1
            window[sizeWin] = 1.0
            for(i in sizeWin - 1 downTo  0){
                window[i] = kotlin.math.exp(-fl * fl / sig2)
                window[2*sizeWin - i] = window[i]
                fl++
            }
            val startX = if(xRet - radius >= 0){
                xRet - radius
            } else { 0 }
            val startY = if(yRet - radius >= 0){
                yRet - radius
            } else { 0 }
            val endX = if(xRet + radius < w){
                xRet + radius
            } else { w }
            val endY = if(yRet + radius < h){
                yRet + radius
            } else { h }

            for (y in startY until endY){
                for (x in startX until endX){
                    var sum = 0.0
                    var red = 0.0
                    var blue = 0.0
                    var green = 0.0
                    val i = y*w + x
                    val pix: Int = pixels[i]
                    val alpha: Int = Color.alpha(pix)

                    for (k in 0 until 2*sizeWin + 1){
                        val l = x + k - sizeWin
                        if ((l >= startX) && (l <= endX)){
                            val helpPix: Int = pixels[y*w + l]
                            red += Color.red(helpPix)*window[k]
                            green += Color.green(helpPix)*window[k]
                            blue += Color.blue(helpPix)*window[k]
                            sum += window[k]
                        }
                    }
                    val newRed: Int = (red/sum).toInt()
                    val newBlue: Int = (blue/sum).toInt()
                    val newGreen: Int = (green/sum).toInt()
                    pixelsNew[i] = Color.argb(alpha, newRed, newGreen, newBlue)
                }
            }
           val newPixelsArr = pixelsNew.copyOf()

            for (x in startX until endX){
                for (y in startY until endY){
                    var sum = 0.0
                    var red = 0.0
                    var blue = 0.0
                    var green = 0.0
                    val i = y*w + x
                    val pix: Int = newPixelsArr[i]
                    val alpha: Int = Color.alpha(pix)

                    for (k in 0 until 2*sizeWin + 1){
                        val l = y + k - sizeWin
                        if ((l >=startY) && (l <= endY)){
                            val helpPix: Int = newPixelsArr[l*w+x]
                            red += Color.red(helpPix)*window[k]
                            green += Color.green(helpPix)*window[k]
                            blue += Color.blue(helpPix)*window[k]
                            sum += window[k]
                        }
                    }
                    val newRed: Int = (red/sum).toInt()
                    val newBlue: Int = (blue/sum).toInt()
                    val newGreen: Int = (green/sum).toInt()
                    pixelsNew[i] = Color.argb(alpha, newRed, newGreen, newBlue)
                }
            }
            image.setBitmap(
                Bitmap.createBitmap(pixelsNew, w, h, image.getLastBitmap().config)
            )*/

        //}
    }

    override fun doAlgorithm() {


        val width = image.getBitmap().width
        val height = image.getBitmap().height
        val pixels = IntArray(width * height)
        image.getBitmap().getPixels(pixels, 0, width, 0, 0, width, height)


        /* image.setBitmap(
             Bitmap.createBitmap(curPic.pixels, curPic.w, curPic.h, image.getBitmap().config)
         )*/
    }


}