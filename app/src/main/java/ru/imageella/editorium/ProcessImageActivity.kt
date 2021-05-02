package ru.imageella.editorium

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.imageella.editorium.databinding.ActivityProcessImageBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin

class ProcessImageActivity : AppCompatActivity(R.layout.activity_process_image) {

    private val binding by viewBinding(ActivityProcessImageBinding::bind, R.id.rootLayout)

    private lateinit var currentBitmap: Bitmap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent.getParcelableExtra<Uri>(StartActivity.EXTRA_IMAGE_URI)

        if (uri != null) {
            val imageStream: InputStream? = contentResolver.openInputStream(uri)
            currentBitmap = BitmapFactory.decodeStream(imageStream)
            binding.currentImage.setImageBitmap(currentBitmap)
        }

        binding.saveBtn.setOnClickListener {
            val sd = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val dest = File(sd, "my_pic.jpg")
            try {
                FileOutputStream(dest).use { out ->
                    currentBitmap.compress(
                        Bitmap.CompressFormat.JPEG,
                        95,
                        out
                    )
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

//        binding.angleSeekBar.setOnSeekBarChangeListener(object :
//            SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                setPreviewRotation(progress)
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
//        })

        binding.editBtn.setOnClickListener {

            val width = currentBitmap.width
            val height = currentBitmap.height
            val pixels = IntArray(width * height)
            currentBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            var curPic = PixelsWithDim(
                pixels,
                width,
                height
            )

            //val angle = Math.toRadians(rotation.toDouble())

//            Log.d("DAROVA", "$s $c")


//            curPic = rotatePic(curPic, angle)
//
//            currentBitmap =
//                Bitmap.createBitmap(curPic.pixels, curPic.w, curPic.h, currentBitmap.config)
//            binding.currentImage.setImageBitmap(currentBitmap)
//            //binding.angleSeekBar.progress = 0

        }
    }

    class PixelsWithDim(
        val pixels: IntArray,
        val w: Int,
        val h: Int
    )

    private fun roundCoord(xy: Pair<Double, Double>): Pair<Int, Int> =
        Pair(round(xy.first).toInt(), round(xy.second).toInt())


    private fun applyRotation(xy: Pair<Int, Int>, s: Double, c: Double): Pair<Int, Int> {
        val x = xy.first
        val y = xy.second
        val nx = x * c - y * s
        val ny = x * s + y * c
        return roundCoord(Pair(nx, ny))
    }


    private fun rotatePic(pic: PixelsWithDim, angle: Double): PixelsWithDim {
        var s = sin(angle)
        var c = cos(angle)

        val w = pic.w
        val h = pic.h

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

        val newPic = IntArray(nw * nh)
        s = sin(-angle)
        c = cos(-angle)
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

        return PixelsWithDim(newPic, nw, nh)
    }


    fun setPreviewRotation(angle: Int) {
        binding.currentImage.rotation = angle.toFloat()
        // binding.angleTV.text = angle.toString()
        //rotation = angle
    }

}