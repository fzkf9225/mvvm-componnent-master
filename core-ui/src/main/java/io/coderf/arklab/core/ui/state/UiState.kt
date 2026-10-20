package io.coderf.arklab.core.ui.state

/**
 * 页面级 UI 状态（Loading / 内容 / 空 / 错误）。
 *
 * 与请求过程 UI（[io.coderf.arklab.core.request.RequestUi] Loading Dialog）正交：
 * - RequestUi：短时遮罩、Toast、业务码
 * - UiState：整页内容区状态（列表空态、错误重试区等）
 *
 * 新页面可用单一 [kotlinx.coroutines.flow.StateFlow] 暴露本类型；旧 LiveData 路径不受影响。
 *
 * @author fz
 * @version 1.0
 * @since 1.2.0
 */
sealed class UiState<out T> {

    /** 初始或主动进入的加载中（整页骨架 / 占位）。 */
    data class Loading(
        val message: String? = null
    ) : UiState<Nothing>()

    /** 成功持有数据。 */
    data class Success<T>(
        val data: T
    ) : UiState<T>()

    /** 空数据（列表无项、详情无内容等）。 */
    data class Empty(
        val message: String? = null
    ) : UiState<Nothing>()

    /** 错误；[retryable] 供 UI 展示重试按钮。 */
    data class Error(
        val message: String? = null,
        val throwable: Throwable? = null,
        val retryable: Boolean = true
    ) : UiState<Nothing>()

    /** 便捷判断：是否 Loading。 */
    val isLoading: Boolean get() = this is Loading
    /** 便捷判断：是否 Success。 */
    val isSuccess: Boolean get() = this is Success
    /** 便捷判断：是否 Empty。 */
    val isEmpty: Boolean get() = this is Empty
    /** 便捷判断：是否 Error。 */
    val isError: Boolean get() = this is Error

    /** Success 时取数据，否则 null。 */
    fun dataOrNull(): T? = (this as? Success)?.data

    companion object {
        /** 构造 Success 状态。 */
        @JvmStatic
        fun <T> success(data: T): UiState<T> = Success(data)

        /** 构造 Loading 状态。 */
        @JvmStatic
        fun loading(message: String? = null): UiState<Nothing> = Loading(message)

        /** 构造 Empty 状态。 */
        @JvmStatic
        fun empty(message: String? = null): UiState<Nothing> = Empty(message)

        /** 构造 Error 状态。 */
        @JvmStatic
        @JvmOverloads
        fun error(
            message: String? = null,
            throwable: Throwable? = null,
            retryable: Boolean = true
        ): UiState<Nothing> = Error(message, throwable, retryable)
    }
}
