package ru.imageella.editorium

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.imageella.editorium.databinding.ActivityStartBinding
import java.io.File
import java.io.IOException
import java.text.DateFormat
import java.util.*


class StartActivity : AppCompatActivity(R.layout.activity_start) {

    companion object {
        internal const val EXTRA_IMAGE_URI = "ru.imageella.editorium.IMAGE_URI"
    }

    private val binding by viewBinding(ActivityStartBinding::bind, R.id.rootLayout)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.loadBtn.setOnClickListener {
            getImageFromGallery.launch("image/*")
        }

        binding.photoBtn.setOnClickListener {
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
                takePicture.launch(photoURI)
            }
        }

    }

    private val getImageFromGallery = registerForActivityResult(GetContent()) { uri: Uri? ->
        if (uri != null) {
            openProcessImage(uri)
        }
    }

    private val takePicture = registerForActivityResult(TakePicture()) { success: Boolean ->
        if (success) {
            openProcessImage(File(currentPhotoPath).toUri())
        }
    }


    private fun openProcessImage(uri: Uri) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(EXTRA_IMAGE_URI, uri)
        }
        startActivity(intent)
    }

    private lateinit var currentPhotoPath: String


    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = DateFormat.getDateTimeInstance().format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }


}