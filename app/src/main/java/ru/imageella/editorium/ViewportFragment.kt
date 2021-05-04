package ru.imageella.editorium

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.view.drawToBitmap
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.imageella.editorium.databinding.FragmentViewportBinding
import ru.imageella.editorium.interfaces.ImageHandler
import ru.imageella.editorium.interfaces.Viewport


class ViewportFragment : Fragment(R.layout.fragment_viewport), Viewport {

    private val binding by viewBinding(FragmentViewportBinding::bind, R.id.rootLayout)
//    private val checkImage = binding.currentImage
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

    override fun previewScale(ratio: Float) {
        val checkImage = binding.currentImage.drawable
//        val bmHalf = Bitmap.createScaledBitmap(
//            binding.currentImage.drawToBitmap(),
//            (binding.currentImage.width * ratio).toInt(),
//            (binding.currentImage.height * ratio).toInt(),
//            false
//        )
        binding.currentImage.scaleX = ratio
        binding.currentImage.scaleY = ratio
        binding.previewWarningTV.visibility =
            if (ratio == 1f) View.VISIBLE else View.INVISIBLE
//        binding.currentImage.setImageBitmap(bmHalf)
//        binding.currentImage.setImageDrawable(checkImage);
    }


}