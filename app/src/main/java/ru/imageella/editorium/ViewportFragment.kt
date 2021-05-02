package ru.imageella.editorium

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.imageella.editorium.databinding.FragmentCanvasBinding
import ru.imageella.editorium.interfaces.Viewport
import ru.imageella.editorium.interfaces.ImageHandler

class ViewportFragment : Fragment(R.layout.fragment_viewport), Viewport {

    private val binding by viewBinding(FragmentCanvasBinding::bind, R.id.rootLayout)

    companion object {
        val TAG: String = ViewportFragment::class.java.simpleName

        fun newInstance() = ViewportFragment()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setBitmap((activity as ImageHandler).getBitmap())
    }

    override fun setBitmap(bitmap: Bitmap) {
        binding.currentImage.setImageBitmap(bitmap)
        binding.imgInfoTV.text = "${bitmap.width} x ${bitmap.height} px"
    }

    override fun previewRotate(angle: Float) {
        binding.currentImage.rotation = angle
        binding.previewWarningTV.visibility =
            if (angle % 360 != 0f) View.VISIBLE else View.INVISIBLE
    }


}