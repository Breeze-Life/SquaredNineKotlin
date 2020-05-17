package com.example.kotlin.squaredgongge

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT

class LockPatternViewKotlin : View {

    // 二维数组初始化，int[3][3]
    private var mPoints: Array<Array<Point?>> = Array(3) { Array<Point?>(3, { null }) }

    // 是否初始化，确保只初始化一次
    private var mIsInit = false

    //外圆半径
    private var mDotRadius = 0

    // 画笔
    private lateinit var mLinePaint: Paint
    private lateinit var mPressedPaint: Paint
    private lateinit var mErrorPaint: Paint
    private lateinit var mNormalPaint: Paint
    private lateinit var mArrowPaint: Paint

    //颜色
    private val mOuterPressedColor = 0xff8cbad8.toInt()
    private val mInnerPressedColor = 0xff0596f6.toInt()
    private val mOuterNormalColor = 0xffd9d9d9.toInt()
    private val mInnerNormalColor = 0xff929292.toInt()
    private val mOuterErrorColor = 0xff901032.toInt()
    private val mInnerErrorColor = 0xffea0945.toInt()

    //按下的时候是否在一个宫格上
    private var mIsTouchPoint = false

    //选中的所有点
    private var mSelectPoints = ArrayList<Point>()

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onDraw(canvas: Canvas) {
        //初始化九宫格，onDraw多次调用
        if (!mIsInit) {
            initDot()
            initPaint()
            mIsInit = true
        }

        //绘制九个宫格
        drawShow(canvas)
    }

    /**
     * //绘制两点之间的连线以及箭头
     */
    private fun drawLines(canvas: Canvas) {
        if (mSelectPoints.size >= 1) {
            //两点之间需要绘制一条直线和箭头
            var lastPoint = mSelectPoints[0]

            for (point in mSelectPoints) {
                //两点之间绘制一条直线   sin   cos   数学
                drawLine(lastPoint, point, canvas, mLinePaint)
                //两点之间绘制一个箭头
                drawArrow(canvas, mArrowPaint, lastPoint, point, (mDotRadius / 4).toFloat(), 38)
                lastPoint = point
            }
        }
    }

    /**
     * 箭头
     */
    private fun drawArrow(canvas: Canvas, paint: Paint, start: Point, end: Point, arrowHeight: Float, angle: Int) {
        val d = MathUtil.distance(start.centerX.toDouble(), start.centerY.toDouble(), end.centerX.toDouble(), end.centerY.toDouble())
        val sin_B = ((end.centerX - start.centerX) / d).toFloat()
        val cos_B = ((end.centerY - start.centerY) / d).toFloat()
        val tan_A = Math.tan(Math.toRadians(angle.toDouble())).toFloat()
        val h = (d - arrowHeight.toDouble() - mDotRadius * 1.1).toFloat()
        val l = arrowHeight * tan_A
        val a = l * sin_B
        val b = l * cos_B
        val x0 = h * sin_B
        val y0 = h * cos_B
        val x1 = start.centerX + (h + arrowHeight) * sin_B
        val y1 = start.centerY + (h + arrowHeight) * cos_B
        val x2 = start.centerX + x0 - b
        val y2 = start.centerY.toFloat() + y0 + a
        val x3 = start.centerX.toFloat() + x0 + b
        val y3 = start.centerY + y0 - a
        val path = Path()
        path.moveTo(x1, y1)
        path.lineTo(x2, y2)
        path.lineTo(x3, y3)
        path.close()
        canvas.drawPath(path, paint)
    }

    /**
     * 画线
     */
    private fun drawLine(start: Point, end: Point, canvas: Canvas, paint: Paint) {
        val pointDistance = MathUtil.distance(start.centerX.toDouble(), start.centerY.toDouble(), end.centerX.toDouble(), end.centerY.toDouble())

        var dx = end.centerX - start.centerX
        var dy = end.centerY - start.centerY

        val rx = (dx / pointDistance * (mDotRadius / 6.0)).toFloat()
        val ry = (dy / pointDistance * (mDotRadius / 6.0)).toFloat()
        canvas.drawLine(start.centerX + rx, start.centerY + ry, end.centerX - rx, end.centerY - ry, paint)
    }

