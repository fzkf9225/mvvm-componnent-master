package io.coderf.arklab.core.request

/**
 * 遮罩 Loading 的用户取消策略。
 * <p>
 * 由 [io.coderf.arklab.core.bean.RequestOptions] /
 * [io.coderf.arklab.common.bean.ApiRequestOptions] /
 * [io.coderf.arklab.common.bean.RoomRequestOptions] 声明，
 * 经 [io.coderf.arklab.common.base.NetworkRequestUiHost] 下发到 Dialog。
 * 用户取消后由页面回调 [io.coderf.arklab.common.base.BaseViewModel.cancelUiRequests]，
 * 不反向侵入 Repository。
 *
 * @author fz
 * @version 1.0
 * @since 1.2.0
 * @created 2026/11/12
 */
enum class LoadingCancelPolicy {
    /**
     * 不可取消：屏蔽系统返回、手势返回与点外侧（默认，兼容历史行为）。
     * 适用于登录提交、支付、关键写操作。
     */
    None,

    /**
     * 可取消当前请求，停留在当前页。
     * 适用于按钮触发的查询 / 刷新。
     */
    CancelRequest,

    /**
     * 可取消当前请求，并离开当前页（Activity finish / Fragment 回退）。
     * 适用于进入页立刻拉数且可中断的场景。
     */
    CancelRequestAndExit;

    /** 是否允许用户通过返回 / 点外侧关闭 Loading。 */
    fun isCancelable(): Boolean = this != None

    /** 取消后是否应离开当前页。 */
    fun shouldExitOnCancel(): Boolean = this == CancelRequestAndExit

    companion object {
        /**
         * 取更严格的策略：[None] > [CancelRequest] > [CancelRequestAndExit]。
         * 多请求并行遮罩时，只要有一个不可取消，整体不可取消。
         */
        @JvmStatic
        fun stricter(a: LoadingCancelPolicy?, b: LoadingCancelPolicy?): LoadingCancelPolicy {
            val left = a ?: None
            val right = b ?: None
            return if (left.ordinal <= right.ordinal) left else right
        }
    }
}
