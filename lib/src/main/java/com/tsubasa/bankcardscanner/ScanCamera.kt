@file:Suppress("DEPRECATION")

package com.tsubasa.bankcardscanner

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.ImageFormat
import android.graphics.Point
import android.hardware.Camera
import android.hardware.Camera.PreviewCallback
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.util.DisplayMetrics
import android.view.*
import android.view.SurfaceHolder.Callback
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.RelativeLayout.LayoutParams
import android.widget.Toast
import com.wintone.bankcard.BankCardAPI
import com.wintone.scanner.widget.ViewfinderView
import java.io.IOException
import java.util.*

const val EXTRA_SCAN_CARD_RESULT_IMAGE = "EXTRA_SCAN_CARD_RESULT_IMAGE"
const val EXTRA_SCAN_CARD_RESULT_STR = "EXTRA_SCAN_CARD_RESULT_IMAGE"

class ScanCamera : Activity(), Callback, PreviewCallback {

    private val NORMAL_CARD_SCALE = 1.58577

    private var api: BankCardAPI? = null
    private var camera: Camera? = null
    private var counter = 0
    private var counterCut = 0
    private var height: Int = 0
    private var isFatty = false
    private var isROI = false
    private var myView: ViewfinderView? = null

    private var preHeight = 0
    private var preWidth = 0
    private var re_c: RelativeLayout? = null
    var srcHeight: Int = 0
    var srcWidth: Int = 0
    var surfaceHeight: Int = 0
    var surfaceWidth: Int = 0
    private var time: Timer? = null
    private var timer: TimerTask? = null
    private var width: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        super.onCreate(savedInstanceState)
        //        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_scan_card)
        setScreenSize(this)
        findView()
    }

    override fun onRestart() {
        super.onRestart()
    }

    override fun onResume() {
        super.onResume()
        this.api = BankCardAPI()
        this.api!!.WTInitCardKernal("", 0)
    }

    private fun setScreenSize(context: Context) {
        val x: Int
        val y: Int
        val display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val screenSize = Point()
        display.getRealSize(screenSize)
        x = screenSize.x
        y = screenSize.y
        this.srcWidth = x
        this.srcHeight = y
    }

    private fun findView() {
        val surfaceView = findViewById(R.id.surfaceViwe) as SurfaceView
        this.re_c = findViewById(R.id.re_c) as RelativeLayout
        val help_word = findViewById(R.id.help_word) as ImageView
        val back = findViewById(R.id.back_camera) as ImageButton
        val flash = findViewById(R.id.flash_camera) as ImageButton
        val metric = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metric)
        this.width = metric.widthPixels
        this.height = metric.heightPixels
        if (this.width * 3 == this.height * 4) {
            this.isFatty = true
        }
        val back_w = (this.width.toDouble() * 0.066796875).toInt()
        var layoutParams = LayoutParams(back_w, back_w)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        var Fheight = this.height
        if (this.isFatty) {
            Fheight = (this.height.toDouble() * 0.75).toInt()
        }
        layoutParams.leftMargin = (((this.width.toDouble() - Fheight.toDouble() * 0.8 * 1.585) / 2.0 - back_w.toDouble()) / 2.0).toInt()
        layoutParams.bottomMargin = (this.height.toDouble() * 0.10486111111111111).toInt()
        back.layoutParams = layoutParams
        val flash_w = (this.width.toDouble() * 0.066796875).toInt()
        layoutParams = LayoutParams(flash_w, flash_w)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE)
        if (this.isFatty) {
            Fheight = (this.height.toDouble() * 0.75).toInt()
        }
        layoutParams.leftMargin = (((this.width.toDouble() - Fheight.toDouble() * 0.8 * 1.585) / 2.0 - back_w.toDouble()) / 2.0).toInt()
        layoutParams.topMargin = (this.height.toDouble() * 0.10486111111111111).toInt()
        flash.layoutParams = layoutParams
        val help_word_w = (this.width.toDouble() * 0.474609375).toInt()
        val help_word_h = (help_word_w.toDouble() * 0.05185185185185185).toInt()
        layoutParams = LayoutParams(help_word_w, help_word_h)
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE)
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE)
        help_word.layoutParams = layoutParams
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        if (this.isFatty) {
            layoutParams.bottomMargin = this.height / 10 - help_word_h / 2
        } else {
            layoutParams.bottomMargin = this.height / 20 - help_word_h / 2
        }
        val surfaceHolder = surfaceView.holder
        surfaceHolder.addCallback(this)
        surfaceHolder.setType(3)
        back.setOnClickListener { this@ScanCamera.finish() }
        flash.setOnClickListener {
            if (!this@ScanCamera.packageManager.hasSystemFeature("android.hardware.camera.flash")) {
                Toast.makeText(this@ScanCamera, this@ScanCamera.resources.getString(this@ScanCamera.resources.getIdentifier("toast_flash", "string", this@ScanCamera.application.packageName)), Toast.LENGTH_SHORT).show()
            } else if (this@ScanCamera.camera != null) {
                val parameters = this@ScanCamera.camera!!.parameters
                if (parameters.flashMode == "torch") {
                    parameters.flashMode = "off"
                    parameters.exposureCompensation = 0
                } else {
                    parameters.flashMode = "torch"
                    parameters.exposureCompensation = -1
                }
                try {
                    this@ScanCamera.camera!!.parameters = parameters
                } catch (e: Exception) {
                    Toast.makeText(this@ScanCamera, this@ScanCamera.resources.getString(this@ScanCamera.resources.getIdentifier("toast_flash", "string", this@ScanCamera.application.packageName)), Toast.LENGTH_SHORT).show()
                }

                this@ScanCamera.camera!!.startPreview()
            }
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (this.camera == null) {
            try {
                this.camera = Camera.open()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(applicationContext, resources.getString(R.string.cannot_open_camera), Toast.LENGTH_SHORT).show()
                return
            }

        }
        try {
            this.camera!!.setPreviewDisplay(holder)
            this.time = Timer()
            if (this.timer == null) {
                this.timer = object : TimerTask() {
                    override fun run() {
                        if (this@ScanCamera.camera != null) {
                            try {
                                this@ScanCamera.camera!!.autoFocus { success, camera1 -> }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                        }
                    }
                }
            }
            this.time!!.schedule(this.timer, 500, 2500)
            initCamera(holder)
        } catch (e2: IOException) {
            e2.printStackTrace()
        }

    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        try {
            if (this.camera != null) {
                this.camera!!.setPreviewCallback(null)
                this.camera!!.stopPreview()
                this.camera!!.release()
                this.camera = null
            }
        } catch (ignored: Exception) {
        }

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            try {
                if (this.camera != null) {
                    this.camera!!.setPreviewCallback(null)
                    this.camera!!.stopPreview()
                    this.camera!!.release()
                    this.camera = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            finish()
        }
        return super.onKeyDown(keyCode, event)
    }

    @TargetApi(14)
    private fun initCamera(holder: SurfaceHolder) {
        val parameters = this.camera!!.parameters
        getCameraPreParameters(this.camera!!)
        if (!this.isROI) {
            var t = this.height / 10
            var b = this.height - t
            var l = (this.width - ((b - t).toDouble() * NORMAL_CARD_SCALE).toInt()) / 2
            var r = this.width - l
            l += 30
            t += 19
            r -= 30
            b -= 19
            if (this.isFatty) {
                t = this.height / 5
                b = this.height - t
                l = (this.width - ((b - t).toDouble() * NORMAL_CARD_SCALE).toInt()) / 2
                r = this.width - l
            }
            val proportion = this.width.toDouble() / this.preWidth.toDouble()
            l = (l.toDouble() / proportion).toInt()
            t = (t.toDouble() / proportion).toInt()
            r = (r.toDouble() / proportion).toInt()
            b = (b.toDouble() / proportion).toInt()
            this.api!!.WTSetROI(intArrayOf(l, t, r, b), this.preWidth, this.preHeight)
            this.isROI = true
            this.myView = ViewfinderView(this, this.width, this.height, this.isFatty)
            this.re_c!!.addView(this.myView)
        }
        parameters.pictureFormat = ImageFormat.JPEG
        parameters.setPreviewSize(this.preWidth, this.preHeight)
        if (parameters.supportedFocusModes.contains("continuous-picture")) {
            if (this.time != null) {
                this.time!!.cancel()
                this.time = null
            }
            if (this.timer != null) {
                this.timer!!.cancel()
                this.timer = null
            }
            parameters.focusMode = "continuous-picture"
        } else if (parameters.supportedFocusModes.contains("auto")) {
            println("聚焦else")
            parameters.focusMode = "auto"
        }
        this.camera!!.setPreviewCallback(this)
        this.camera!!.parameters = parameters
        try {
            this.camera!!.setPreviewDisplay(holder)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        this.camera!!.startPreview()
    }

    override fun onPreviewFrame(data: ByteArray, camera: Camera) {
        val parameters = camera.parameters
        val isBorders = IntArray(4)
        this.counter++
        if (this.counter == 2) {
            this.counter = 0
            val recogval = CharArray(30)
            val bRotated = IntArray(1)
            val pLineWarp = IntArray(32000)
            val result = this.api!!.RecognizeNV21(data, parameters.previewSize.width, parameters.previewSize.height, isBorders, recogval, 30, bRotated, pLineWarp)
            if (isBorders[0] == 1) {
                if (this.myView != null) {
                    this.myView!!.leftLine = 1
                }
            } else if (this.myView != null) {
                this.myView!!.leftLine = 0
            }
            if (isBorders[1] == 1) {
                if (this.myView != null) {
                    this.myView!!.topLine = 1
                }
            } else if (this.myView != null) {
                this.myView!!.topLine = 0
            }
            if (isBorders[2] == 1) {
                if (this.myView != null) {
                    this.myView!!.rightLine = 1
                }
            } else if (this.myView != null) {
                this.myView!!.rightLine = 0
            }
            if (isBorders[3] == 1) {
                if (this.myView != null) {
                    this.myView!!.bottomLine = 1
                }
            } else if (this.myView != null) {
                this.myView!!.bottomLine = 0
            }
            if (isBorders[0] != 1 || isBorders[1] != 1 || isBorders[2] != 1 || isBorders[3] != 1) {
                this.counterCut++
                if (this.counterCut == 5) {
                    this.counterCut = 0
                }
            } else if (result == 0) {
                camera.stopPreview()
                this.api!!.WTUnInitCardKernal()
                val mVibrator = application.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                mVibrator.vibrate(100)
                val cardNumber = String(recogval)
                val intent = Intent()
                intent.putExtra(EXTRA_SCAN_CARD_RESULT_IMAGE, pLineWarp)
                intent.putExtra(EXTRA_SCAN_CARD_RESULT_STR, cardNumber)
                setResult(RESULT_OK, intent)
                finish()
                camera.setPreviewCallback(null)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (this.timer != null) {
            this.timer!!.cancel()
            this.timer = null
        }
        try {
            if (this.camera != null) {
                this.camera!!.setPreviewCallback(null)
                this.camera!!.stopPreview()
                this.camera!!.release()
                this.camera = null
            }
        } catch (ignored: Exception) {
        }

    }

    fun getCameraPreParameters(camera: Camera) {
        var isShowBorder = false
        if ("PLK-TL01H" == Build.MODEL) {
            this.preWidth = 1920
            this.preHeight = 1080
        } else if ("MI 3" == Build.MODEL) {
            this.preWidth = 1024
            this.preHeight = 576
        } else {
            val list = camera.parameters.supportedPreviewSizes
            val ratioScreen = this.srcWidth.toFloat() / this.srcHeight.toFloat()
            var i = 0
            while (i < list.size) {
                if (ratioScreen == list[i].width.toFloat() / list[i].height.toFloat() && (list[i].width >= 1280 || list[i].height >= 720)) {
                    if (this.preWidth == 0 && this.preHeight == 0) {
                        this.preWidth = list[i].width
                        this.preHeight = list[i].height
                    }
                    if (list[0].width > list[list.size - 1].width) {
                        if (this.preWidth > list[i].width || this.preHeight > list[i].height) {
                            this.preWidth = list[i].width
                            this.preHeight = list[i].height
                        }
                    } else if ((this.preWidth < list[i].width || this.preHeight < list[i].height) && this.preWidth < 1280 && this.preHeight < 720) {
                        this.preWidth = list[i].width
                        this.preHeight = list[i].height
                    }
                }
                i++
            }
            if (this.preWidth == 0 || this.preHeight == 0) {
                isShowBorder = true
                this.preWidth = list[0].width
                this.preHeight = list[0].height
                i = 0
                while (i < list.size) {
                    if (list[0].width > list[list.size - 1].width) {
                        if ((this.preWidth >= list[i].width || this.preHeight >= list[i].height) && list[i].width >= 1280) {
                            this.preWidth = list[i].width
                            this.preHeight = list[i].height
                        }
                    } else if ((this.preWidth <= list[i].width || this.preHeight <= list[i].height) && this.preWidth < 1280 && this.preHeight < 720 && list[i].width >= 1280) {
                        this.preWidth = list[i].width
                        this.preHeight = list[i].height
                    }
                    i++
                }
            }
            if (this.preWidth == 0 || this.preHeight == 0) {
                isShowBorder = true
                if (list[0].width > list[list.size - 1].width) {
                    this.preWidth = list[0].width
                    this.preHeight = list[0].height
                } else {
                    this.preWidth = list[list.size - 1].width
                    this.preHeight = list[list.size - 1].height
                }
            }
            if (!isShowBorder) {
                this.surfaceWidth = this.srcWidth
                this.surfaceHeight = this.srcHeight
            } else if (ratioScreen > this.preWidth.toFloat() / this.preHeight.toFloat()) {
                this.surfaceWidth = (this.preWidth.toFloat() / this.preHeight.toFloat() * this.srcHeight.toFloat()).toInt()
                this.surfaceHeight = this.srcHeight
            } else {
                this.surfaceWidth = this.srcWidth
                this.surfaceHeight = (this.preHeight.toFloat() / this.preWidth.toFloat() * this.srcHeight.toFloat()).toInt()
            }
        }
    }
}