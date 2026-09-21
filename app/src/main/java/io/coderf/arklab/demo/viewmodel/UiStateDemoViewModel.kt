package io.coderf.arklab.demo.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.coderf.arklab.common.base.BaseRepository
import io.coderf.arklab.common.base.BaseViewModel
import io.coderf.arklab.core.ui.state.UiState
import io.coderf.arklab.core.ui.state.UiStateHolder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UiState 演示 ViewModel。
 *
 * 用 [UiStateHolder] 表达整页内容状态（骨架 / 列表 / 空 / 错误），
 * 与 [io.coderf.arklab.core.request.RequestUi]（遮罩 Loading、Toast）正交。
 */
@HiltViewModel
class UiStateDemoViewModel @Inject constructor(
    application: Application
) : BaseViewModel<BaseRepository>(application) {

    private val holder = UiStateHolder<List<String>>(UiState.Loading("首次加载…"))

    /** 页面收集此 Flow 即可驱动内容区。 */
    val uiState = holder.state

    override fun createRepository(): BaseRepository? = null

    /** 模拟成功：返回若干条数据。 */
    fun loadSuccess() {
        viewModelScope.launch {
            holder.loading("加载中…")
            delay(MOCK_DELAY_MS)
            holder.success(
                listOf(
                    "UiState.Success — 有数据可展示",
                    "与 RequestUi 的遮罩 Loading 无关",
                    "配置变更后 StateFlow 仍保留当前状态",
                    "适合列表 / 详情整页内容区"
                )
            )
        }
    }

    /** 模拟空数据。 */
    fun loadEmpty() {
        viewModelScope.launch {
            holder.loading("加载中…")
            delay(MOCK_DELAY_MS)
            holder.empty("暂无数据，点下方按钮重试")
        }
    }

    /** 模拟失败。 */
    fun loadError() {
        viewModelScope.launch {
            holder.loading("加载中…")
            delay(MOCK_DELAY_MS)
            holder.error(
                message = "网络异常，请稍后重试",
                retryable = true
            )
        }
    }

    /**
     * 仅演示 RequestUi 遮罩（不改变 UiState）。
     * 页面可通过 showLoading 走 BaseActivity 的 RequestUi。
     */
    fun currentStateLabel(): String = when (val s = holder.value) {
        is UiState.Loading -> "Loading(${s.message ?: ""})"
        is UiState.Success -> "Success(size=${s.data.size})"
        is UiState.Empty -> "Empty(${s.message ?: ""})"
        is UiState.Error -> "Error(${s.message ?: ""})"
    }

    companion object {
        private const val MOCK_DELAY_MS = 800L
    }
}
