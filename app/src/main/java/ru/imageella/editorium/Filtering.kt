package ru.imageella.editorium

import android.graphics.Color
import kotlin.math.min

class Filtering {
    companion object {
        fun doBilinearFilteredPixelColor(
            pic: PixelsWithSizes,
            x: Float,
            y: Float
        ): Int {
            val floorX = x.toInt()
            val floorY = y.toInt()
            val ceilX = (x + 1).toInt()
            val ceilY = (y + 1).toInt()
            val n1 = floorY * pic.w + floorX
            val n2 = floorY * pic.w + min(ceilX, pic.w - 1)
            val n3 = min(ceilY, pic.h - 1) * pic.w + floorX
            val n4 = min(ceilY, pic.h - 1) * pic.w + min(ceilX, pic.w - 1)

            val result = IntArray(4)
            for (c in 0 until 4) {
                val r1 = (getColorChannel(pic.pixels[n1], c) * (ceilX - x) +
                        getColorChannel(pic.pixels[n2], c) * (x - floorX)) * (ceilY - y)
                val r2 = (getColorChannel(pic.pixels[n3], c) * (ceilX - x) +
                        getColorChannel(pic.pixels[n4], c) * (x - floorX)) * (y - floorY)
                result[c] = (r1 + r2).toInt()
            }
            return Color.argb(result[0], result[1], result[2], result[3])
        }

        fun doTrilinearFilteredPixelColor(
            mPic: PixelsWithSizes,
            m2Pic: PixelsWithSizes,
            m: Int,
            k: Float,
            x: Float,
            y: Float
        ): Int {
            val mX = (x / m).toInt().coerceIn(0, mPic.w - 1)
            val mY = (y / m).toInt().coerceIn(0, mPic.h - 1)
            val m2X = (x / m / 2).toInt().coerceIn(0, m2Pic.w - 1)
            val m2Y = (y / m / 2).toInt().coerceIn(0, m2Pic.h - 1)

            val mi = mY * mPic.w + mX
            val m2i = m2Y * m2Pic.w + m2X
            val result = IntArray(4)
            for (c in 0 until 4) {
                result[c] = ((getColorChannel(mPic.pixels[mi], c) * (2 * m - k) +
                        getColorChannel(m2Pic.pixels[m2i], c) * (k - m)) / m).toInt()
            }
            return Color.argb(result[0], result[1], result[2], result[3])
        }

        fun halfSize(pic: PixelsWithSizes): PixelsWithSizes {
            val w = pic.w
            val h = pic.h
            val nw = (w+1) / 2
            val nh = (h+1) / 2
            val newPic = IntArray(nw * nh)
            for (nx in 0 until nw) {
                for (ny in 0 until nh) {
                    val ni = ny * nw + nx
                    newPic[ni] = average4pix(pic, nx, ny)
                }
            }
            return PixelsWithSizes(newPic, nw, nh)
        }

        private fun average4pix(pic: PixelsWithSizes, x: Int, y: Int): Int {
            val p1 = y * 2 * pic.w + x * 2
            val p2 = y * 2 * pic.w + (x * 2 + 1).coerceIn(0, pic.w - 1)
            val p3 = (y * 2 + 1).coerceIn(0, pic.h - 1) * pic.w + x * 2
            val p4 = (y * 2 + 1).coerceIn(0, pic.h - 1) * pic.w + (x * 2 + 1).coerceIn(0, pic.w - 1)
            val result = IntArray(4)
            for (c in 0 until 4) {
                result[c] =
                    (getColorChannel(pic.pixels[p1], c) + getColorChannel(pic.pixels[p2], c) +
                            getColorChannel(pic.pixels[p3], c) + getColorChannel(
                        pic.pixels[p4],
                        c
                    )) / 4
            }
            return Color.argb(result[0], result[1], result[2], result[3])
        }

        private fun getColorChannel(color: Int, channel: Int) =
            when (channel) {
                0 -> Color.alpha(color)
                1 -> Color.red(color)
                2 -> Color.green(color)
                3 -> Color.blue(color)
                else -> 255
            }
    }
}