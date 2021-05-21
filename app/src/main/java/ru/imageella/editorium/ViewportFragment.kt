package ru.imageella.editorium

import android.annotation.SuppressLint
import android.graphics.*
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.scale
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.imageella.editorium.databinding.FragmentViewportBinding
import ru.imageella.editorium.interfaces.ImageHandler
import ru.imageella.editorium.interfaces.Viewport
import ru.imageella.editorium.utils.RotationGestureDetector
import kotlin.math.min

class ViewportFragment : Fragment(R.layout.fragment_viewport), Viewport {

    private val binding by viewBinding(FragmentViewportBinding::bind, R.id.rootLayout)

    companion object {
        val TAG: String = ViewportFragment::class.java.simpleName

        fun newInstance() = ViewportFragment()
    }

    private val canvas = Canvas()
    private val paint = Paint()
    private var overlayBitmap: Bitmap? = null

    private var image: ImageHandler? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        image = activity as? ImageHandler

        binding.root.post {
            image?.let { setBitmap(it.getBitmap()) }
        }

        val rotationDetector = RotationGestureDetector(binding.overlayImage,
            object : RotationGestureDetector.OnRotationGestureListener {
                override fun onRotation(angle: Float) {
                    image?.onImageRotationGesture(angle)
                }

            })

        binding.overlayImage.setOnTouchListener { v, event ->
//            Log.d("DAROVA", "${event.action} - ${event.x}, ${event.y}")
            val lastRotationActive = rotationDetector.isRotationActive
            rotationDetector.onTouchEvent(event)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    image?.onImageClick(event.x, event.y)
                    image?.onImageTouchMove(
                        event.x,
                        event.y,
                        event.x / v.width,
                        event.y / v.height,
                        true
                    )
                    v.performClick()
                }
                MotionEvent.ACTION_MOVE -> if (!rotationDetector.isRotationActive) {
                    image?.onImageTouchMove(
                        event.x,
                        event.y,
                        event.x / v.width,
                        event.y / v.height,
                        lastRotationActive
                    )
                }
            }
            true
        }
    }


    override fun drawPoint(x: Float, y: Float, width: Float, color: Int) {
        paint.strokeWidth = width
        paint.color = color
        canvas.drawPoint(x, y, paint)
    }

    override fun drawLine(x1: Float, y1: Float, x2: Float, y2: Float, width: Float, color: Int) {
        paint.strokeWidth = width
        paint.color = color
        canvas.drawLine(x1, y1, x2, y2, paint)
    }

    override fun drawRect(l: Float, t: Float, r: Float, b: Float, width: Float, color: Int) {
        paint.strokeWidth = width
        paint.color = color
        paint.style = Paint.Style.STROKE
        canvas.drawRect(l, t, r, b, paint)
    }


    override fun drawPath(path: Path, isFill: Boolean, width: Float, color: Int) {
        paint.strokeWidth = width
        paint.color = color
        paint.style = if (isFill) Paint.Style.FILL else Paint.Style.STROKE
        canvas.drawPath(path, paint)
    }

    override fun clearOverlay() {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
    }

    override fun refresh() {
        binding.overlayImage.invalidate()
    }
    override fun drawCanvasToImage() {
        val bmp = image?.getBitmap() ?: return
        val imageCanvas = Canvas(bmp)
        overlayBitmap?.let { imageCanvas.drawBitmap(it, null, Rect(0, 0, bmp.width, bmp.height), null) }
        setBitmap(bmp)
    }

    override fun setBitmap(bitmap: Bitmap) {

        val scaleFactor = min(
            bitmap.width.toDouble() / binding.root.width,
            bitmap.height.toDouble() / binding.root.height
        )

        val size = Pair(
            (bitmap.width / scaleFactor).toInt(),
            (bitmap.height / scaleFactor).toInt()
        )

        binding.currentImage.setImageBitmap(
            bitmap.scale(size.first, size.second, false)
        )

        binding.currentImage.post {
            overlayBitmap = Bitmap.createBitmap(
                binding.currentImage.width,
                binding.currentImage.height,
                Bitmap.Config.ARGB_8888
            )

            canvas.setBitmap(overlayBitmap)
            binding.overlayImage.setImageBitmap(overlayBitmap)
        }

        binding.imgInfoTV.text = "${bitmap.width} x ${bitmap.height} px"
    }


    override fun previewRotate(angle: Float) {
        binding.currentImage.rotation = angle
        binding.previewWarningTV.visibility =
            if (angle % 360 != 0f) View.VISIBLE else View.INVISIBLE
    }

    override fun getOverlaySize() = Pair(binding.overlayImage.width, binding.overlayImage.height)


    override fun previewScale(ratio: Float) {
        binding.currentImage.scaleX = ratio
        binding.currentImage.scaleY = ratio
        binding.previewWarningTV.visibility =
            if (ratio != 1f) View.VISIBLE else View.INVISIBLE
    }

}