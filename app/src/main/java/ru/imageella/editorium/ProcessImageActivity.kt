package ru.imageella.editorium

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import kotlin.math.roundToInt


class ProcessImageActivity : AppCompatActivity() {


    private lateinit var currentBitmap: Bitmap
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_process_image)
//
// //       val uri = intent.getParcelableExtra<Uri>(EXTRA_IMAGE_URI)
//
//        if (uri != null) {
//            val imageStream: InputStream? = contentResolver.openInputStream(uri)
//            currentBitmap = BitmapFactory.decodeStream(imageStream)
//            currentImage.setImageBitmap(currentBitmap)
//        }
//
//        saveBtn.setOnClickListener {
//            val sd = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//            val dest = File(sd, "my_pic.jpg")
//            try {
//                FileOutputStream(dest).use { out ->
//                    currentBitmap.compress(
//                        Bitmap.CompressFormat.JPEG,
//                        95,
//                        out
//                    )
//                }
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//        }
//
//        var currentRatio = 1f
//        val seekBarChangeListener: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
//            override fun onStartTrackingTouch(seekBar: SeekBar) {}
//            override fun onStopTrackingTouch(seekBar: SeekBar) {
//                currentRatio = ( seekBar.progress.toFloat() + 1 )  / 4
//                ratioText.text = currentRatio.toString()
//            }
//        }
//
//        ratioSeekBar.setOnSeekBarChangeListener(seekBarChangeListener)
//
//
//        editBtn.setOnClickListener {
//            val w = currentBitmap.width
//            val h = currentBitmap.height
//            val pixels = IntArray(w * h)
//
//            val nw = (w * currentRatio).roundToInt()
//            val nh = (h * currentRatio).roundToInt()
//            currentBitmap.getPixels(pixels, 0, w, 0, 0, w, h)
//            val newPixels = IntArray(nw * nh)
//            for (nx in 0 until nw) {
//                for (ny in 0 until nh) {
//                    val x = (nx / currentRatio).toInt()
//                    val y = (ny / currentRatio).toInt()
//
//                    val i = y * w + x
//                    val ni = ny * nw + nx
//                    newPixels[ni] = pixels[i]
//                }
//            }
//
//            currentBitmap = Bitmap.createBitmap(newPixels, nw, nh, currentBitmap.config)
//            currentImage.setImageBitmap(currentBitmap)
//        }
//    }
//

}