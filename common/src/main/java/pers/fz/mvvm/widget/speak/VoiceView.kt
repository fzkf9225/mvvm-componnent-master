package pers.fz.mvvm.widget.speak;

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.View
import android.view.animation.BounceInterpolator
import android.view.animation.Interpolator
import pers.fz.mvvm.util.common.DensityUtil
import pers.fz.mvvm.util.common.ThreadExecutor
import java.util.Random
import kotlin.concurrent.Volatile
import kotlin.math.max

/**
 * 只是用来显示微信语音动画的一个控件
 *
 * 在录音的时候可以通过 [.addVoiceSize] 来设置一个音量条的高度
 *
 * 如果不使用该控件的时候需要使用[.quit] 方法来停止显示，避免内存泄露
 */
class VoiceView : View {
    private val minVoiceSize = 20
    private var linePaint: Paint? = null
    private var bgPaint: Paint? = null
    private var txtPaint: Paint? = null

    var drawLines: MutableList<DrawLine> = ArrayList()
    private val lineWidth = 10 //音量条的宽度
    private val lineSpace = 10

    var lineHandler: LineHandler? = null

    private val duration = 400
    private var mInterpolator: Interpolator = BounceInterpolator()

    private val initWidthRotas = 0.42f //最开始显示的宽度占控件整个宽度的比例
    private val maxWidthRotas = 0.8f //最大显示宽度
    private var widthRotas = initWidthRotas //当前显示的宽度

    private var bgRound = 15 //背景圆角的大小,单位dp
    private var showWidth = 0 //显示的宽度
    private val textRect = Rect()

    @Volatile
    private var quit = false
    private val cancelBgColor = "#F85050"
    private val normalBgColor = "#EFEFEF"

    constructor(context: Context?) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        linePaint = Paint()
        linePaint?.color = Color.parseColor("#000000")
        linePaint?.style = Paint.Style.FILL
        linePaint?.strokeCap = Paint.Cap.ROUND
        linePaint?.strokeJoin = Paint.Join.ROUND

        bgPaint = Paint()
        bgPaint?.color = Color.parseColor(normalBgColor)
        bgPaint?.style = Paint.Style.FILL
        bgPaint?.strokeCap = Paint.Cap.ROUND
        bgPaint?.strokeJoin = Paint.Join.ROUND
        bgPaint?.isAntiAlias = true
        bgRound = DensityUtil.dp2px(context, bgRound.toFloat())


        txtPaint = Paint()
        txtPaint?.color = Color.parseColor("#000000")
        txtPaint?.style = Paint.Style.FILL
        txtPaint?.strokeCap = Paint.Cap.ROUND
        txtPaint?.strokeJoin = Paint.Join.ROUND
        txtPaint?.textSize = DensityUtil.dp2px(context, 14f).toFloat()
        txtPaint?.isAntiAlias = true

