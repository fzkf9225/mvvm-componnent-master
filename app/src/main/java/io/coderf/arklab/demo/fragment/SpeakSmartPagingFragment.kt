package io.coderf.arklab.demo.fragment

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import io.coderf.arklab.common.base.BasePagingAdapter
import io.coderf.arklab.common.base.BaseSmartPagingFragment
import io.coderf.arklab.common.databinding.BaseSmartPagingBinding
import io.coderf.arklab.common.widget.dialog.ConfirmDialog
import io.coderf.arklab.common.widget.recyclerview.RecycleViewDivider
import io.coderf.arklab.core.bean.RequestResult
import io.coderf.arklab.core.utils.ext.dp2px
import io.coderf.arklab.demo.R
import io.coderf.arklab.demo.adapter.PagingSpeakAdapter
import io.coderf.arklab.demo.viewmodel.SpeakDemoViewModel
import io.coderf.arklab.speak.db.VoiceMessage
import io.coderf.arklab.speak.model.VoiceRecordResult
import io.coderf.arklab.speak.widget.SpeakerView

/**
 * core-speak 语音列表：Room + SmartRefresh + Paging3（对齐 [RoomSmartPagingFragment]）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/25 14:20
 */
@AndroidEntryPoint
class SpeakSmartPagingFragment :
    BaseSmartPagingFragment<SpeakDemoViewModel, BaseSmartPagingBinding, VoiceMessage>() {

    override fun getRecyclerAdapter(): BasePagingAdapter<VoiceMessage, *> {
        return PagingSpeakAdapter()
    }

    override fun initData(bundle: Bundle?) {
        super.initData(bundle)
        mViewModel.getItems().observe(viewLifecycleOwner, observer)
        onRefresh(binding.smartFreshLayout)
    }

    override fun onItemClick(view: View?, item: VoiceMessage, position: Int) {
        super.onItemClick(view, item, position)
        val path = item.filePath
        if (path.isNullOrBlank() || view == null) return
        view.findViewById<SpeakerView>(R.id.speakerView)?.start(path)
    }

    override fun onItemLongClick(view: View?, item: VoiceMessage, position: Int) {
        super.onItemLongClick(view, item, position)
        ConfirmDialog(requireContext())
            .setPositiveText("删除")
            .setNegativeText("取消")
            .setMessage("删除这条语音？")
            .setOnPositiveClickListener {
                mViewModel.delete(item) { result ->
                    if (result is RequestResult.Success) {
                        showToast("已删除")
                        onRefresh(binding.smartFreshLayout)
                    }
                }
            }
            .builder()
            .show()
    }

    /** 发送入库成功后刷新列表（与下拉刷新同入口）。 */
    fun saveSend(result: VoiceRecordResult, onDone: (() -> Unit)? = null) {
        mViewModel.saveSend(result) {
            onRefresh(binding.smartFreshLayout)
            onDone?.invoke()
        }
    }

    override fun createDivider(): RecyclerView.ItemDecoration {
        return RecycleViewDivider(requireContext(),LinearLayoutManager.VERTICAL,8.dp2px(requireContext()))
    }

    fun refreshList() {
        onRefresh(binding.smartFreshLayout)
    }
}