    /**
     * 初始化绘制
     */
    private fun drawShow(canvas: Canvas) {
        for (i in 0..2) {
            for (point in mPoints[i]) {
                //默认状态
                if (point!!.statusIsNormal()) {
                    //先绘制外圆
                    mNormalPaint.color = mOuterNormalColor
                    canvas.drawCircle(point.centerX.toFloat(), point.centerY.toFloat(), mDotRadius.toFloat(), mNormalPaint)

                    //后绘制内圆
                    mNormalPaint.color = mInnerNormalColor
                    canvas.drawCircle(point.centerX.toFloat(), point.centerY.toFloat(), mDotRadius.toFloat() / 6, mNormalPaint)
                }
                //选中状态
                if (point.statusIsPressed()) {
                    //先绘制外圆
                    mNormalPaint.color = mOuterPressedColor
                    canvas.drawCircle(point.centerX.toFloat(), point.centerY.toFloat(), mDotRadius.toFloat(), mPressedPaint)

                    //后绘制内圆
                    mNormalPaint.color = mInnerPressedColor
                    canvas.drawCircle(point.centerX.toFloat(), point.centerY.toFloat(), mDotRadius.toFloat() / 6, mPressedPaint)
                }
                //错误状态
                if (point.statusIsError()) {
                    //先绘制外圆
                    mNormalPaint.color = mOuterErrorColor
                    canvas.drawCircle(point.centerX.toFloat(), point.centerY.toFloat(), mDotRadius.toFloat(), mErrorPaint)

                    //后绘制内圆
                    mNormalPaint.color = mInnerErrorColor
                    canvas.drawCircle(point.centerX.toFloat(), point.centerY.toFloat(), mDotRadius.toFloat() / 6, mErrorPaint)
                }

            }
        }

        //绘制两点之间的连线以及箭头
        drawLines(canvas)
    }

    /**
     * 初始化画笔
     * 3个点状态的画笔、线的画笔、线上的箭头画笔
     */
    private fun initPaint() {
        //线的画笔
        mLinePaint = Paint()
        mLinePaint.color = mInnerPressedColor
        mLinePaint.style = Paint.Style.STROKE
        mLinePaint.isAntiAlias = true
        mLinePaint.strokeWidth = (mDotRadius / 9).toFloat()

        //按下的画笔
        mPressedPaint = Paint()
        mPressedPaint.style = Paint.Style.STROKE
        mPressedPaint.isAntiAlias = true
        mPressedPaint.strokeWidth = (mDotRadius / 6).toFloat()

        // 错误的画笔
        mErrorPaint = Paint()
        mErrorPaint.style = Paint.Style.STROKE
        mErrorPaint.isAntiAlias = true
        mErrorPaint.strokeWidth = (mDotRadius / 6).toFloat()

        // 默认的画笔
        mNormalPaint = Paint()
        mNormalPaint.style = Paint.Style.STROKE
        mNormalPaint.isAntiAlias = true
        mNormalPaint.strokeWidth = (mDotRadius / 9).toFloat()

        // 箭头的画笔
        mArrowPaint = Paint()
        mArrowPaint.color = mInnerPressedColor
        mArrowPaint.style = Paint.Style.FILL
        mArrowPaint.isAntiAlias = true
    }

