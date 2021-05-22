package ru.imageella.editorium

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
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

    private val writePermission = registerForActivityResult(RequestPermission()) { granted ->
        when {
            granted -> {
                takePicturePrepare()
            }
            !shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                Toast.makeText(
                    this,
                    getString(R.string.writePermissionRationaleBlocked),
                    Toast.LENGTH_LONG
                ).show()
            }
            else -> {
                Toast.makeText(
                    this,
                    getString(R.string.writePermissionRationale),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.loadBtn.setOnClickListener {
            getImageFromGallery.launch("image/*")
        }

        binding.photoBtn.setOnClickListener {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(
                        this,
                        getString(R.string.writePermissionRationale),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                writePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            } else {
                takePicturePrepare()
            }
        }

    }

    private fun takePicturePrepare() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val name = "JPEG_${timeStamp}"
        val dir = Environment.DIRECTORY_DCIM
        val appDirName = "Editorium"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "$name.jpg")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, dir + File.separator + appDirName)
            }
            photoURI =
                resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        } else {
            try {
                val storageDir = Environment.getExternalStoragePublicDirectory(dir)
                val appDir = File(storageDir, appDirName)
                if (!appDir.exists()) {
                    if (!appDir.mkdirs()) {
                        Log.d("DAROVA", "failed to create directory")
                    }
                }
                photoURI = FileProvider.getUriForFile(
                    this,
                    "ru.imageella.fileprovider",
                    File(appDir, "$name.jpg")
                )
            } catch (ex: IOException) {
                Log.e("DAROVA", "create file", ex)
            }
        }
        photoURI?.let { takePicture.launch(it) }
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.data = uri
            sendBroadcast(mediaScanIntent)
        }
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(EXTRA_IMAGE_URI, uri)
        }
        startActivity(intent)
    }

}