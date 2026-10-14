package io.coderf.arklab.mqtt.widget

import android.app.Dialog
import android.content.Context
import android.os.CountDownTimer
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import io.coderf.arklab.mqtt.R
import io.coderf.arklab.mqtt.databinding.DialogMqttReconnectBinding

/**
 * MQTT 重连等待弹窗：转圈 + 重试进度文案 + 可选退出按钮。
 *
 * 配合 [io.coderf.arklab.mqtt.MqttListener.onReconnecting] 使用：
 * ```
 * dialog.updateReconnectState(attempt, maxAttempts, nextRetryDelaySeconds)
 * ```
 *
 * Java 接入：
 * ```
 * MqttReconnectDialog dialog = new MqttReconnectDialog(context);
 * dialog.setOnExitClickListener(() -> { ... });
 * dialog.builder().show();
 * dialog.updateReconnectState(1, 20, 5);
 * ```
 *
 * @param context 上下文
 * @param dialogWidthDp 弹窗宽度（dp），默认 300
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/7/27 10:10
 */
class MqttReconnectDialog @JvmOverloads constructor(
    context: Context,
    private val dialogWidthDp: Int = DEFAULT_DIALOG_WIDTH_DP,
) : Dialog(context, R.style.MqttReconnectDialogTheme) {

    /** ViewBinding */
    private val binding = DialogMqttReconnectBinding.inflate(layoutInflater)

    /** 退出按钮点击回调 */
    private var onExitClickListener: (() -> Unit)? = null

    /** 重试倒计时 */
    private var countdownTimer: CountDownTimer? = null

    /** 当前第几次重连 */
    private var attempt = 0

    /** 最大重连次数 */
    private var maxAttempts = 0

    init {
        setContentView(binding.root)
        setCanceledOnTouchOutside(false)
        setCancelable(false)
        binding.btnExit.setOnClickListener {
            onExitClickListener?.invoke()
        }
    }

    /** Java：设置退出按钮回调 */
    fun setOnExitClickListener(listener: Runnable): MqttReconnectDialog {
        onExitClickListener = { listener.run() }
        return this
    }

    /** Kotlin：设置退出按钮回调 */
    fun setOnExitClickListener(listener: () -> Unit): MqttReconnectDialog {
        onExitClickListener = listener
        return this
    }

    /** 配置弹窗尺寸/位置/透明背景并开启进度环，供链式 show */
    fun builder(): MqttReconnectDialog {
        window?.apply {
            setLayout(
                dpToPx(context, dialogWidthDp.toFloat()),
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            setGravity(Gravity.CENTER)
            setBackgroundDrawableResource(android.R.color.transparent)
        }
        binding.progressReconnect.isIndeterminate = true
        return this
    }

    /**
     * 更新重连进度文案并启动倒计时。
     *
     * @param attempt 当前第几次重连（从 1 开始）
     * @param maxAttempts 最大重连次数
     * @param nextRetryDelaySeconds 距下次发起连接的秒数
     */
    fun updateReconnectState(
        attempt: Int,
        maxAttempts: Int,
        nextRetryDelaySeconds: Int,
    ) {
        this.attempt = attempt
        this.maxAttempts = maxAttempts
        binding.tvDetail.text = buildDetailText(attempt, maxAttempts, nextRetryDelaySeconds)
        startCountdown(nextRetryDelaySeconds)
    }

    /** 展示时开启进度环 */
    override fun show() {
        super.show()
        binding.progressReconnect.show()
    }

    /** 关闭时停止进度环并取消倒计时 */
    override fun dismiss() {
        binding.progressReconnect.hide()
        cancelCountdown()
        super.dismiss()
    }

    /** 启动秒级倒计时刷新文案；秒数 ≤0 时直接展示「重试中」 */
    private fun startCountdown(totalSeconds: Int) {
        cancelCountdown()
        if (totalSeconds <= 0) {
            binding.tvDetail.text = buildDetailText(attempt, maxAttempts, 0)
            return
        }
        countdownTimer = object : CountDownTimer(totalSeconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = ((millisUntilFinished + 999L) / 1000L).toInt()
                binding.tvDetail.text = buildDetailText(attempt, maxAttempts, secondsLeft)
            }

            override fun onFinish() {
                binding.tvDetail.text = buildDetailText(attempt, maxAttempts, 0)
            }
        }.start()
    }

    /** 取消倒计时 */
    private fun cancelCountdown() {
        countdownTimer?.cancel()
        countdownTimer = null
    }

    /** 拼装倒计时/重试中文案 */
    private fun buildDetailText(
        attempt: Int,
        maxAttempts: Int,
        secondsLeft: Int,
    ): String {
        return if (secondsLeft > 0) {
            context.getString(
                R.string.mqtt_reconnect_detail_countdown,
                attempt,
                maxAttempts,
                secondsLeft,
            )
        } else {
            context.getString(
                R.string.mqtt_reconnect_detail_retrying,
                attempt,
                maxAttempts,
            )
        }
    }

    /** dp 转 px */
    private fun dpToPx(context: Context, dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics,
        ).toInt()
    }

    companion object {
        /** 默认弹窗宽度（dp） */
        private const val DEFAULT_DIALOG_WIDTH_DP = 300
    }
}
