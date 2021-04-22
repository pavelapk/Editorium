package ru.imageella.editorium

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
import java.text.DateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min


class MainActivity : AppCompatActivity() {


    private val RESULT_LOAD_IMG = 1
    private val REQUEST_IMAGE_CAPTURE = 2

    private lateinit var currentBitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadBtn.setOnClickListener {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG)
        }

        photoBtn.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "ru.imageella.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
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

    override fun onActivityResult(reqCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(reqCode, resultCode, data)
        if (reqCode == RESULT_LOAD_IMG) {
            if (resultCode == RESULT_OK) {
                try {
                    val imageUri: Uri? = data?.data
                    if (imageUri != null) {
                        val imageStream: InputStream? = contentResolver.openInputStream(imageUri)
                        val selectedImage = BitmapFactory.decodeStream(imageStream)
                        currentBitmap = selectedImage
                        currentImage.setImageBitmap(selectedImage)
                    }
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "You haven't picked Image", Toast.LENGTH_LONG).show()
            }
        }
        if (reqCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            setPic()
        }
    }

    lateinit var currentPhotoPath: String

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = DateFormat.getDateTimeInstance().format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun setPic() {
        // Get the dimensions of the View
        val targetW: Int = currentImage.width
        val targetH: Int = currentImage.height

        val bmOptions = BitmapFactory.Options().apply {
            // Get the dimensions of the bitmap

            currentBitmap = BitmapFactory.decodeFile(currentPhotoPath, this)

            val photoW: Int = outWidth
            val photoH: Int = outHeight

            // Determine how much to scale down the image
            val scaleFactor: Int = max(1, min(photoW / targetW, photoH / targetH))

            inSampleSize = scaleFactor
        }
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions)?.also { bitmap ->
            currentImage.setImageBitmap(bitmap)
        }
    }
}