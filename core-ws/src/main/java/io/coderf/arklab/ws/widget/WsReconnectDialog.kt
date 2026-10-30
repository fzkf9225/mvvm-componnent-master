package io.coderf.arklab.ws.widget

import android.app.Dialog
import android.content.Context
import android.os.CountDownTimer
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import io.coderf.arklab.ws.R
import io.coderf.arklab.ws.databinding.DialogWsReconnectBinding

/**
 * WebSocket 重连等待弹窗：转圈 + 重试进度文案 + 可选退出按钮。
 *
 * 配合 [io.coderf.arklab.ws.WsListener.onReconnecting] 使用：
 * ```
 * dialog.updateReconnectState(attempt, maxAttempts, nextRetryDelaySeconds)
 * ```
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/30
 */
class WsReconnectDialog @JvmOverloads constructor(
    context: Context,
    private val dialogWidthDp: Int = DEFAULT_DIALOG_WIDTH_DP,
) : Dialog(context, R.style.WsReconnectDialogTheme) {

    private val binding = DialogWsReconnectBinding.inflate(layoutInflater)
    private var onExitClickListener: (() -> Unit)? = null
    private var countdownTimer: CountDownTimer? = null
    private var attempt = 0
    private var maxAttempts = 0

    init {
        setContentView(binding.root)
        setCanceledOnTouchOutside(false)
        setCancelable(false)
        binding.btnExit.setOnClickListener {
            onExitClickListener?.invoke()
        }
    }

    fun setOnExitClickListener(listener: Runnable): WsReconnectDialog {
        onExitClickListener = { listener.run() }
        return this
    }

    fun setOnExitClickListener(listener: () -> Unit): WsReconnectDialog {
        onExitClickListener = listener
        return this
    }

    fun builder(): WsReconnectDialog {
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

    override fun show() {
        super.show()
        binding.progressReconnect.show()
    }

    override fun dismiss() {
        binding.progressReconnect.hide()
        cancelCountdown()
        super.dismiss()
    }

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

    private fun cancelCountdown() {
        countdownTimer?.cancel()
        countdownTimer = null
    }

    private fun buildDetailText(
        attempt: Int,
        maxAttempts: Int,
        secondsLeft: Int,
    ): String {
        return if (secondsLeft > 0) {
            context.getString(
                R.string.ws_reconnect_detail_countdown,
                attempt,
                maxAttempts,
                secondsLeft,
            )
        } else {
            context.getString(
                R.string.ws_reconnect_detail_retrying,
                attempt,
                maxAttempts,
            )
        }
    }

    private fun dpToPx(context: Context, dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics,
        ).toInt()
    }

    companion object {
        private const val DEFAULT_DIALOG_WIDTH_DP = 300
    }
}