        buildDrawLines()
        ThreadExecutor.getInstance().execute {
            if (Looper.myLooper() == null) {
                Looper.prepare()
            }
            lineHandler = LineHandler(Looper.myLooper()!!)
            Looper.loop()
        }
    }


    class DrawLine {
        var rectF: RectF? = null
        var maxSize: Int = 0
        var lineSize: Int = 0
        var small: Boolean = true //是否缩小模式，
        var rotas: Float = 1.0f
        var timeCompletion: Float = 0f //时间完成度，返回在0-1
        var duration: Int = 0
    }

    fun setCancel(cancel: Boolean) {
        if (cancel) {
            bgPaint?.color = Color.parseColor(cancelBgColor)
        } else {
            bgPaint?.color = Color.parseColor(normalBgColor)
        }
    }

    /**
     * 每条音频线能显示的最大值与showVoiceSize的比值
     */
    private var ratios: FloatArray = floatArrayOf(
        0.2f,
        0.3f,
        0.4f,
        0.5f,
        0.6f,
        0.7f,
        0.5f,
        0.3f,
        0.5f,
        0.8f,
        1.0f,
        0.8f,
        0.5f,
        0.3f,
        0.5f,
        0.7f,
        0.6f,
        0.5f,
        0.4f,
        0.3f,
        0.2f
    )

    private fun buildDrawLines() {
        drawLines.clear()
        for (ratio in ratios) {
            val maxSize = (minVoiceSize * ratio).toInt()
            val rect = RectF(
                (-lineWidth / 2).toFloat(),
                (-maxSize / 2).toFloat(),
                (lineWidth / 2).toFloat(),
                (maxSize / 2).toFloat()
            )
            val drawLine = DrawLine()
            drawLine.maxSize = maxSize
            drawLine.rectF = rect
            drawLine.lineSize = Random().nextInt(maxSize)
            drawLine.rotas = ratio
            //通过设置不同的时间完成度，让每个音量条有不同的初始值。可以实现参差不齐的效果
            // drawLine.timeCompletion = ratio;
            drawLine.duration = (duration * (1.0f / ratio)).toInt()
            drawLines.add(drawLine)
        }
    }


    private var showText = false
    private var content: String? = null

    fun setContent(content: String) {
        showText = true
        this.content = content
        txtPaint?.getTextBounds(content, 0, content.length, textRect)
        //lineHandler.removeMessages(1);
        //lineHandler.removeMessages(2);
        // invalidate();
    }

    private var toBig = true

    /**
     * 设置音量大小，其实就是音量条显示的高度（单位像素)
     *
     * @param voiceSize
     */
    fun addVoiceSize(voiceSize: Int) {
        // showVoiceSize = Math.max(MIN_VOICE_SIZE, voiceSize);
        val message = Message.obtain()
        message.obj = voiceSize
        message.what = WHAT_CHANGE_VOICE_SIZE
        lineHandler?.sendMessage(message)
        if (toBig) {
            //开始变大
            lineHandler?.removeMessages(WHAT_BIG)
            lineHandler?.sendEmptyMessage(WHAT_BIG)
            toBig = false
        }
    }


    fun setInterpolator(mInterpolator: Interpolator) {
        this.mInterpolator = mInterpolator
    }

    private val showVoiceSize = 40

    private var checkStarIndex = 0
    private var isCheckMode = false

    inner class LineHandler(looper: Looper) : Handler(looper) {
        override fun dispatchMessage(msg: Message) {
//            Log.i("xxx", "wx dispatchMessage what=" + msg.what);
            when (msg.what) {
                WHAT_ANIMATION -> {
                    for (drawLine in drawLines) {
                        val timeStep = 16.0f / drawLine.duration //时间增长步长
                        var timeCompletion = drawLine.timeCompletion //时间完成度
                        timeCompletion += timeStep //更新时间完成度
                        val animationCompletion =
                            mInterpolator.getInterpolation(timeCompletion) //获取动画完成度。
                        //int maxLineSize = (int) (drawLine.rotas * showVoiceSize);
                        var lineSize = 0
                        //更新音量条的高度
                        lineSize = if (drawLine.small) {
                            //变小
                            ((1 - animationCompletion) * drawLine.maxSize).toInt()
                        } else {
                            (animationCompletion * drawLine.maxSize).toInt()
                        }
                        if (timeCompletion >= 1) {
                            //完成了单边的缩小，或增长，则切换模式
                            drawLine.small = !drawLine.small
                            drawLine.timeCompletion = 0f
                        } else {
                            drawLine.timeCompletion = timeCompletion //更新时间完成度。
                        }

                        lineSize = max(lineSize.toDouble(), 10.0).toInt() //对最小值进行过滤
                        val rectF = drawLine.rectF
                        rectF?.top = -lineSize * 1.0f / 2
                        rectF?.bottom = lineSize * 1.0f / 2
                        drawLine.lineSize = lineSize
                    }
                    invalidate() //更新UI
                    removeMessages(WHAT_ANIMATION)
                    sendEmptyMessageDelayed(WHAT_ANIMATION, 16)
                }

                WHAT_BIG ->                     //改变宽度
                    if (widthRotas < maxWidthRotas) {
                        widthRotas += 0.005f
                        showWidth = (width * widthRotas).toInt()
                        invalidate()
                        lineHandler?.sendEmptyMessageDelayed(WHAT_BIG, 500)
                    }

                WHAT_CHANGE_VOICE_SIZE -> {
                    val voiceSize = msg.obj as Int
                    if (voiceSize < 30) {
                        if (!isCheckMode) {
                            //声音太小进入监听模式
                            isCheckMode = true
                            checkStarIndex = drawLines.size - 1
                            sendEmptyMessage(WHAT_CHECK_VOICE)
                        }
                        return
                    } else {
                        //退出监听模式
                        isCheckMode = false
                        removeMessages(WHAT_CHECK_VOICE)
                    }


                    for (drawLine in drawLines) {
                        drawLine.timeCompletion = 0f
                        drawLine.small = false
                        drawLine.maxSize = (drawLine.rotas * voiceSize).toInt()
                    }
                    sendEmptyMessage(WHAT_ANIMATION)
                }

                WHAT_CHECK_VOICE -> {
                    if (!isCheckMode) {
                        //判断是否是监听模式
                        return
                    }

                    removeMessages(WHAT_ANIMATION) //移除之前的上下的动画模式
                    //由于声音太小，显示波浪动画表示正在检查声音
                    var i = 0
                    while (i < drawLines.size) {
                        val drawLine = drawLines[i]
                        val index = i - checkStarIndex
                        if (index >= 0 && index < checkModeItemSize.size) {
                            drawLine.lineSize = checkModeItemSize[index]
                        } else {
                            drawLine.lineSize = 10
                        }
                        drawLine.rectF?.top = (-drawLine.lineSize / 2).toFloat()
                        drawLine.rectF?.bottom = (drawLine.lineSize / 2).toFloat()

                        i++
                    }
                    checkStarIndex--
                    if (checkStarIndex == -checkModeItemSize.size) {
                        checkStarIndex = drawLines.size - 1
                    }

                    invalidate() //更新UI
                    removeMessages(WHAT_CHECK_VOICE)
                    sendEmptyMessageDelayed(WHAT_CHECK_VOICE, 100)
                }
            }
        }
    }

    fun quit() {
        lineHandler?.looper?.quit()
        quit = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        showWidth = (widthRotas * w).toInt()
        lineHandler?.sendEmptyMessage(WHAT_ANIMATION)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        canvas.translate((width / 2).toFloat(), (height / 2).toFloat())
        //画背景
        canvas.drawRoundRect(
            (-showWidth / 2).toFloat(),
            (-height / 2).toFloat(),
            (showWidth / 2).toFloat(),
            (height / 2).toFloat(),
            bgRound.toFloat(),
            bgRound.toFloat(),
            bgPaint!!
        )
        if (showText) {
            val txtHeight = textRect.height() / 2
            val txtWidth = textRect.width() / 2
            canvas.drawText(content!!, -txtWidth.toFloat(), txtHeight.toFloat(), txtPaint!!)
        } else {
            val offsetX = (drawLines.size - 1) * 1.0f / 2 * (lineWidth + lineSpace)
            canvas.translate(-offsetX, 0f)
            for (drawLine in drawLines) {
                canvas.drawRoundRect(drawLine.rectF!!, 5f, 5f, linePaint!!)
                canvas.translate((lineWidth + lineSpace).toFloat(), 0f)
            }
        }
        canvas.restore()
    }

    companion object {
        private const val WHAT_ANIMATION = 1 //驱动动画的事件
        private const val WHAT_BIG = 2 //驱动变宽的事件
        private const val WHAT_CHANGE_VOICE_SIZE = 3 //驱动音量条高低变化的事件
        private const val WHAT_CHECK_VOICE = 4 //驱动进入巡检模式的事件

        private val checkModeItemSize = intArrayOf(15, 20, 25, 30, 25, 20, 15)
    }
}
