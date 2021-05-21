package ru.imageella.editorium

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Path
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.imageella.editorium.databinding.ActivityMainBinding
import ru.imageella.editorium.interfaces.Algorithm
import ru.imageella.editorium.interfaces.ImageHandler
import ru.imageella.editorium.interfaces.ToolSelectListener
import ru.imageella.editorium.interfaces.Viewport
import ru.imageella.editorium.tools.*
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
            BitmapFactory.decodeStream(imageStream).copy(Bitmap.Config.ARGB_8888, true)
        } else {
            Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        }

        val viewportFragment = ViewportFragment.newInstance()

        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.viewportFragment, viewportFragment, ViewportFragment.TAG)
            replace(R.id.toolsFragment, ToolsFragment.newInstance(), ViewportFragment.TAG)
        }

        viewport = viewportFragment
    }

    private var toolbarMenu: Menu? = null
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        toolbarMenu = menu
        return true
    }

    private fun getCurrentToolFragment() =
        (supportFragmentManager.findFragmentById(R.id.toolsFragment) as? Algorithm)

    private var isToolActive = false

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            if (isToolActive) {
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
//            getCurrentToolFragment()?.doAlgorithm()
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
        previewScale(1f)
        previewRotate(0f)
        progressIndicator(null, false)
        clearOverlay()
        refresh()
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

    override fun onToolClick(taskNum: Int) {
        lastBitmap = currentBitmap.copy(currentBitmap.config, false)

        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_cancel_24)
        toolbarMenu?.apply {
            setGroupVisible(R.id.mainActionsGroup, false)
            setGroupVisible(R.id.toolActionsGroup, true)
        }
        isToolActive = true

        supportFragmentManager.commit {
            setReorderingAllowed(true)
            when (taskNum) {
                1 -> replace(R.id.toolsFragment, RotateFragment.newInstance(), RotateFragment.TAG)
                2 -> replace(R.id.toolsFragment, FiltersFragment.newInstance(), FiltersFragment.TAG)
                3 -> replace(R.id.toolsFragment, ScaleFragment.newInstance(), ScaleFragment.TAG)
                4 -> replace(R.id.toolsFragment, FaceFragment.newInstance(), FaceFragment.TAG)
                6 -> replace(
                    R.id.toolsFragment,
                    RetouchingFragment.newInstance(),
                    RetouchingFragment.TAG
                )
                7 -> replace(
                    R.id.toolsFragment,
                    UnsharpMaskingFragment.newInstance(),
                    UnsharpMaskingFragment.TAG
                )
                8 -> replace(R.id.toolsFragment, AffineFragment.newInstance(), AffineFragment.TAG)
                9 -> replace(R.id.toolsFragment, CubeFragment.newInstance(), CubeFragment.TAG)
                5 -> replace(R.id.toolsFragment, SplineFragment.newInstance(), SplineFragment.TAG)
            }

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

    override fun previewScale(ratio: Float) {
        viewport.previewScale(ratio)
    }

    override fun getOverlaySize() = viewport.getOverlaySize()

    override fun getLastBitmap() = lastBitmap ?: currentBitmap

    private fun disableAllViews(layout: ViewGroup, isEnabled: Boolean) {
        layout.isEnabled = isEnabled
        for (i in 0 until layout.childCount) {
            layout.getChildAt(i).isEnabled = isEnabled
        }
    }

    override fun progressIndicator(toolLayout: ViewGroup?, isEnabled: Boolean) {
        binding.progressIndicator.visibility = if (isEnabled) View.VISIBLE else View.INVISIBLE
        binding.progressBar.visibility = if (isEnabled) View.VISIBLE else View.INVISIBLE
        toolLayout?.let { disableAllViews(it, !isEnabled) }
    }

    override fun onImageClick(x: Float, y: Float) {
        getCurrentToolFragment()?.onImageClick(x, y)
    }

    override fun onImageTouchMove(xRaw:Float, yRaw:Float, x: Float, y: Float, isStart: Boolean) {
        getCurrentToolFragment()?.onImageTouchMove(xRaw, yRaw, x, y, isStart)
    }

    override fun onImageRotationGesture(angle: Float) {
        getCurrentToolFragment()?.onImageRotationGesture(angle)
    }

    override fun drawPoint(x: Float, y: Float, width: Float, color: Int) {
        viewport.drawPoint(x, y, width, color)
    }

    override fun drawLine(x1: Float, y1: Float, x2: Float, y2: Float, width: Float, color: Int) {
        viewport.drawLine(x1, y1, x2, y2, width, color)
    }

    override fun drawRect(l: Float, t: Float, r: Float, b: Float, width: Float, color: Int) {
        viewport.drawRect(l, t, r, b, width, color)
    }

    override fun drawPath(path: Path, isFill: Boolean, width: Float, color: Int) {
        viewport.drawPath(path, isFill, width, color)
    }

    override fun clearOverlay() {
        viewport.clearOverlay()
    }

    override fun refresh() {
        viewport.refresh()
    }
    override fun drawCanvasToImage(){
        viewport.drawCanvasToImage()
    }

}