/*
package ru.imageella.editorium

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
//import kotlinx.android.synthetic.main.activity_process_image.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.Math.exp

class ProcessImageActivity : AppCompatActivity() {


    private lateinit var currentBitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_process_image)

        val uri = intent.getParcelableExtra<Uri>(EXTRA_IMAGE_URI)

        if (uri != null) {
            val imageStream: InputStream? = contentResolver.openInputStream(uri)
            currentBitmap = BitmapFactory.decodeStream(imageStream)
            currentImage.setImageBitmap(currentBitmap)
        }
        saveBtn.setOnClickListener {
            val sd = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val dest = File(sd, "my_pic.jpg")
            try {
                FileOutputStream(dest).use { out ->
                    currentBitmap.compress(
                        Bitmap.CompressFormat.JPEG,
                        95,
                        out
                    )
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        filter1.setOnClickListener {
            val w = currentBitmap.width
            val h = currentBitmap.height
            val pixels = IntArray(w * h)
            currentBitmap.getPixels(pixels, 0, w, 0, 0, w, h)

            val newBitmap: Bitmap = currentBitmap.copy(Bitmap.Config.ARGB_8888, true)

            for(x in 0 until w){
                for(y in 0 until h){
                    val oldPix: Int = currentBitmap.getPixel(x, y)
                    val oldRed: Int = Color.red(oldPix)
                    val oldBlue: Int = Color.blue(oldPix)
                    val oldGreen: Int = Color.green(oldPix)
                    val oldAlpha: Int = Color.alpha(oldPix)


                    //сепия почти
                    var newGreen: Int = (0.25 * oldRed + 0.65 * oldGreen + 0.18 * oldBlue).toInt()
                    var newRed: Int = (0.3 * oldRed + 0.68 * oldGreen + 0.25 * oldBlue).toInt()
                    var newBlue: Int = (0.18 * oldRed + 0.5 * oldGreen + 0.1 * oldBlue).toInt()
                    if (newRed > 255){
                        newRed = 255
                    }
                    if (newBlue > 255){
                        newBlue = 255
                    }
                    if (newGreen > 255){
                        newGreen = 255
                    }

                    val newPixel: Int = Color.argb(oldAlpha, newRed, newGreen, newBlue)
                    newBitmap.setPixel(x, y, newPixel);
                }
            }
            currentImage.setImageBitmap(newBitmap)
        }
        filter2.setOnClickListener {
            val w = currentBitmap.width
            val h = currentBitmap.height
            val pixels = IntArray(w * h)
            currentBitmap.getPixels(pixels, 0, w, 0, 0, w, h)

            val cmData = floatArrayOf(1f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 0f, 1f, 0f)
            val newBitmap: Bitmap = currentBitmap.copy(Bitmap.Config.ARGB_8888, true)

            for(x in 0 until w){
                for(y in 0 until h){
                    val oldPix: Int = currentBitmap.getPixel(x, y)
                    val oldRed: Int = Color.red(oldPix)
                    val oldBlue: Int = Color.blue(oldPix)
                    val oldGreen: Int = Color.green(oldPix)
                    val oldAlpha: Int = Color.alpha(oldPix)

                    //фильтр чб
                    val intensity: Int = (oldBlue + oldGreen + oldRed)/3
                    val newRed: Int = intensity
                    val newBlue: Int = intensity
                    val newGreen: Int = intensity

                    val newPixel: Int = Color.argb(oldAlpha, newRed, newGreen, newBlue)
                    newBitmap.setPixel(x, y, newPixel);
                }
            }
            currentImage.setImageBitmap(newBitmap)
        }
        filter3.setOnClickListener {
            val w = currentBitmap.width
            val h = currentBitmap.height
            val pixels = IntArray(w * h)
            currentBitmap.getPixels(pixels, 0, w, 0, 0, w, h)

            val newBitmap: Bitmap = currentBitmap.copy(Bitmap.Config.ARGB_8888, true)

            for(x in 0 until w){
                for(y in 0 until h){
                    val oldPix: Int = currentBitmap.getPixel(x, y)
                    val oldRed: Int = Color.red(oldPix)
                    val oldBlue: Int = Color.blue(oldPix)
                    val oldGreen: Int = Color.green(oldPix)
                    val oldAlpha: Int = Color.alpha(oldPix)

                    //набросок
                    */
