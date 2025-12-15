package com.casic.otitan.common.widget.speak

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.media.MediaPlayer
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.casic.otitan.common.utils.common.DensityUtil
import com.casic.otitan.common.utils.encode.Base64Util
import com.casic.otitan.common.utils.log.LogUtil
import java.io.File
import java.io.FileOutputStream

/**
 *
 * 功能说明：
 * 1. 支持两种样式：TYPE_MINE（自己发送）和TYPE_OTHER（他人发送），显示位置和对齐方式不同
 * 2. 播放语音时显示扩散波形动画，包含一个实心扇形和两个空心圆弧
 * 3. 支持Base64编码的音频数据解码播放
 * 4. 自动管理临时音频文件，播放完成后自动清理
 *
 * 使用示例：
 * ```kotlin
 * // XML布局中
 * <com.casic.otitan.common.widget.speak.SpeakerView
 *     android:id="@+id/speakerView"
 *     android:layout_width="wrap_content"
 *     android:layout_height="wrap_content" />
 *
 * // 代码中
 * speakerView.setType(SpeakerView.TYPE_MINE) // 或 TYPE_OTHER
 * speakerView.setText("16″") // 设置时长文本
 * speakerView.start(messageId, ".mp3", base64AudioData) // 开始播放
 * ```
 *
 * created by fz on 2024/11/5 16:40
 * describe：语音消息播放控件（带波形动画）
 */
class SpeakerView : View {
    companion object {
        public const val TYPE_MINE = 0
        public const val TYPE_OTHER = 1
        const val TAG = "SpeakerView"
    }

    private var type = TYPE_MINE
    private val path: Path by lazy {
        Path()
    }
    private val startAngles by lazy {
        if (type == TYPE_OTHER) -45f else 135f
    }
    private val sweepAngles = 90f
    private val radius: Float by lazy {
        DensityUtil.dp2px(context, 4f).toFloat()
    }
    private val animator: ValueAnimator by lazy {
        ValueAnimator.ofInt(0, 3).apply {
            this.duration = 900L // 900ms 总时间
            this.interpolator = DecelerateInterpolator()
            this.repeatCount = ValueAnimator.INFINITE
            this.repeatMode = ValueAnimator.RESTART
            this.addUpdateListener { animation: ValueAnimator ->
                currentArcIndex = animation.animatedValue as Int
                invalidate()
            }
        }
    }

    private var currentArcIndex = -1
    private val textPaint: Paint by lazy {
        // 文字绘制用的 Paint 对象
        Paint().apply {
            this.color = ContextCompat.getColor(context, com.casic.otitan.common.R.color.autoColor)
            this.textSize = DensityUtil.sp2px(context, 12f).toFloat()
            this.textAlign = if (type == TYPE_OTHER) Paint.Align.LEFT else Paint.Align.RIGHT
        }
    }
    private var text: String? = null

    private val paintFill: Paint by lazy {
        Paint().apply {
            this.color = ContextCompat.getColor(context, com.casic.otitan.common.R.color.autoColor)
            this.style = Paint.Style.FILL // 设置为FILL
            this.isAntiAlias = true
        }
    }
    private val paintStroke: Paint by lazy {
        Paint().apply {
            this.color = ContextCompat.getColor(context, com.casic.otitan.common.R.color.autoColor)
            this.style = Paint.Style.STROKE
            this.strokeWidth = 5f
            this.isAntiAlias = true
        }
    }

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun setType(type: Int) {
        this.type = type
    }

