package io.coderf.arklab.common.base

import android.text.TextUtils
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.coderf.arklab.core.request.AppError
import io.coderf.arklab.core.request.RequestUi
import kotlinx.coroutines.launch

/**
 * 将 [NetworkRequestUiHost] 派发到页面实现的 [RequestUi]。
 *
 * 由 [BaseActivity] / [BaseFragment] 在创建 ViewModel 后自动调用。
 * Loading 收集 [NetworkRequestUiHost.loadingState]，页面回到前台时拿到当前遮罩。
 * Toast / 业务码仍观察 LiveData，并先经 [UiEvent.getContentIfNotHandled] 取走。
 *
 * @author fz
 * @version 2.2
 * @since 1.0
 * @updated 2026/11/20
 */
object NetworkRequestUiBinder {

    /**
     * 订阅 Host 并转发到页面侧 [RequestUi]。
     * 随 [owner] 销毁解除订阅。
     */
    @JvmStatic
    fun bind(
        owner: LifecycleOwner,
        host: NetworkRequestUiHost,
        ui: RequestUi,
    ) {
        owner.lifecycleScope.launch {
            owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                host.loadingState.collect { state ->
                    if (state.visible) {
                        ui.showLoading(
                            state.message.orEmpty(),
                            state.enableDynamicEllipsis,
                            state.cancelPolicy,
                        )
                    } else {
                        ui.hideLoading()
                    }
                }
            }
        }
        host.toast.observe(owner) { event ->
            if (event == null) {
                return@observe
            }
            val msg = event.getContentIfNotHandled()
            if (!TextUtils.isEmpty(msg)) {
                ui.showError(AppError.Unknown(msg!!, null))
            }
        }
        host.errorCode.observe(owner) { event ->
            if (event == null) {
                return@observe
            }
            val model = event.getContentIfNotHandled() ?: return@observe
            ui.onBusinessCode(model.code.orEmpty(), model.message.orEmpty())
        }
    }
}
