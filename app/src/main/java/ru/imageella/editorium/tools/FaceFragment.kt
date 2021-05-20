package ru.imageella.editorium.tools

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import ru.imageella.editorium.R
import ru.imageella.editorium.databinding.FragmentFaceToolBinding
import ru.imageella.editorium.interfaces.Algorithm
import ru.imageella.editorium.interfaces.ImageHandler
import java.io.File


class FaceFragment : Fragment(R.layout.fragment_face_tool), Algorithm {

    private val binding by viewBinding(FragmentFaceToolBinding::bind, R.id.rootLayout)

    companion object {
        val TAG: String = FaceFragment::class.java.simpleName

        fun newInstance() = FaceFragment()
    }

    private lateinit var image: ImageHandler

    private val mLoaderCallback = object : BaseLoaderCallback(context) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                SUCCESS -> {
                    Log.i("DAROVA", "OpenCV loaded successfully")
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        image = activity as ImageHandler

        binding.findBtn.setOnClickListener { doAlgorithm() }
    }

    private fun loadHaarCascadeFile(): File {
        try {
            val cascadeDir = context?.getDir("haarcascade_frontalface", Context.MODE_PRIVATE)
            val cascadeFile = File(cascadeDir, "haarcascade_frontalface_alt2.xml")
            if (!cascadeFile.exists()) {
                val inputStream = resources.openRawResource(R.raw.haarcascade_frontalface_alt2)
                cascadeFile.outputStream().use { inputStream.copyTo(it) }
            }
            return cascadeFile
        } catch (throwable: Throwable) {
            throw RuntimeException("Failed to load Haar Cascade file")
        }
    }

    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.d(
                "DAROVA",
                "Internal OpenCV library not found. Using OpenCV Manager for initialization"
            )
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, context, mLoaderCallback)
        } else {
            Log.d("DAROVA", "OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    private fun doAlgorithm() {
        val bmp = image.getBitmap()
        val mat = Mat(bmp.width, bmp.height, CvType.CV_8UC4)
        Utils.bitmapToMat(bmp, mat)

        val faceCascade = CascadeClassifier(loadHaarCascadeFile().absolutePath)
        val matOfRect = MatOfRect()
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2GRAY)
        faceCascade.detectMultiScale(
            mat,
            matOfRect,
            1.05,
            4,
            0,
            Size(bmp.width / 20.0, bmp.height / 20.0)
        )
        val rects = matOfRect.toList()
//
//        for (rect in rects) {
//            Imgproc.rectangle(mat, rect, Scalar(0.0, 0.0, 255.0),)
//        }

        val (w, h) = image.getOverlaySize()
        for (rect in rects) {
            val l = rect.tl().x / mat.width() * w
            val t = rect.tl().y / mat.height() * h
            val r = rect.br().x / mat.width() * w
            val b = rect.br().y / mat.height() * h
            image.drawRect(l.toFloat(), t.toFloat(), r.toFloat(), b.toFloat(), 10f, Color.RED)
        }
        image.refresh()
//        Utils.matToBitmap(mat, bmp)
//        image.setBitmap(bmp)
    }
}