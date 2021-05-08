package ru.imageella.editorium.tools

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.imageella.editorium.R
import ru.imageella.editorium.databinding.FragmentRotateToolBinding
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
                currentRatio = ( seekBar.progress.toFloat() + 1 )  / 4
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
        val nw = (width * currentRatio).roundToInt()
        val nh = (height * currentRatio).roundToInt()
        val pixels = IntArray(width * height)
        val newPixels = IntArray(nw * nh)

        image.getBitmap().getPixels(pixels, 0, width, 0, 0, width, height)

        if(currentRatio >= 1f){
            for (nx in 0 until nw - 1) {
                for (ny in 0 until nh - 1) {
                    val x = (nx / currentRatio).toInt()
                    val y = (ny / currentRatio).toInt()

                    val i = y * width + x
                    val ni = ny * nw + nx
                    if( (x.toFloat() != nx / currentRatio || y.toFloat() != ny / currentRatio)){
                        val result = doBilinearFilteredPixelColor(pixels, width, nx / currentRatio, ny / currentRatio)
                        newPixels[ni] = Color.argb(result[0], result[1], result[2], result[3])
                    }
                    else{
//                        if( (x.toFloat() != nx / currentRatio || y.toFloat() != ny / currentRatio)){
////                            newPixels[ni] = Color.argb(255, 0, 0, 255)
//                            newPixels[ni] = pixels[i]
//                        }
//                        else{
//                            newPixels[ni] = pixels[i]
//                        }
                        newPixels[ni] = pixels[i]

                    }
                }
            }
        }
        else{

            var m = 1
            while(currentRatio <= (1f / m)){
                m *= 2
            }

            val nw1 = (width / (m / 2)).toFloat().roundToInt()
            val nh1 = (height / (m / 2)).toFloat().roundToInt()
            val nw2 = (width / m).toFloat().roundToInt()
            val nh2 = (height / m).toFloat().roundToInt()
            val mLevel = IntArray(nw1 * nh1)
            val m2Level = IntArray(nw2 * nh2)
            for (nx1 in 0 until nw1) {
                for (ny1 in 0 until nh1) {
                    val x = (nx1 * (m / 2))
                    val y = (ny1 * (m / 2))

                    val i = y * width + x
                    val ni = ny1 * nw1 + nx1
                    mLevel[ni] = pixels[i]
                }
            }
            for (nx2 in 0 until nw2) {
                for (ny2 in 0 until nh2) {
                    val x = (nx2 * m)
                    val y = (ny2 * m)

                    val i = y * width + x
                    val ni = ny2 * nw2 + nx2
                    m2Level[ni] = pixels[i]
                }
            }
            for (nx in 0 until nw - 1) {
                for (ny in 0 until nh - 1) {
                    val x = (nx / currentRatio)
                    val y = (ny / currentRatio)

                    val i = y.toInt() * width + x.toInt()
                    val ni = ny * nw + nx
                    if(x.toInt().toFloat() != x || y.toInt().toFloat() != y){
                        val result = doTrilinearFilteredPixelColor(pixels, mLevel, m2Level, x, y, width, (m / 2), currentRatio)
                        newPixels[ni] = Color.argb(result[0], result[1], result[2], result[3])
                    }
                    else
                    {
                        newPixels[ni] = pixels[i]
                    }
                }
            }

        }


        
        image.setBitmap(
            Bitmap.createBitmap(newPixels, nw, nh, image.getBitmap().config)
        )
    }
    private fun setPreviewRotation(ratio: Float) {
        image.previewScale(ratio.toFloat())
    }
    private fun doBilinearFilteredPixelColor( pixels:IntArray, w: Int, u: Float, v: Float): IntArray{
        val floorX = u.toInt()
        val floorY = v.toInt()
        val ceilX = ceil(u).toInt()
        val ceilY = ceil(v).toInt()
        val n1 = floorY * w + floorX
        val n2 = floorY * w + ceilX
        val n3 = ceilY * w + floorX
        val n4 = ceilY * w + ceilX
        val result = IntArray(4)

        for (c in 0 until 4){
            val p:Float
            val r1 = (getColorChannel(pixels[n1], c) * (ceilX - u) + getColorChannel(pixels[n2], c)*(u - floorX)) / (ceilX - floorX)
            val r2 = (getColorChannel(pixels[n3], c) * (ceilX - u) + getColorChannel(pixels[n4], c)*(u - floorX)) / (ceilX - floorX)
            p = if (floorY == ceilY) {
                r1
            } else{
                if(floorX == ceilX){
                    (getColorChannel(pixels[n1], c)* (ceilY - v) + getColorChannel(pixels[n3], c)*(v - floorY)) / (ceilY - floorY)
                } else{
                    (r1* (ceilY - v) + r2*(v - floorY)) / (ceilY - floorY)
                }
            }

//            val r = ((getColorChannel(pixels[n1], c) * (ceilX - u) + getColorChannel(pixels[n2], c)*(u - floorX))*(ceilY - v) +
//                    + (getColorChannel(pixels[n3], c) * (ceilX - u) + getColorChannel(pixels[n4], c)*(u - floorX))*(v - floorY)).toLong()

            result[c] = p.toInt()
        }
        return result
    }
    private fun doTrilinearFilteredPixelColor(pixels:IntArray, mLevel:IntArray, m2Level:IntArray, u:Float, v:Float, w:Int, m:Int, k:Float): IntArray{
        val x = u
        val y = v
        val mx = (x / m)
        val my = (y / m)
        val m2x = (x / (m * 2))
        val m2y = (y / (m * 2))
        val mResult = doBilinearFilteredPixelColor(mLevel, (w / m), mx, my)
        val m2Result = doBilinearFilteredPixelColor(m2Level, (w / (m * 2)), m2x, m2y)
        var result = IntArray(4)
        for(i in 0 until 4){
            result[i] = ((mResult[i]*(2* m - (1 / k)) + m2Result[i]*((1 / k) - m)) / m).toInt()
        }
        return result
    }
    private fun getColorChannel(color: Int, channel:Int ) =
        when (channel) {
            0 -> Color.alpha(color)
            1 -> Color.red(color)
            2 -> Color.green(color)
            3 -> Color.blue(color)
            else -> 255
        }



}