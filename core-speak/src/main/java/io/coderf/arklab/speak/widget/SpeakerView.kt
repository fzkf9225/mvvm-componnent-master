package io.coderf.arklab.speak.widget

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
import io.coderf.arklab.core.utils.DensityUtil
import io.coderf.arklab.core.utils.encode.Base64Util
import io.coderf.arklab.log.ArkLog
import io.coderf.arklab.speak.record.VoiceRecorder
import io.coderf.arklab.speak.style.SpeakUiStyle
import java.io.File
import java.io.FileOutputStream

/**
 * 语音气泡播放控件（列表项展示时长 + 点击播放本地 AMR）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/25 13:42
 */
class SpeakerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        const val TYPE_MINE = 0
        const val TYPE_OTHER = 1
        private const val TAG = "SpeakerView"
    }

    private val log = ArkLog.channel(VoiceRecorder.MODULE_ID, TAG)

    private var type = TYPE_MINE
    private var text: String? = null
    private var waveColor: Int = SpeakUiStyle.DEFAULT_ACCENT

    private val path = Path()
    private var currentArcIndex = -1
    private var mediaPlayer: MediaPlayer? = null
    private var playingTempFile: File? = null
    private var deleteOnComplete = false

    private val startAngles: Float
        get() = if (type == TYPE_OTHER) -45f else 135f
    private val sweepAngles = 90f
    private val radius: Float
        get() = DensityUtil.dp2px(context, 4f).toFloat()

    private val animator: ValueAnimator = ValueAnimator.ofInt(0, 3).apply {
        duration = 900L
        interpolator = DecelerateInterpolator()
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.RESTART
        addUpdateListener { animation ->
            currentArcIndex = animation.animatedValue as Int
            invalidate()
        }
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, android.R.color.darker_gray)
        textSize = DensityUtil.sp2px(context, 12f).toFloat()
    }

    private val paintFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = waveColor
    }

    private val paintStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        color = waveColor
    }

    fun setType(type: Int) {
        this.type = type
        textPaint.textAlign = if (type == TYPE_OTHER) Paint.Align.LEFT else Paint.Align.RIGHT
        invalidate()
    }

    fun setText(text: String?) {
        this.text = text
        invalidate()
    }

    fun setWaveColor(color: Int) {
        waveColor = color
        paintFill.color = color
        paintStroke.color = color
        invalidate()
    }

    /**
     * 播放本地文件（持久文件，播完不删除）。
     */
    fun start(filePath: String) {
        toggleOrPlay(filePath = filePath, deleteOnComplete = false)
    }

    /**
     * 兼容旧 API：Base64 写入 cache 临时文件后播放，播完删除。
     */
    fun start(messageId: String, extension: String?, base64: String?) {
        try {
            val bytes = Base64Util.decode(base64)
            val ext = extension ?: ".amr"
            val temp = File(context.cacheDir, messageId + ext)
            FileOutputStream(temp).use { it.write(bytes) }
            toggleOrPlay(filePath = temp.absolutePath, deleteOnComplete = true, tempFile = temp)
        } catch (e: Exception) {
            log.e(TAG, "base64 play failed: $e")
            Toast.makeText(context, "播放失败", Toast.LENGTH_SHORT).show()
            stopPlaybackUi()
        }
    }

    private fun toggleOrPlay(
        filePath: String,
        deleteOnComplete: Boolean,
        tempFile: File? = null
    ) {
        if (animator.isRunning) {
            stopPlaybackFully()
            return
        }
        try {
            this.deleteOnComplete = deleteOnComplete
            playingTempFile = tempFile
            animator.start()
            val player = MediaPlayer()
            mediaPlayer = player
            player.setDataSource(filePath)
            player.setOnCompletionListener {
                stopPlaybackFully()
            }
            player.setOnErrorListener { _, _, _ ->
                stopPlaybackFully()
                true
            }
            player.prepare()
            player.start()
        } catch (e: Exception) {
            log.e(TAG, "play failed: $e")
            Toast.makeText(context, "播放失败", Toast.LENGTH_SHORT).show()
            stopPlaybackFully()
        }
    }

    private fun stopPlaybackUi() {
        if (animator.isRunning) {
            animator.cancel()
        }
        currentArcIndex = -1
        invalidate()
    }

    private fun stopPlaybackFully() {
        stopPlaybackUi()
        try {
            mediaPlayer?.stop()
        } catch (_: Exception) {
        }
        try {
            mediaPlayer?.release()
        } catch (_: Exception) {
        }
        mediaPlayer = null
        if (deleteOnComplete) {
            playingTempFile?.delete()
        }
        playingTempFile = null
        deleteOnComplete = false
    }

    override fun onDetachedFromWindow() {
        stopPlaybackFully()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerY = height / 2f
        val label = text ?: "0″"
        textPaint.textAlign = if (type == TYPE_OTHER) Paint.Align.LEFT else Paint.Align.RIGHT
        val textWidth = textPaint.measureText(label)
        val fontMetrics = textPaint.fontMetrics
        val distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
        val baseline = centerY + distance

        if (type == TYPE_MINE) {
            canvas.drawText(label, (width - paddingEnd).toFloat(), baseline, textPaint)
        } else {
            canvas.drawText(label, paddingStart.toFloat(), baseline, textPaint)
        }

        val arcStartX: Float = if (type == TYPE_MINE) {
            width - textWidth - paddingEnd - DensityUtil.dp2px(context, 8f)
        } else {
            textWidth + paddingStart + DensityUtil.dp2px(context, 8f)
        }

        when (currentArcIndex) {
            0 -> drawArcLevel(canvas, arcStartX, centerY, 1, fill = true)
            1 -> {
                drawArcLevel(canvas, arcStartX, centerY, 1, fill = true)
                drawArcLevel(canvas, arcStartX, centerY, 2, fill = false)
            }
            2 -> {
                drawArcLevel(canvas, arcStartX, centerY, 1, fill = true)
                drawArcLevel(canvas, arcStartX, centerY, 2, fill = false)
                drawArcLevel(canvas, arcStartX, centerY, 3, fill = false)
            }
            else -> {
                for (i in 1..3) {
                    drawArcLevel(canvas, arcStartX, centerY, i, fill = i == 1)
                }
            }
        }
    }

    private fun drawArcLevel(
        canvas: Canvas,
        arcStartX: Float,
        centerY: Float,
        level: Int,
        fill: Boolean
    ) {
        path.reset()
        val r = radius * level
        path.addArc(
            arcStartX - r,
            centerY - r,
            arcStartX + r,
            centerY + r,
            startAngles,
            sweepAngles
        )
        if (fill) {
            path.lineTo(arcStartX, centerY)
            path.close()
            canvas.drawPath(path, paintFill)
        } else {
            canvas.drawPath(path, paintStroke)
        }
    }
}