/* val newPixel: Int
                     val intensity: Int = (oldBlue + oldGreen + oldRed)/3
                     val intKoef: Int = 120
                     if(intensity > intKoef){
                       newPixel = Color.argb(oldAlpha, 255,255, 255)
                     }
                     else if (intensity > 90){
                       newPixel = Color.argb(oldAlpha, 150, 150, 150)
                     }
                     else{
                       newPixel = Color.argb(oldAlpha, 0, 0, 0)
                     }*//*


                    //негатив
                    val newRed: Int = 255 - oldRed
                    val newBlue: Int = 255 - oldBlue
                    val newGreen: Int = 255 - oldGreen

                    val newPixel: Int = Color.argb(oldAlpha, newRed, newGreen, newBlue)
                    newBitmap.setPixel(x, y, newPixel);
                }
            }
            currentImage.setImageBitmap(newBitmap)
        }
        editBtn.setOnClickListener {
            val w = currentBitmap.width
            val h = currentBitmap.height
            val pixels = IntArray(w * h)
            currentBitmap.getPixels(pixels, 0, w, 0, 0, w, h)


            val newBitmap: Bitmap = currentBitmap.copy(Bitmap.Config.ARGB_8888, true)
*/
/*
            for(x in 0 until w) {
                for (y in 0 until h) {
                    val pix: Int = currentBitmap.getPixel(x, y)
                    var red: Int = 0
                    var blue: Int = 0
                    var green: Int = 0
                    var alpha: Int = Color.alpha(pix)

                    val k1 = if(x - 6 < 0){
                        0
                    }
                    else{
                        x - 6
                    }
                   val l1 =  if (y - 6 < 0){
                         0
                    } else {
                         y - 6
                    }
                    val k2 = if(x + 6 >= w){
                        w - 1
                    }
                    else{
                        x + 6
                    }
                    val l2 = if (y + 6 >= h){
                        h - 1
                    } else {
                        y + 6
                    }
                    var sum = 0
                    for (i in k1 until k2){
                        for (j in l1 until l2){
                            val oldPix: Int = currentBitmap.getPixel(i, j)
                            val oldRed: Int = Color.red(oldPix)
                            val oldBlue: Int = Color.blue(oldPix)
                            val oldGreen: Int = Color.green(oldPix)
                            sum++

                            red += oldRed
                            blue += oldBlue
                            green += oldGreen
                        }
                    }

                    val newRed: Int = (red/sum).toInt()
                    val newBlue: Int = (blue/sum).toInt()
                    val newGreen: Int = (green/sum).toInt()
                    val newPixel: Int = Color.argb(alpha, newRed, newGreen, newBlue)
                    newBitmap.setPixel(x, y, newPixel);
                }
            }*//*


            val sigma = 3.0
            val sig2 = 2*sigma*sigma
            val sizeWin = (3*sigma).toInt()
            val window = DoubleArray(2*sizeWin + 1)
            var fl = 1
            window[sizeWin] = 1.0
            for(i in sizeWin - 1 downTo  0){
                window[i] = kotlin.math.exp(-fl * fl / sig2)
                window[2*sizeWin - i] = window[i]
                fl++
            }

            for (y in 0 until h){
                for (x in 0 until w){
                    var sum: Double = 0.0
                    var red: Double = 0.0
                    var blue:  Double = 0.0
                    var green: Double = 0.0
                    val pix: Int = currentBitmap.getPixel(x, y)
                    var alpha: Int = Color.alpha(pix)

                    for (k in 0 until 2*sizeWin + 1){
                        val l = x + k - sizeWin // РАЗОБРАТЬСЯ
                        if ((l >= 0) && (l < w)){
                            val helpPix: Int = currentBitmap.getPixel(l, y)
                            red += Color.red(helpPix)*window[k]
                            green += Color.green(helpPix)*window[k]
                            blue += Color.blue(helpPix)*window[k]
                            sum += window[k]
                        }
                    }
                    val newRed: Int = (red/sum).toInt()
                    val newBlue: Int = (blue/sum).toInt()
                    val newGreen: Int = (green/sum).toInt()
                    val newPixel: Int = Color.argb(alpha, newRed, newGreen, newBlue)
                    newBitmap.setPixel(x, y, newPixel);
                }
            }
            val newBitmap2: Bitmap = newBitmap.copy(Bitmap.Config.ARGB_8888, true)

            for (x in 0 until w){
                for (y in 0 until h){
                    var sum: Double = 0.0
                    var red:  Double = 0.0
                    var blue:  Double = 0.0
                    var green:  Double = 0.0
                    val pix: Int = newBitmap2.getPixel(x, y)
                    var alpha: Int = Color.alpha(pix)

                    for (k in 0 until 2*sizeWin + 1){
                        val l = y + k - sizeWin
                        if ((l >=0) && (l < h)){
                            val helpPix: Int = newBitmap2.getPixel(x, l)
                            red += Color.red(helpPix)*window[k]
                            green += Color.green(helpPix)*window[k]
                            blue += Color.blue(helpPix)*window[k]
                            sum += window[k]
                        }
                    }
                    val newRed: Int = (red/sum).toInt()
                    val newBlue: Int = (blue/sum).toInt()
                    val newGreen: Int = (green/sum).toInt()
                    val newPixel: Int = Color.argb(alpha, newRed, newGreen, newBlue)
                    newBitmap.setPixel(x, y, newPixel);
                }
            }

            for (x in 0 until w){
                for (y in 0 until h){
                    val blurPix: Int = newBitmap.getPixel(x, y)
                    val origPix: Int = currentBitmap.getPixel(x, y)
                    val maskRed: Int = Color.red(origPix) - Color.red(blurPix)
                    val maskBlue: Int = Color.blue(origPix) - Color.blue(blurPix)
                    val maskGreen: Int = Color.green(origPix) - Color.green(blurPix)
                    val alpha: Int = Color.alpha(origPix)



                    var newRed: Int = Color.red(origPix) + maskRed
                    var newBlue: Int = Color.blue(origPix) + maskBlue
                    var newGreen: Int = Color.green(origPix) + maskGreen

                    if (newRed > 255) {
                        newRed = 255
                    } else if (newRed < 0){
                        newRed = 0
                    }
                    if (newBlue > 255){
                        newBlue = 255
                    } else if (newBlue < 0){
                        newBlue = 0
                    }
                    if (newGreen > 255){
                        newGreen = 255
                    } else if (newGreen < 0){
                        newGreen = 0
                    }

                    val maskPix: Int = Color.argb(alpha, newRed, newGreen, newBlue)
                    newBitmap.setPixel(x, y, maskPix)
                }
            }
            currentImage.setImageBitmap(newBitmap)
        }
    }


}*/