    fun setText(text: String?) {
        this.text = text
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerY = height / 2
        val text = this.text ?: "16″"
        val textWidth = textPaint.measureText(text)
        val fontMetrics = textPaint.fontMetrics
        val distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
        val baseline = centerY + distance

        // 绘制文字
        if (type == TYPE_MINE) {
            canvas.drawText(text, (width - paddingEnd).toFloat(), baseline, textPaint)
        } else {
            //文字中心和圆心一致
            canvas.drawText(text, 0f + paddingStart, baseline, textPaint)
        }

        // 计算圆弧的起始位置
        val arcStartX: Float =
            if (type == TYPE_MINE) (width - textWidth - paddingEnd - DensityUtil.dp2px(context, 8f))
            else (textWidth + paddingStart + DensityUtil.dp2px(context, 8f))
        when (currentArcIndex) {
            0 -> {
                path.reset()
                // 绘制第一个实心扇形
                path.addArc(
                    arcStartX - radius,
                    centerY - radius,
                    arcStartX + radius,
                    centerY + radius,
                    startAngles,
                    sweepAngles
                )
                path.lineTo(arcStartX, centerY.toFloat()) // 连接到圆心
                path.close() // 闭合路径
                canvas.drawPath(path, paintFill)
            }

            1 -> {
                // 绘制第二空心圆弧
                path.addArc(
                    arcStartX - radius * 2,
                    centerY - radius * 2,
                    arcStartX + radius * 2,
                    centerY + radius * 2,
                    startAngles,
                    sweepAngles
                )
                canvas.drawPath(path, paintStroke)
            }

            2 -> {
                // 绘制第三个空心圆弧
                path.addArc(
                    arcStartX - radius * 3,
                    centerY - radius * 3,
                    arcStartX + radius * 3,
                    centerY + radius * 3,
                    startAngles,
                    sweepAngles
                )
                canvas.drawPath(path, paintStroke)
            }

            else -> {
                for (i in 0 until 3) {
                    path.reset()
                    if (i == 0) {
                        // 绘制第一个实心扇形
                        path.addArc(
                            arcStartX - radius * (i + 1),
                            centerY - radius * (i + 1),
                            arcStartX + radius * (i + 1),
                            centerY + radius * (i + 1),
                            startAngles,
                            sweepAngles
                        )
                        path.lineTo(arcStartX, centerY.toFloat()) // 连接到圆心
                        path.close() // 闭合路径
                        canvas.drawPath(path, paintFill)
                    } else {
                        // 绘制第二和第三个空心圆弧
                        path.addArc(
                            arcStartX - radius * (i + 1),
                            centerY - radius * (i + 1),
                            arcStartX + radius * (i + 1),
                            centerY + radius * (i + 1),
                            startAngles,
                            sweepAngles
                        )
                        canvas.drawPath(path, paintStroke)
                    }
                }
            }
        }
    }

    fun start(messageId: String, extension: String?, file: String?) {
        if (animator.isRunning) {
            currentArcIndex = -1
            animator.cancel()
            invalidate()
        } else {
            try {
                animator.start()
                playAudioFromByteArray(messageId, extension, Base64Util.decode(file))
            } catch (e: Exception) {
                e.printStackTrace()
                LogUtil.show(TAG,"播放音频异常：$e")
                Toast.makeText(context, "播放失败", Toast.LENGTH_SHORT).show()
                stop()
            }
        }
    }

    private fun stop() {
        if (animator.isRunning) {
            animator.cancel()
            currentArcIndex = -1
            invalidate()
        }
    }

    private fun playAudioFromByteArray(messageId: String, extension: String?, byteArray: ByteArray?) {
        // 1. 创建临时文件
        val tempFile = File(context.cacheDir, messageId + extension) // 你可以根据需要调整文件名和格式
        // 2. 将字节数组写入文件
        val fos = FileOutputStream(tempFile)
        fos.write(byteArray)
        fos.close()
        LogUtil.show(TAG,"音频临时文件：${tempFile.absolutePath}")
        // 3. 使用 MediaPlayer 播放音频
        val mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(tempFile.absolutePath)
        mediaPlayer.prepare() // 异步加载音频资源
        mediaPlayer.start() // 播放音频
        // 4. 在播放完成后删除临时文件
        mediaPlayer.setOnCompletionListener {
            LogUtil.show(TAG,"音频播放完成")
            tempFile.delete() // 播放完成后删除临时文件
            mediaPlayer.release() // 释放资源
            stop()
        }
    }
}
