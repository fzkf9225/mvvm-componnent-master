package io.coderf.arklab.demo.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import io.coderf.arklab.common.base.BaseActivity
import io.coderf.arklab.core.ui.state.UiState
import io.coderf.arklab.core.ui.state.collectUiState
import io.coderf.arklab.demo.R
import io.coderf.arklab.demo.databinding.ActivityUiStateDemoBinding
import io.coderf.arklab.demo.viewmodel.UiStateDemoViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * UiState 用法演示：整页内容状态 vs RequestUi 遮罩。
 *
 * - 上方内容区由 [UiState] 驱动（Loading / Success / Empty / Error）
 * - 「仅 RequestUi 遮罩」只调 [showLoading]/[hideLoading]，不改变 UiState
 */
@AndroidEntryPoint
class UiStateDemoActivity :
    BaseActivity<UiStateDemoViewModel, ActivityUiStateDemoBinding>() {

    private val listAdapter = SimpleStringAdapter()

    override fun getLayoutId(): Int = R.layout.activity_ui_state_demo

    override fun setTitleBar(): String = "UiState 演示"

    override fun initView(savedInstanceState: Bundle?) {
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = listAdapter

        binding.btnSuccess.setOnClickListener { mViewModel.loadSuccess() }
        binding.btnEmpty.setOnClickListener { mViewModel.loadEmpty() }
        binding.btnError.setOnClickListener { mViewModel.loadError() }
        binding.btnRetry.setOnClickListener { mViewModel.loadSuccess() }
        binding.btnRequestUi.setOnClickListener { demoRequestUiOnly() }

        collectUiState(mViewModel.uiState) { state ->
            render(state)
        }
    }

    override fun initData(bundle: Bundle?) {
        // 仅在尚未有业务数据时拉一次（配置变更后 ViewModel 仍在，不重复请求）
        if (mViewModel.uiState.value is UiState.Loading) {
            mViewModel.loadSuccess()
        }
    }

    private fun render(state: UiState<List<String>>) {
        binding.tvState.text = "当前 UiState：${mViewModel.currentStateLabel()}"
        when (state) {
            is UiState.Loading -> {
                binding.progress.visibility = View.VISIBLE
                binding.recycler.visibility = View.GONE
                binding.emptyPanel.visibility = View.GONE
                binding.errorPanel.visibility = View.GONE
            }
            is UiState.Success -> {
                binding.progress.visibility = View.GONE
                binding.recycler.visibility = View.VISIBLE
                binding.emptyPanel.visibility = View.GONE
                binding.errorPanel.visibility = View.GONE
                listAdapter.submit(state.data)
            }
            is UiState.Empty -> {
                binding.progress.visibility = View.GONE
                binding.recycler.visibility = View.GONE
                binding.emptyPanel.visibility = View.VISIBLE
                binding.errorPanel.visibility = View.GONE
                binding.tvEmpty.text = state.message ?: "暂无数据"
            }
            is UiState.Error -> {
                binding.progress.visibility = View.GONE
                binding.recycler.visibility = View.GONE
                binding.emptyPanel.visibility = View.GONE
                binding.errorPanel.visibility = View.VISIBLE
                binding.tvError.text = state.message ?: "加载失败"
                binding.btnRetry.visibility =
                    if (state.retryable) View.VISIBLE else View.GONE
            }
        }
    }

    /**
     * 只走 RequestUi：遮罩 Loading，内容区 UiState 不变。
     * 用于对比「请求过程反馈」与「整页内容状态」。
     */
    private fun demoRequestUiOnly() {
        showLoading("RequestUi 遮罩中…", true)
        lifecycleScope.launch {
            delay(1200)
            hideLoading()
            showToast("遮罩已关闭，UiState 未变：${mViewModel.currentStateLabel()}")
        }
    }

    /** 演示用轻量 Adapter，避免 ArrayAdapter 与 RecyclerView 不兼容。 */
    private class SimpleStringAdapter : RecyclerView.Adapter<SimpleStringAdapter.VH>() {
        private val items = mutableListOf<String>()

        @SuppressLint("NotifyDataSetChanged")
        fun submit(data: List<String>) {
            items.clear()
            items.addAll(data)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.text.text = items[position]
        }

        override fun getItemCount(): Int = items.size

        class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val text: TextView = itemView.findViewById(android.R.id.text1)
        }
    }
}
