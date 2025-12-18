package com.casic.otitan.demo.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.casic.otitan.common.base.BaseSmartPagingFragment
import com.casic.otitan.common.databinding.BaseSmartPagingBinding
import com.casic.otitan.common.widget.dialog.ConfirmDialog
import com.casic.otitan.demo.adapter.PagingDemoAdapter
import com.casic.otitan.demo.bean.NotificationMessageBean
import com.casic.otitan.demo.viewmodel.DemoFlowPagingViewModel
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
