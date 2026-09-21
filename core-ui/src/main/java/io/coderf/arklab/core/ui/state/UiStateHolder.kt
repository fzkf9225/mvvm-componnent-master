package io.coderf.arklab.core.ui.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 可复用的 [UiState] 持有者，便于在 ViewModel 中组合而非强制继承。
 *
 * 用法（Kotlin ViewModel）：
 * ```
 * private val holder = UiStateHolder<List<Item>>(UiState.Loading())
 * val uiState: StateFlow<UiState<List<Item>>> = holder.state
 *
 * fun load() {
 *     holder.loading()
 *     // ... 请求
 *     holder.success(list)
 * }
 * ```
 *
 * 不替换现有 LiveData / RequestUi；仅作为可选能力。
 *
 * @author fz
 * @version 1.0
 * @since 1.2.0
 */
class UiStateHolder<T>(
    initial: UiState<T> = UiState.Loading()
) {
    private val _state = MutableStateFlow(initial)
    val state: StateFlow<UiState<T>> = _state.asStateFlow()

    val value: UiState<T>
        get() = _state.value

    fun set(state: UiState<T>) {
        _state.value = state
    }

    fun loading(message: String? = null) {
        _state.value = UiState.Loading(message)
    }

    fun success(data: T) {
        _state.value = UiState.Success(data)
    }

    fun empty(message: String? = null) {
        _state.value = UiState.Empty(message)
    }

    fun error(
        message: String? = null,
        throwable: Throwable? = null,
        retryable: Boolean = true
    ) {
        _state.value = UiState.Error(message, throwable, retryable)
    }

    /** 若当前为 Success，用 [transform] 更新数据；否则忽略。 */
    fun updateData(transform: (T) -> T) {
        val cur = _state.value
        if (cur is UiState.Success) {
            _state.value = UiState.Success(transform(cur.data))
        }
    }
}
