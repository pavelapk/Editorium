package ru.imageella.editorium

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import kotlinx.android.synthetic.main.activity_process_image.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

class ProcessImageActivity : AppCompatActivity() {


    private lateinit var currentBitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_process_image)

        val uri = intent.getParcelableExtra<Uri>(EXTRA_IMAGE_URI)

        if (uri != null) {
            val imageStream: InputStream? = contentResolver.openInputStream(uri)
            currentBitmap = BitmapFactory.decodeStream(imageStream)
            currentImage.setImageBitmap(currentBitmap)
        }
        saveBtn.setOnClickListener {
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

        editBtn.setOnClickListener {
            val w = currentBitmap.width
            val h = currentBitmap.height
            val pixels = IntArray(w * h)
            currentBitmap.getPixels(pixels, 0, w, 0, 0, w, h)


            val newPixels = IntArray(w * h)
            for (x in 0 until w) {
                for (y in 0 until h) {
                    val nx = h - 1 - y
                    val ny = x

//                    pixels[i] = Color.argb(Color.alpha(pixels[i]), Color.red(pixels[i]), 0, 0)

                    val i = y * w + x
                    val ni = ny * h + nx
                    newPixels[ni] = pixels[i]
                }
            }

            currentBitmap = Bitmap.createBitmap(newPixels, h, w, currentBitmap.config)
            currentImage.setImageBitmap(currentBitmap)
        }
    }


}