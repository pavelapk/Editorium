package ru.imageella.editorium

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.imageella.editorium.databinding.ActivityMainBinding
import ru.imageella.editorium.interfaces.Algorithm
import ru.imageella.editorium.interfaces.Viewport
import ru.imageella.editorium.interfaces.ImageHandler
import ru.imageella.editorium.interfaces.ToolSelectListener
import ru.imageella.editorium.tools.RotateFragment
import java.io.InputStream


class MainActivity : AppCompatActivity(R.layout.activity_main), ToolSelectListener, ImageHandler {

    private val binding by viewBinding(ActivityMainBinding::bind, R.id.rootLayout)

    private var lastBitmap: Bitmap? = null
    private lateinit var currentBitmap: Bitmap
    private lateinit var viewport: Viewport

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_image_24)
            title = ""
        }

        val uri = intent.getParcelableExtra<Uri>(StartActivity.EXTRA_IMAGE_URI)

        currentBitmap = if (uri != null) {
            val imageStream: InputStream? = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(imageStream)
        } else {
            Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        }

        val viewportFragment = ViewportFragment.newInstance()

        supportFragmentManager.commit {
            setReorderingAllowed(true)
            add(R.id.viewportFragment, viewportFragment, ViewportFragment.TAG)
            add(R.id.toolsFragment, ToolsFragment.newInstance(), ViewportFragment.TAG)
        }

        viewport = viewportFragment
    }

    private var toolbarMenu: Menu? = null
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        toolbarMenu = menu
        return true
    }

    private var isToolActive = false

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            if (isToolActive) {
                previewRotate(0f)
                lastBitmap?.let { setBitmap(it) }
                closeTool()
            } else {
//                val intent = Intent(this, StartActivity::class.java)
//                startActivity(intent)
                finish()
            }
            true
        }

        R.id.actionSave -> {
            saveFile.launch("the most beautiful photo card.jpg")
            true
        }

        R.id.actionDone -> {
            (supportFragmentManager.findFragmentById(R.id.toolsFragment) as Algorithm).doAlgorithm()
            closeTool()
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    class SaveImage : CreateDocument() {
        override fun createIntent(context: Context, input: String): Intent {
            return super.createIntent(context, input).apply {
                type = "image/jpeg"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
        }
    }

    private val saveFile = registerForActivityResult(SaveImage()) { uri: Uri? ->
        if (uri != null) {
            try {
                contentResolver.openOutputStream(uri).use {
                    currentBitmap.compress(
                        Bitmap.CompressFormat.JPEG,
                        95,
                        it
                    )
                }
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "something went wrong" + e.message, Toast.LENGTH_LONG)
                    .show()
                e.printStackTrace()
            }
        }
    }


    private fun closeTool() {
        isToolActive = false
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_image_24)
        toolbarMenu?.apply {
            setGroupVisible(R.id.mainActionsGroup, true)
            setGroupVisible(R.id.toolActionsGroup, false)
        }
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.toolsFragment, ToolsFragment.newInstance(), ToolsFragment.TAG)
        }
    }

    override fun onToolClick() {
        lastBitmap = currentBitmap.copy(currentBitmap.config, false)

        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_cancel_24)
        toolbarMenu?.apply {
            setGroupVisible(R.id.mainActionsGroup, false)
            setGroupVisible(R.id.toolActionsGroup, true)
        }
        isToolActive = true

        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.toolsFragment, RotateFragment.newInstance(), RotateFragment.TAG)
        }
    }

    override fun setBitmap(bitmap: Bitmap) {
        currentBitmap = bitmap
        viewport.setBitmap(currentBitmap)
    }

    override fun getBitmap() = currentBitmap

    override fun previewRotate(angle: Float) {
        viewport.previewRotate(angle)
    }

}