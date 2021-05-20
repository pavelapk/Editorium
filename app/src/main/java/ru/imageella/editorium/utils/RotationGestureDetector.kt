package ru.imageella.editorium.utils

import android.graphics.PointF
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2


class RotationGestureDetector(v: View, private val mListener: OnRotationGestureListener) {
    private val mFPoint = PointF()
    private val mSPoint = PointF()
    private var mPtrID1 = INVALID_POINTER_ID
    private var mPtrID2 = INVALID_POINTER_ID
    private var lastAngle = 0f
    private val mView = v
    var isRotationActive = false

    companion object {
        private const val INVALID_POINTER_ID = -1
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
//                Log.v("DAROVA", "ACTION_DOWN")
                mPtrID1 = event.getPointerId(event.actionIndex)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
//                Log.v("DAROVA", "ACTION_POINTER_DOWN")
                mPtrID2 = event.getPointerId(event.actionIndex)
                getRawPoint(event, mPtrID1, mSPoint)
                getRawPoint(event, mPtrID2, mFPoint)
                lastAngle = 0f
            }
            MotionEvent.ACTION_MOVE -> {
//                Log.v("DAROVA", "ACTION_MOVE: $mPtrID1, $mPtrID2")
                if (mPtrID1 != INVALID_POINTER_ID && mPtrID2 != INVALID_POINTER_ID) {
                    isRotationActive = true
                    val nfPoint = PointF()
                    val nsPoint = PointF()
                    getRawPoint(event, mPtrID1, nsPoint)
                    getRawPoint(event, mPtrID2, nfPoint)
                    val angle = angleBetweenLines(mFPoint, mSPoint, nfPoint, nsPoint)
                    mListener.onRotation(angle - lastAngle)
                    lastAngle = angle
                } else {
                    isRotationActive = false
                }
            }
            MotionEvent.ACTION_UP -> mPtrID1 = INVALID_POINTER_ID
            MotionEvent.ACTION_POINTER_UP -> mPtrID2 = INVALID_POINTER_ID
            MotionEvent.ACTION_CANCEL -> {
                mPtrID1 = INVALID_POINTER_ID
                mPtrID2 = INVALID_POINTER_ID
            }
            else -> {
            }
        }
        return true
    }

    private fun getRawPoint(ev: MotionEvent, index: Int, point: PointF) {
        val location = intArrayOf(0, 0)
        mView.getLocationOnScreen(location)
        val x = ev.getX(index) + location[0]
        val y = ev.getY(index) + location[1]
        point.set(x, y)
    }

    private fun angleBetweenLines(
        fPoint: PointF,
        sPoint: PointF,
        nFpoint: PointF,
        nSpoint: PointF
    ): Float {
        val angle1 = atan2((fPoint.y - sPoint.y).toDouble(), (fPoint.x - sPoint.x).toDouble())
            .toFloat()
        val angle2 =
            atan2((nFpoint.y - nSpoint.y).toDouble(), (nFpoint.x - nSpoint.x).toDouble())
                .toFloat()
        return angle2 - angle1

    }

    interface OnRotationGestureListener {
        fun onRotation(angle: Float)
    }

}