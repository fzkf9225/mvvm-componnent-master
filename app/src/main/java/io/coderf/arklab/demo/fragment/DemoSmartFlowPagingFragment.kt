package io.coderf.arklab.demo.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import io.coderf.arklab.common.base.BaseSmartPagingFragment
import io.coderf.arklab.common.databinding.BaseSmartPagingBinding
import io.coderf.arklab.common.widget.dialog.ConfirmDialog
import io.coderf.arklab.demo.adapter.PagingDemoAdapter
import io.coderf.arklab.demo.bean.NotificationMessageBean
import io.coderf.arklab.demo.viewmodel.DemoFlowPagingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Created by fz on 2023/12/1 16:40
 * describe :
 */
@AndroidEntryPoint
class DemoSmartFlowPagingFragment :
    BaseSmartPagingFragment<DemoFlowPagingViewModel, BaseSmartPagingBinding, NotificationMessageBean>() {
    override fun getRecyclerAdapter() = PagingDemoAdapter()

    override fun initData(bundle: Bundle?) {
        super.initData(bundle)
        lifecycleScope.launch {
            mViewModel.dataFlow.collect { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }

    override fun onItemClick(view: View?, item: NotificationMessageBean, position: Int) {
        super.onItemClick(view, item, position)
//        showToast("点击的是第" + position + "行，内容是：" + item.title)
        lifecycleScope.launch {
            mViewModel.getInfoById(item.id)?.collect {
                showToast("点击的是第" + position + "行，内容是：" + it.title)
            }
        }
    }

    override fun onItemLongClick(view: View?, item: NotificationMessageBean?, position: Int) {
        super.onItemLongClick(view, item, position)
        //不可以这样删除，因为这样值删除了列表项，但是真是数据没有删除
        ConfirmDialog(requireContext())
            .setPositiveText("确认删除")
            .setMessage("是否确认删除此行？")
            .setOnPositiveClickListener { dialog: Dialog? ->
                adapter.notifyItemRemoved(position)
            }
            .builder()
            .show()
    }
}
