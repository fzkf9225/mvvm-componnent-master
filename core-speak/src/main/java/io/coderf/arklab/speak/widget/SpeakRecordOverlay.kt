package io.coderf.arklab.speak.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import io.coderf.arklab.core.utils.DensityUtil
import io.coderf.arklab.speak.model.SpeakHitZone
import io.coderf.arklab.speak.style.SpeakUiStyle
import kotlin.math.abs

/**
 * 仿微信输入法语音面板（底部 IME 区域构图）。
 *
 * 由 [SpeakButton] 全屏 Dialog 承载；接入方一般无需直接实例化。
 * 视觉由 [SpeakUiStyle] 驱动（白弧高度、撤回/发送/编辑色与字号、是否启用编辑等）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/25 13:42
 */
class SpeakRecordOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private var style: SpeakUiStyle = SpeakUiStyle.defaults(context)
    private var zone: SpeakHitZone = SpeakHitZone.Edit
    private var panelMode: PanelMode = PanelMode.Waveform

    private val hintView = TextView(context)
    private val waveform = WaveformView(context)
    private val chrome = ChromeView(context)

    private var enterProgress = 0f
    private var modeProgress = 0f
    private var modeAnimator: ValueAnimator? = null

    /** 按下时本地坐标，用于上滑判定 Cancel / Send */
    private var downLocalX = 0f
    private var downLocalY = 0f

    init {
        setWillNotDraw(false)
        hintView.gravity = Gravity.CENTER
        addView(
            hintView,
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                bottomMargin = DensityUtil.dp2px(context, 210f)
            }
        )
        addView(
            waveform,
            LayoutParams(
                LayoutParams.MATCH_PARENT,
                DensityUtil.dp2px(context, 64f)
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                bottomMargin = DensityUtil.dp2px(context, 140f)
            }
        )
        addView(chrome, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        applyStyle(style)
        alpha = 0f
    }

    /** 同步样式到提示文案、波形与底部 Chrome。 */
    fun applyStyle(uiStyle: SpeakUiStyle) {
        style = uiStyle
        hintView.setTextColor(uiStyle.hintTextColor)
        hintView.textSize = pxToSp(uiStyle.hintTextSizePx)
        waveform.applyStyle(uiStyle)
        chrome.style = uiStyle
        // 按白弧高度微调提示/波形底边距
        val arcH = uiStyle.editArcHeightPx
        (hintView.layoutParams as LayoutParams).bottomMargin = (arcH + DensityUtil.dp2px(context, 98f)).toInt()
        (waveform.layoutParams as LayoutParams).bottomMargin = (arcH + DensityUtil.dp2px(context, 28f)).toInt()
        updateHint()
        chrome.invalidate()
        invalidate()
    }

    fun onTouchDown(localX: Float, localY: Float) {
        downLocalX = localX
        downLocalY = localY
    }

    fun showEnterAnimation() {
        enterProgress = 0f
        alpha = 0f
        animate().alpha(1f).setDuration(180L).setInterpolator(DecelerateInterpolator()).start()
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 260L
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                enterProgress = it.animatedValue as Float
                invalidate()
                chrome.enterProgress = enterProgress
                chrome.invalidate()
            }
            start()
        }
        val initial = if (style.editEnabled) SpeakHitZone.Edit else SpeakHitZone.Send
        setZone(initial, animate = false)
    }

    fun hideWithAnimation(end: (() -> Unit)? = null) {
        animate().alpha(0f).setDuration(150L).withEndAction {
            end?.invoke()
        }.start()
    }

    fun setAmplitude(voiceSize: Int) {
        waveform.setAmplitude(voiceSize)
    }

    /** 更新当前热区高亮（编辑 / 撤回 / 发送）。 */
    fun setZone(hitZone: SpeakHitZone, animate: Boolean = true) {
        val zoneChanged = zone != hitZone
        zone = hitZone
        chrome.setHoverZone(hitZone, animate)
        updateHint()
        val wantPills = hitZone == SpeakHitZone.Send || hitZone == SpeakHitZone.Cancel
        val targetMode = if (wantPills) PanelMode.Pills else PanelMode.Waveform
        if (targetMode != panelMode) {
            panelMode = targetMode
            animateMode(if (wantPills) 1f else 0f, animate)
        } else if (zoneChanged) {
            chrome.invalidate()
        }
    }

    /**
     * 根据相对按下点的位移解析松手热区。
     * 未明显上滑时落在 Edit（若 [SpeakUiStyle.editEnabled]）或 Send。
     */
    fun resolveZone(localX: Float, localY: Float): SpeakHitZone {
        val fallback = if (style.editEnabled) SpeakHitZone.Edit else SpeakHitZone.Send
        if (height <= 0) return fallback
        val upThreshold = DensityUtil.dp2px(context, 56f).toFloat()
        val dy = localY - downLocalY
        if (dy > -upThreshold) {
            return fallback
        }
        val midX = width / 2f
        val pivotX = if (downLocalX > 0f) (downLocalX * 0.35f + midX * 0.65f) else midX
        return if (localX < pivotX) SpeakHitZone.Cancel else SpeakHitZone.Send
    }

    private fun animateMode(target: Float, animate: Boolean) {
        modeAnimator?.cancel()
        if (!animate) {
            modeProgress = target
            applyModeProgress()
            return
        }
        modeAnimator = ValueAnimator.ofFloat(modeProgress, target).apply {
            duration = 200L
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                modeProgress = it.animatedValue as Float
                applyModeProgress()
            }
            start()
        }
    }

    private fun applyModeProgress() {
        waveform.alpha = 1f - modeProgress
        waveform.setAlphaFactor(1f - modeProgress * 0.75f)
        chrome.modeProgress = modeProgress
        chrome.invalidate()
        invalidate()
    }

    private fun updateHint() {
        hintView.text = when (zone) {
            SpeakHitZone.Edit -> if (panelMode == PanelMode.Waveform) {
                style.hintRecording
            } else {
                style.hintEdit
            }
            SpeakHitZone.Send -> if (!style.editEnabled && panelMode == PanelMode.Waveform) {
                style.hintRecording
            } else {
                style.hintSend
            }
            SpeakHitZone.Cancel -> style.hintCancel
        }
        hintView.animate().cancel()
        hintView.alpha = 0.45f
        hintView.animate().alpha(1f).setDuration(120L).start()
    }

    private fun pxToSp(px: Float): Float = px / resources.displayMetrics.scaledDensity

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return
        val scrim = ColorUtils.setAlphaComponent(
            style.panelBackground,
            (120 * enterProgress).toInt()
        )
        canvas.drawColor(scrim)
        val glowAlpha = (0.7f * enterProgress * 255).toInt().coerceIn(0, 255)
        val glow = ColorUtils.setAlphaComponent(style.glowColor, glowAlpha)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.shader = RadialGradient(
            w / 2f,
            h * 0.78f,
            w * 0.55f,
            intArrayOf(glow, ColorUtils.setAlphaComponent(style.panelBackground, 0)),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, h * 0.35f, w, h, paint)
    }

    private enum class PanelMode { Waveform, Pills }

    /**
     * 底部大半径编辑弧 + 贴边弯曲撤回/发送（悬停放大）。
     */
    private class ChromeView(context: Context) : View(context) {
        var style: SpeakUiStyle = SpeakUiStyle.defaults(context)
        var modeProgress: Float = 0f
        var enterProgress: Float = 1f

        private var hoverZone: SpeakHitZone = SpeakHitZone.Edit
        private var cancelScale = 1f
        private var sendScale = 1f
        private var cancelScaleAnim: ValueAnimator? = null
        private var sendScaleAnim: ValueAnimator? = null

        private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
        }
        private val arcPath = Path()
        private val ovalRect = RectF()

        fun setHoverZone(zone: SpeakHitZone, animate: Boolean) {
            hoverZone = zone
            val cancelTarget = if (zone == SpeakHitZone.Cancel) HOVER_SCALE else 1f
            val sendTarget = if (zone == SpeakHitZone.Send) HOVER_SCALE else 1f
            cancelScaleAnim = playScaleAnim(
                cancelScaleAnim,
                cancelScale,
                cancelTarget,
                animate
            ) { cancelScale = it; invalidate() }
            sendScaleAnim = playScaleAnim(
                sendScaleAnim,
                sendScale,
                sendTarget,
                animate
            ) { sendScale = it; invalidate() }
            invalidate()
        }

        private fun playScaleAnim(
            previous: ValueAnimator?,
            current: Float,
            target: Float,
            animate: Boolean,
            onUpdate: (Float) -> Unit
        ): ValueAnimator? {
            previous?.cancel()
            if (abs(current - target) < 0.01f) {
                onUpdate(target)
                return null
            }
            if (!animate) {
                onUpdate(target)
                return null
            }
            return ValueAnimator.ofFloat(current, target).apply {
                duration = if (target > current) 180L else 140L
                interpolator = if (target > current) {
                    OvershootInterpolator(1.4f)
                } else {
                    DecelerateInterpolator()
                }
                addUpdateListener { onUpdate(it.animatedValue as Float) }
                start()
            }
        }

        override fun onDetachedFromWindow() {
            cancelScaleAnim?.cancel()
            sendScaleAnim?.cancel()
            super.onDetachedFromWindow()
        }

        override fun onDraw(canvas: Canvas) {
            val w = width.toFloat()
            val h = height.toFloat()
            if (w <= 0f || h <= 0f) return
            drawEditArc(canvas, w, h)
            drawCurvedActions(canvas, w, h)
        }

        /**
         * 扁椭圆鼓包；高度取 [SpeakUiStyle.editArcHeightPx]。
         */
        private fun drawEditArc(canvas: Canvas, w: Float, h: Float) {
            if (!style.showEditArc) return
            val highlight = hoverZone == SpeakHitZone.Edit && style.editEnabled
            val arcAlpha = ((if (highlight) 1f else 0.55f) * enterProgress).coerceIn(0f, 1f)
            fillPaint.style = Paint.Style.FILL
            fillPaint.color = ColorUtils.setAlphaComponent(
                style.editArcBackground,
                (250 * arcAlpha).toInt()
            )

            val moundH = style.editArcHeightPx.coerceAtLeast(
                DensityUtil.dp2px(context, 64f).toFloat()
            )
            val ovalW = w * 1.55f
            val ovalH = moundH * 2.4f
            val cx = w / 2f
            val cy = h - moundH + ovalH / 2f
            val peakY = cy - ovalH / 2f
            ovalRect.set(cx - ovalW / 2f, cy - ovalH / 2f, cx + ovalW / 2f, cy + ovalH / 2f)
            arcPath.reset()
            arcPath.addOval(ovalRect, Path.Direction.CW)
            canvas.drawPath(arcPath, fillPaint)

            if (!style.editEnabled) return
            textPaint.textSize = style.editTextSizePx
            textPaint.color = if (highlight) {
                style.editTextColor
            } else {
                ColorUtils.setAlphaComponent(style.inactiveTextColor, (175 * enterProgress).toInt())
            }
            val textY = peakY + DensityUtil.dp2px(context, 26f).toFloat()
            val fm = textPaint.fontMetrics
            val baseline = textY - (fm.ascent + fm.descent) / 2f
            canvas.drawText(style.actionEdit, cx, baseline, textPaint)
        }

        /** 左右轻弯豆形按钮（±16°）。 */
        private fun drawCurvedActions(canvas: Canvas, w: Float, h: Float) {
            if (modeProgress <= 0.01f) return
            val alpha = (modeProgress * enterProgress).coerceIn(0f, 1f)
            val pillW = DensityUtil.dp2px(context, 112f).toFloat()
            val pillH = DensityUtil.dp2px(context, 46f).toFloat()
            val margin = DensityUtil.dp2px(context, 8f).toFloat()
            val cy = h - style.editArcHeightPx - DensityUtil.dp2px(context, 10f).toFloat()

            drawSideBean(
                canvas = canvas,
                cx = margin + pillW * 0.48f,
                cy = cy,
                pillW = pillW,
                pillH = pillH,
                rotationDeg = -16f,
                label = style.actionCancel,
                textColor = style.cancelTextColor,
                textSizePx = style.cancelTextSizePx,
                active = hoverZone == SpeakHitZone.Cancel,
                scale = cancelScale,
                alpha = alpha
            )
            drawSideBean(
                canvas = canvas,
                cx = w - margin - pillW * 0.48f,
                cy = cy,
                pillW = pillW,
                pillH = pillH,
                rotationDeg = 16f,
                label = style.actionSend,
                textColor = style.sendTextColor,
                textSizePx = style.sendTextSizePx,
                active = hoverZone == SpeakHitZone.Send,
                scale = sendScale,
                alpha = alpha
            )
        }

        /**
         * 轻弯豆形；文字画在描边弧视觉中心，保证居中。
         */
        private fun drawSideBean(
            canvas: Canvas,
            cx: Float,
            cy: Float,
            pillW: Float,
            pillH: Float,
            rotationDeg: Float,
            label: String,
            @androidx.annotation.ColorInt textColor: Int,
            textSizePx: Float,
            active: Boolean,
            scale: Float,
            alpha: Float
        ) {
            canvas.save()
            canvas.scale(scale, scale, cx, cy)
            canvas.rotate(rotationDeg, cx, cy)

            val localR = pillW * 2.4f
            val localCy = cy + localR * 0.96f
            // 描边弧中心线顶点（视觉中心）
            val visualCy = localCy - localR
            ovalRect.set(cx - localR, localCy - localR, cx + localR, localCy + localR)
            val halfChord = pillW * 0.48f
            val halfSweep = Math.toDegrees(
                kotlin.math.asin((halfChord / localR).toDouble().coerceIn(-1.0, 1.0))
            ).toFloat() * 2f

            strokePaint.strokeWidth = pillH
            strokePaint.color = ColorUtils.setAlphaComponent(
                style.pillBackground,
                (255 * alpha * if (active) 1f else 0.96f).toInt()
            )
            canvas.drawArc(ovalRect, 270f - halfSweep / 2f, halfSweep, false, strokePaint)

            if (active) {
                strokePaint.color = ColorUtils.setAlphaComponent(textColor, (36 * alpha).toInt())
                canvas.drawArc(ovalRect, 270f - halfSweep / 2f, halfSweep, false, strokePaint)
            }

            textPaint.textSize = textSizePx * (0.98f + (scale - 1f) * 0.4f)
            textPaint.color = if (active) {
                ColorUtils.setAlphaComponent(textColor, (255 * alpha).toInt())
            } else {
                ColorUtils.setAlphaComponent(textColor, (210 * alpha).toInt())
            }
            val fm = textPaint.fontMetrics
            val baseline = visualCy - (fm.ascent + fm.descent) / 2f
            canvas.drawText(label, cx, baseline, textPaint)

            canvas.restore()
        }

        companion object {
            private const val HOVER_SCALE = 1.16f
        }
    }
}
