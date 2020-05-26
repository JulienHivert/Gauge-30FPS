package com.example.gauge30fps

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import java.util.*
import kotlin.math.abs

class SpeedGaugeView : View {
    private var mPaint: Paint? = null
    var strokeWidth = 0f
    private var mStrokeColor = 0
    private var mRect: RectF? = null
    private var mStartAngle = 0
    private var mSweepAngle = 0
    private var mStartValue = 0
    private var mEndValue = 0
    private var mValue = 0
    private var mPointAngle = 0.0
    private var mPoint = 0
    private var mPointRelative = 0
    private var isActive = false
    private var animThread: Thread? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.SpeedTestGauge, 0, 0)
        // stroke style
        strokeWidth = a.getDimension(R.styleable.SpeedTestGauge_gaugeStrokeWidth, 10f)
        mStrokeColor =
            a.getColor(
                R.styleable.SpeedTestGauge_gaugeStrokeColor,
                ContextCompat.getColor(context, android.R.color.darker_gray)
            )

        // angle start and sweep (opposite direction 0, 270, 180, 90)
        mStartAngle = a.getInt(R.styleable.SpeedTestGauge_gaugeStartAngle, 0)
        mSweepAngle = a.getInt(R.styleable.SpeedTestGauge_gaugeSweepAngle, 360)

        // scale (from mStartValue to mEndValue)
        mStartValue = a.getInt(R.styleable.SpeedTestGauge_gaugeStartValue, 0)
        setEndValue(a.getInt(R.styleable.SpeedTestGauge_gaugeEndValue, 100))

        // calculating one point sweep
        mPointAngle = abs(mSweepAngle).toDouble() / (mEndValue - mStartValue)
        a.recycle()
        init()
    }

    private fun init() {
        mPaint = Paint()
        mPaint!!.color = mStrokeColor
        mPaint!!.strokeWidth = strokeWidth
        mPaint!!.isAntiAlias = true
        mPaint!!.strokeCap = Paint.Cap.BUTT
        mPaint!!.style = Paint.Style.STROKE
        mRect = RectF()
        mValue = mStartValue
        mPoint = mStartAngle
    }

    fun startAnim() {
        if (animThread == null || !animThread!!.isAlive) {
            animThread = object : Thread() {
                override fun run() {
                    super.run()
                    while (!isInterrupted) {
                        try {
                            // 1000/30 = 30FPS
                            sleep(1000 / 30.toLong())
                            postInvalidate()
                        } catch (e1: InterruptedException) {
                            e1.printStackTrace()
                        }
                    }
                }
            }
            (animThread as Thread).start()
        }
    }

    fun stopAnim() {
        if (!animThread!!.isInterrupted) {
            animThread!!.isInterrupted
        }
    }

    private fun initCanvas() {
        val padding = strokeWidth
        val size = (if (width < height) width else height).toFloat()
        val width = size - 2 * padding
        val height = size - 2 * padding
        val radius = if (width < height) width / 2 else height / 2
        val rectLeft = (getWidth() - 2 * padding) / 2 - radius + padding
        val rectTop = (getHeight() - 2 * padding) / 2 - radius + padding
        val rectRight = (getWidth() - 2 * padding) / 2 - radius + padding + width
        val rectBottom =
            (getHeight() - 2 * padding) / 2 - radius + padding + height
        mRect!![rectLeft, rectTop, rectRight] = rectBottom
        mPaint!!.color = mStrokeColor
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        initCanvas()
        if (isActive) {
            mPointRelative =
                (mPointRelative + (mPoint - mPointRelative) * mPointWeight).toInt()
        } else {
            val date = Date()
            val timestamp = date.time.toDouble()
            val now = System.currentTimeMillis().toDouble()
            val elapsed = now - timestamp

            // For empty the gauge, I check the difference between two time
            // If The elapsed time is > 250ms, I empty the gauge quickly
            // else I gradually empty it on 250ms
            if (elapsed > 250) {
                mPointRelative =
                    (mPointRelative + (mPoint - mPoint) * mPointWeight).toInt()
                animThread!!.interrupt()
            } else {
                mPointRelative = (mPoint - mPoint * (elapsed / 250)).toInt()
            }
        }
        canvas.drawArc(mRect!!, mStartAngle.toFloat(), mSweepAngle.toFloat(), false, mPaint!!)
        // Make a perfect gradient of blue
        for (i in 0 until mPointRelative - mStartAngle) {
            val blue = 255 - 270 * i / 255
            mPaint!!.setARGB(255, blue, blue, 255)
            canvas.drawArc(mRect!!, mStartAngle + i.toFloat(), 1f, false, mPaint!!)
        }
    }

    fun setActive(isActive: Boolean) {
        this.isActive = isActive
    }

    var value: Int
        get() = mValue
        set(value) {
            mValue = value
            mPoint = (mStartAngle + (mValue - mStartValue) * mPointAngle).toInt()
        }

    private fun setEndValue(endValue: Int) {
        mEndValue = endValue
        mPointAngle = abs(mSweepAngle).toDouble() / (endValue - mStartValue)
        invalidate()
    }

    companion object {
        // The weight makes the balanced animation not too slow but not too fast either
        private const val mPointWeight = 0.4
    }
}