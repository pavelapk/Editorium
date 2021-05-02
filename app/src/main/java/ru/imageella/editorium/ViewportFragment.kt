package ru.imageella.editorium

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.core.graphics.scale
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.imageella.editorium.databinding.FragmentViewportBinding
import ru.imageella.editorium.interfaces.Viewport
import ru.imageella.editorium.interfaces.ImageHandler
import kotlin.math.min

class ViewportFragment : Fragment(R.layout.fragment_viewport), Viewport {

    private val binding by viewBinding(FragmentViewportBinding::bind, R.id.rootLayout)

    companion object {
        val TAG: String = ViewportFragment::class.java.simpleName

        fun newInstance() = ViewportFragment()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.post {
            setBitmap((activity as ImageHandler).getBitmap())
        }
    }

    override fun setBitmap(bitmap: Bitmap) {

        val scaleFactor = min(
            bitmap.width.toDouble() / binding.root.width,
            bitmap.height.toDouble() / binding.root.height
        )

        if (scaleFactor > 1) {
            binding.currentImage.setImageBitmap(
                bitmap.scale(
                    (bitmap.width / scaleFactor).toInt(),
                    (bitmap.height / scaleFactor).toInt(),
                    false
                )
            )
        } else {
            binding.currentImage.setImageBitmap(bitmap)
        }

        binding.imgInfoTV.text = "${bitmap.width} x ${bitmap.height} px"
    }


    override fun previewRotate(angle: Float) {
        binding.currentImage.rotation = angle
        binding.previewWarningTV.visibility =
            if (angle % 360 != 0f) View.VISIBLE else View.INVISIBLE
    }


}