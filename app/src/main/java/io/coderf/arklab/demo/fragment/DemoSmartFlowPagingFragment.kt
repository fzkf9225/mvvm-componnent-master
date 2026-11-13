package io.coderf.arklab.demo.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.coderf.arklab.common.base.BaseSmartPagingFragment
import io.coderf.arklab.common.databinding.BaseSmartPagingBinding
import io.coderf.arklab.common.widget.dialog.ConfirmDialog
import io.coderf.arklab.demo.adapter.PagingDemoAdapter
import io.coderf.arklab.demo.bean.NotificationMessageBean
import io.coderf.arklab.demo.viewmodel.DemoFlowPagingViewModel
import kotlinx.coroutines.launch

/**
 * Smart + Flow Paging Demo。
 * <p>
 * 分页 [dataFlow] 可在 Fragment collect；带遮罩 Loading 的详情请求必须由 ViewModel
 * [DemoFlowPagingViewModel.loadInfoById]（内部 [io.coderf.arklab.common.base.BaseViewModel.launchRequest]）发起，
 * 页面只 observe 结果，才能走 Loading 取消协议。
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @created 2023/12/1 16:40
 * @updated 2026/11/12
 */
@AndroidEntryPoint
class DemoSmartFlowPagingFragment :
    BaseSmartPagingFragment<DemoFlowPagingViewModel, BaseSmartPagingBinding, NotificationMessageBean>() {
    override fun getRecyclerAdapter() = PagingDemoAdapter()

    override fun initData(bundle: Bundle?) {
        super.initData(bundle)
        // 分页数据：无遮罩 Loading，可继续在 Fragment collect
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mViewModel.dataFlow.collect { pagingData ->
                    adapter.submitData(viewLifecycleOwner.lifecycle, pagingData)
                }
            }
        }
        // 详情结果：只观察，不在此 launch 请求
        mViewModel.itemClickToast.observe(viewLifecycleOwner) { toast ->
            if (toast == null) {
                return@observe
            }
            showToast("点击的是第" + toast.position + "行，内容是：" + toast.title)
        }
    }

    override fun onItemClick(view: View?, item: NotificationMessageBean, position: Int) {
        super.onItemClick(view, item, position)
        mViewModel.loadInfoById(item.id, position)
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
