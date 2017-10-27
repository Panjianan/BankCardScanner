package com.wintone.scanner.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.support.v4.content.ContextCompat
import android.view.View


/***
 * <br> Project kfx
 * <br> Package com.caetp.kfx.base.view.activity.scanner.widget
 * <br> Description 不知道干嘛的，大概是中间的方形吧?
 * <br> Version 1.0
 * <br> Author Administrator
 * <br> Creation 2017/7/7 10:24
 * <br> Mender Administrator
 * <br> Modification 2017/7/7 10:24
 * <br> Copyright Copyright © 2012 - 2017 ZhongWangXinTong.All Rights Reserved.
 */
class ViewfinderView constructor(context: Context, val w: Int, val h: Int, boo: Boolean) : View(context) {

    private val ANIMATION_DELAY: Long = 10

    var boo = false
    private val frameColor: Int = ContextCompat.getColor(context, resources.getIdentifier("viewfinder_frame", "color", context.packageName))
    private val laserColor: Int = ContextCompat.getColor(context, resources.getIdentifier("viewfinder_laser", "color", context.packageName))
    private val maskColor: Int = ContextCompat.getColor(context, resources.getIdentifier("viewfinder_mask", "color", context.packageName))
    private val resultColor: Int = ContextCompat.getColor(context, resources.getIdentifier("result_view", "color", context.packageName))

    private val scannerAlpha: Int = 0

    var leftLine = 0
    var topLine = 0
    var rightLine = 0
    var bottomLine = 0

    private val frame: Rect by lazy { Rect() }
    private val paint: Paint by lazy { Paint() }
    private val paintLine: Paint by lazy { Paint() }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        var t: Int
        var b: Int
        var l: Int
        var r: Int
        val width = canvas.width
        val height = canvas.height
        if (width <= height || this.boo) {
            t = h / 5
            b = h - t
            l = (this.w - ((b - t).toDouble() * 1.585).toInt()) / 2
            r = this.w - l
            l += 30
            t += 19
            r -= 30
            b -= 19
        } else {
            t = this.h / 10
            b = this.h - t
            l = (this.w - ((b - t).toDouble() * 1.585).toInt()) / 2
            r = this.w - l
            l += 30
            t += 19
            r -= 30
            b -= 19
        }

        frame.left = l
        frame.top = t
        frame.right = r
        frame.bottom = b

        this.paint.color = this.maskColor
        canvas.drawRect(0.0f, 0.0f, width.toFloat(), this.frame.top.toFloat(), this.paint)
        canvas.drawRect(0.0f, this.frame.top.toFloat(), this.frame.left.toFloat(), (this.frame.bottom + 1).toFloat(), this.paint)
        canvas.drawRect((this.frame.right + 1).toFloat(), this.frame.top.toFloat(), width.toFloat(), (this.frame.bottom + 1).toFloat(), this.paint)
        canvas.drawRect(0.0f, (this.frame.bottom + 1).toFloat(), width.toFloat(), height.toFloat(), this.paint)
        if (width <= height || this.boo) {
            this.paintLine.color = this.frameColor
            this.paintLine.strokeWidth = 8.0f
            this.paintLine.isAntiAlias = true
            canvas.drawLine(l.toFloat(), t.toFloat(), (l + 100).toFloat(), t.toFloat(), this.paintLine)
            canvas.drawLine(l.toFloat(), t.toFloat(), l.toFloat(), (t + 100).toFloat(), this.paintLine)
            canvas.drawLine(r.toFloat(), t.toFloat(), (r - 100).toFloat(), t.toFloat(), this.paintLine)
            canvas.drawLine(r.toFloat(), t.toFloat(), r.toFloat(), (t + 100).toFloat(), this.paintLine)
            canvas.drawLine(l.toFloat(), b.toFloat(), (l + 100).toFloat(), b.toFloat(), this.paintLine)
            canvas.drawLine(l.toFloat(), b.toFloat(), l.toFloat(), (b - 100).toFloat(), this.paintLine)
            canvas.drawLine(r.toFloat(), b.toFloat(), (r - 100).toFloat(), b.toFloat(), this.paintLine)
            canvas.drawLine(r.toFloat(), b.toFloat(), r.toFloat(), (b - 100).toFloat(), this.paintLine)
        } else {
            this.paintLine.color = this.frameColor
            this.paintLine.strokeWidth = 8.0f
            this.paintLine.isAntiAlias = true
            val num = t - 40
            canvas.drawLine((l - 4).toFloat(), t.toFloat(), (l + num).toFloat(), t.toFloat(), this.paintLine)
            canvas.drawLine(l.toFloat(), t.toFloat(), l.toFloat(), (t + num).toFloat(), this.paintLine)
            canvas.drawLine(r.toFloat(), t.toFloat(), (r - num).toFloat(), t.toFloat(), this.paintLine)
            canvas.drawLine(r.toFloat(), (t - 4).toFloat(), r.toFloat(), (t + num).toFloat(), this.paintLine)
            canvas.drawLine((l - 4).toFloat(), b.toFloat(), (l + num).toFloat(), b.toFloat(), this.paintLine)
            canvas.drawLine(l.toFloat(), b.toFloat(), l.toFloat(), (b - num).toFloat(), this.paintLine)
            canvas.drawLine(r.toFloat(), b.toFloat(), (r - num).toFloat(), b.toFloat(), this.paintLine)
            canvas.drawLine(r.toFloat(), (b + 4).toFloat(), r.toFloat(), (b - num).toFloat(), this.paintLine)
            if (this.leftLine === 1) {
                canvas.drawLine(l.toFloat(), t.toFloat(), l.toFloat(), b.toFloat(), this.paintLine)
            }
            if (this.rightLine === 1) {
                canvas.drawLine(r.toFloat(), t.toFloat(), r.toFloat(), b.toFloat(), this.paintLine)
            }
            if (this.topLine === 1) {
                canvas.drawLine(l.toFloat(), t.toFloat(), r.toFloat(), t.toFloat(), this.paintLine)
            }
            if (this.bottomLine === 1) {
                canvas.drawLine(l.toFloat(), b.toFloat(), r.toFloat(), b.toFloat(), this.paintLine)
            }
        }
        postInvalidateDelayed(ANIMATION_DELAY)
    }

}