    /**
     * 初始化点
     */
    private fun initDot() {
        //九个宫格，保存到集合， Point[3][3]
        //不断绘制的时候，这九个点都有状态，而且需要回调密码，所以点需要有下标
        //计算中心位置
        var width = this.width
        var height = this.height


        //兼容横竖屏
        var offsetX = 0
        var offsetY = 0
        if (height > width) {
            offsetY = (height - width) / 2
            height = width
        } else {
            offsetX = (width - height) / 2
            width = height
        }

        var squareWidth = width / 3

        //外圆的大小，根据宽度来
        mDotRadius = width / 12

        //计算指定点的中心点
        mPoints[0][0] = Point(offsetX + squareWidth / 2, offsetY + squareWidth / 2, 0)
        mPoints[0][1] = Point(offsetX + squareWidth * 3 / 2, offsetY + squareWidth / 2, 1)
        mPoints[0][2] = Point(offsetX + squareWidth * 5 / 2, offsetY + squareWidth / 2, 2)
        mPoints[1][0] = Point(offsetX + squareWidth / 2, offsetY + squareWidth * 3 / 2, 3)
        mPoints[1][1] = Point(offsetX + squareWidth * 3 / 2, offsetY + squareWidth * 3 / 2, 4)
        mPoints[1][2] = Point(offsetX + squareWidth * 5 / 2, offsetY + squareWidth * 3 / 2, 5)
        mPoints[2][0] = Point(offsetX + squareWidth / 2, offsetY + squareWidth * 5 / 2, 6)
        mPoints[2][1] = Point(offsetX + squareWidth * 3 / 2, offsetY + squareWidth * 5 / 2, 7)
        mPoints[2][2] = Point(offsetX + squareWidth * 5 / 2, offsetY + squareWidth * 5 / 2, 8)
    }

    //记录手指按下的位置
    var mMovingX = 0f
    var mMovingY = 0f
    override fun onTouchEvent(event: MotionEvent): Boolean {
        mMovingX = event.x
        mMovingY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                //判断手指是否按在一个九宫格上
                //如何判断一个点是否在圆里   点到圆心的距离  < 半径
                var point = point
                if (point != null) {
                    mIsTouchPoint = true
                    mSelectPoints.add(point)
                    //改变当前点的状态
                    point.setStatusPressed()
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (mIsTouchPoint) {
                    //按下的时候一定要在一个九宫格上    不断的触摸的时候不断去判断
                    var selectPoint = point
                    if (selectPoint != null) {
                        if (!mSelectPoints.contains(selectPoint)) {
                            mSelectPoints.add(selectPoint)
                        }
                        //改变当前点的状态
                        selectPoint.setStatusPressed()
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                mIsTouchPoint = false
                //回调密码获取监听
                var password = ""
                for (selectPoint in mSelectPoints) {
                    password += selectPoint.index
                }
                Toast.makeText(context,password,LENGTH_SHORT).show()

                //清空mSelectPoints中所有点  点状态置为默认
                postDelayed({
                    clearSelectPoints()
//                    mIsErrorStatus = false
                    invalidate()
                }, 1000)
            }
        }

        //刷新
        invalidate()
        return true
    }

    //清空所有点
    private fun clearSelectPoints() {
        //将点状态置为默认
        for (point in mSelectPoints) {
            point.setStatusNormal()
        }
        //清空点
        mSelectPoints.clear()

    }

    /**
     * 获取按下的点
     * @return 当前按下的点
     */
    private val point: Point?
        get() {
            for (i in 0..2) {
                for (point in mPoints[i]) {
                    if (point != null) {
                        if (MathUtil.checkInRound(point.centerX.toFloat(), point.centerY.toFloat(), mDotRadius.toFloat(), mMovingX, mMovingY)) {
                            return point
                        }
                    }
                }
            }
            return null
        }

    /**
     * 宫格点状态的类
     */
    class Point(var centerX: Int, var centerY: Int, var index: Int) {
        //三种状态
        private val STATUS_NORMAL = 1

        private val STATUS_PRESSED = 2
        private val STATUS_ERROR = 3

        //默认状态
        private var status = STATUS_NORMAL;

        //设置当前状态
        fun setStatusNormal() {
            status = STATUS_NORMAL;
        }

        fun setStatusPressed() {
            status = STATUS_PRESSED;
        }

        fun setStatusError() {
            status = STATUS_ERROR;
        }

        //获取当前状态
        fun statusIsNormal(): Boolean {
            return status == STATUS_NORMAL
        }

        fun statusIsPressed(): Boolean {
            return status == STATUS_PRESSED
        }

        fun statusIsError(): Boolean {
            return status == STATUS_ERROR
        }
    }
}