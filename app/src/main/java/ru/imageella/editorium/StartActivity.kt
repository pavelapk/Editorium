package ru.imageella.editorium

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.appcompat.app.AppCompatActivity
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.imageella.editorium.databinding.ActivityStartBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class StartActivity : AppCompatActivity(R.layout.activity_start) {

    companion object {
        internal const val EXTRA_IMAGE_URI = "ru.imageella.editorium.IMAGE_URI"
    }

    private val binding by viewBinding(ActivityStartBinding::bind, R.id.rootLayout)

    private var photoURI: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.loadBtn.setOnClickListener {
            getImageFromGallery.launch("image/*")
        }

        binding.photoBtn.setOnClickListener {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val name = "JPEG_${timeStamp}"
            val dir = Environment.DIRECTORY_DCIM + File.separator + "Editorium"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "$name.jpg")
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, dir)
                }
                photoURI =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            } else {
                try {
                    val storageDir = Environment.getExternalStoragePublicDirectory(dir)
                    photoURI = Uri.fromFile(File(storageDir, "$name.jpg"))
                } catch (ex: IOException) {
                    Log.e("DAROVA", "create file", ex)
                }
            }
            photoURI?.let { takePicture.launch(it) }

        }

    }

    private val getImageFromGallery = registerForActivityResult(GetContent()) { uri: Uri? ->
        if (uri != null) {
            openProcessImage(uri)
        }
    }


    private val takePicture = registerForActivityResult(TakePicture()) { success: Boolean ->
        if (success) {
            photoURI?.let { openProcessImage(it) }
        }
    }


    private fun openProcessImage(uri: Uri) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(EXTRA_IMAGE_URI, uri)
        }
        startActivity(intent)
    }

}