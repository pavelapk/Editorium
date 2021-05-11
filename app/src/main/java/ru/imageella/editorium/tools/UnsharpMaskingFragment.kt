package ru.imageella.editorium.tools

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.imageella.editorium.R
import ru.imageella.editorium.databinding.FragmentUnsharpMaskingToolBinding
import ru.imageella.editorium.interfaces.Algorithm
import ru.imageella.editorium.interfaces.ImageHandler

class UnsharpMaskingFragment : Fragment(R.layout.fragment_unsharp_masking_tool), Algorithm {

    private val binding by viewBinding(FragmentUnsharpMaskingToolBinding::bind, R.id.rootLayout)

    companion object {
        val TAG: String = UnsharpMaskingFragment::class.java.simpleName

        fun newInstance() = UnsharpMaskingFragment()
    }

    private lateinit var image: ImageHandler

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        image = activity as ImageHandler
        var sigma = 3.0
        var coef = 1.0
        binding.sigmaSeekBar2.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.textViewForSigma.text = progress.toString()
               sigma = progress.toDouble()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        binding.coefSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.textViewForCoef.text = progress.toString()
                coef = progress/10.0
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.maskBtn.setOnClickListener {
            val w = image.getLastBitmap().width
            val h = image.getLastBitmap().height
            val pixels = IntArray(w * h)
            val pixelsNew = IntArray(w * h)
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

            for (y in 0 until h){
                for (x in 0 until w){
                    var sum = 0.0
                    var red = 0.0
                    var blue = 0.0
                    var green = 0.0
                    val i = y*w + x
                    val pix: Int = pixels[i]
                    val alpha: Int = Color.alpha(pix)

                    for (k in 0 until 2*sizeWin + 1){
                        val l = x + k - sizeWin
                        if ((l >= 0) && (l < w)){
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

            for (x in 0 until w){
                for (y in 0 until h){
                    var sum = 0.0
                    var red = 0.0
                    var blue = 0.0
                    var green = 0.0
                    val i = y*w + x
                    val pix: Int = newPixelsArr[i]
                    val alpha: Int = Color.alpha(pix)

                    for (k in 0 until 2*sizeWin + 1){
                        val l = y + k - sizeWin
                        if ((l >=0) && (l < h)){
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

            for (x in 0 until w){
                for (y in 0 until h){
                    val i = y*w + x
                    val blurPix: Int = pixelsNew[i]
                    val origPix: Int = pixels[i]
                    val maskRed: Int = Color.red(origPix) - Color.red(blurPix)
                    val maskBlue: Int = Color.blue(origPix) - Color.blue(blurPix)
                    val maskGreen: Int = Color.green(origPix) - Color.green(blurPix)
                    val alpha: Int = Color.alpha(origPix)



                    var newRed: Int = (Color.red(origPix) + coef*maskRed).toInt()
                    var newBlue: Int = (Color.blue(origPix) + coef*maskBlue).toInt()
                    var newGreen: Int = (Color.green(origPix) + coef*maskGreen).toInt()

                    if (newRed > 255) {
                        newRed = 255
                    } else if (newRed < 0){
                        newRed = 0
                    }
                    if (newBlue > 255){
                        newBlue = 255
                    } else if (newBlue < 0){
                        newBlue = 0
                    }
                    if (newGreen > 255){
                        newGreen = 255
                    } else if (newGreen < 0){
                        newGreen = 0
                    }
                    pixels[i] = Color.argb(alpha, newRed, newGreen, newBlue)
                }
            }
            image.setBitmap(
                Bitmap.createBitmap(pixels, w, h, image.getLastBitmap().config)
            )

        }
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