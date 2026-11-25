package io.coderf.arklab.speak.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.graphics.ColorUtils
import io.coderf.arklab.core.utils.DensityUtil
import io.coderf.arklab.speak.style.SpeakUiStyle
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * 对称薄荷绿录音波形条。
 *
 * 接入方一般不直接使用，由 [SpeakRecordOverlay] 驱动；也可单独嵌入自定义面板。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/25 13:42
 */
class WaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /** 柱条画笔（圆角 / 端点圆点共用） */
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        strokeCap = Paint.Cap.ROUND
    }

    /** 对称轮廓相对高度（中间最高） */
    private val ratios = floatArrayOf(
        0.15f, 0.25f, 0.35f, 0.45f, 0.55f, 0.7f, 0.85f, 1f,
        0.85f, 0.7f, 0.55f, 0.45f, 0.35f, 0.25f, 0.15f
    )
    /** 当前帧绘制高度（0~1，相对 maxBar） */
    private val heights = FloatArray(ratios.size) { 0.2f }
    /** 目标高度；动画向其插值 */
    private val targets = FloatArray(ratios.size) { 0.2f }

    private var barColor: Int = SpeakUiStyle.DEFAULT_ACCENT
    /** 整体透明度系数，取消态可压暗 */
    private var alphaFactor: Float = 1f
    /** 音量归一化后的整体幅度 */
    private var level: Float = 0.3f

    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 120L
        interpolator = DecelerateInterpolator()
        addUpdateListener {
            for (i in heights.indices) {
                heights[i] = heights[i] + (targets[i] - heights[i]) * 0.35f
            }
            invalidate()
        }
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.RESTART
    }

    /** 应用面板样式中的波形色。 */
    fun applyStyle(style: SpeakUiStyle) {
        barColor = style.waveformColor
        barPaint.color = barColor
        invalidate()
    }

    /** 设置整体透明度（0~1）。 */
    fun setAlphaFactor(factor: Float) {
        alphaFactor = factor.coerceIn(0f, 1f)
        invalidate()
    }

    /**
     * 根据录音振幅刷新目标柱高。
     *
     * @param voiceSize 约 0~200 的分贝映射值（与 [io.coderf.arklab.speak.record.VoiceRecorder] 一致）
     */
    fun setAmplitude(voiceSize: Int) {
        level = (voiceSize / 200f).coerceIn(0.15f, 1f)
        for (i in targets.indices) {
            val jitter = 0.75f + Random.nextFloat() * 0.5f
            targets[i] = max(0.12f, ratios[i] * level * jitter)
        }
        if (!animator.isRunning) {
            animator.start()
        }
        invalidate()
    }

    /** 停止动画（面板关闭或 View detach 时调用）。 */
    fun quit() {
        animator.cancel()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        quit()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        val count = ratios.size
        val gap = DensityUtil.dp2px(context, 4f).toFloat()
        val barWidth = DensityUtil.dp2px(context, 3.5f).toFloat()
        val total = count * barWidth + (count - 1) * gap
        var x = (w - total) / 2f
        val cy = h / 2f
        val maxBar = h * 0.9f

        barPaint.color = ColorUtils.setAlphaComponent(
            barColor,
            (255 * alphaFactor).toInt().coerceIn(0, 255)
        )

        for (i in 0 until count) {
            val barH = max(DensityUtil.dp2px(context, 4f).toFloat(), heights[i] * maxBar)
            if (i == 0 || i == count - 1) {
                val r = min(barWidth, barH) / 2f
                canvas.drawCircle(x + barWidth / 2f, cy, r, barPaint)
            } else {
                val left = x
                val top = cy - barH / 2f
                val right = x + barWidth
                val bottom = cy + barH / 2f
                val radius = barWidth / 2f
                canvas.drawRoundRect(left, top, right, bottom, radius, radius, barPaint)
            }
            x += barWidth + gap
        }
    }
}
