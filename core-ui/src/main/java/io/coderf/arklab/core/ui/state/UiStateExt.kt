package io.coderf.arklab.core.ui.state

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 在 [LifecycleOwner] 上收集 [UiState]（STARTED 时活跃）。
 *
 * 用法：
 * ```
 * collectUiState(viewModel.uiState) { state ->
 *     when (state) {
 *         is UiState.Loading -> showSkeleton()
 *         is UiState.Success -> bind(state.data)
 *         is UiState.Empty -> showEmpty()
 *         is UiState.Error -> showError(state.message)
 *     }
 * }
 * ```
 *
 * 不强制使用；旧 LiveData 路径不受影响。
 */
fun <T> LifecycleOwner.collectUiState(
    stateFlow: StateFlow<UiState<T>>,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    collector: (UiState<T>) -> Unit
) {
    lifecycleScope.launch {
        repeatOnLifecycle(minActiveState) {
            stateFlow.collect { collector(it) }
        }
    }
}
