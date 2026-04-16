package io.coderf.arklab.demo.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.navigation.Navigation.findNavController
import io.coderf.arklab.demo.adapter.PagingHeaderDemoAdapter
import io.coderf.arklab.demo.bean.NotificationMessageBean
import io.coderf.arklab.demo.viewmodel.DemoPagingViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.coderf.arklab.common.base.BasePagingAdapter
import io.coderf.arklab.common.base.BasePagingFragment
import io.coderf.arklab.common.databinding.PagingRecyclerViewBinding
import io.coderf.arklab.common.widget.dialog.ConfirmDialog

/**
 * Created by fz on 2023/12/1 16:40
 * describe :
 */
@AndroidEntryPoint
class DemoPagingFragment :
    BasePagingFragment<DemoPagingViewModel, PagingRecyclerViewBinding, NotificationMessageBean>() {
    override fun getRecyclerAdapter(): BasePagingAdapter<NotificationMessageBean?, *> {
        return PagingHeaderDemoAdapter()
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
    }

    override fun initData(bundle: Bundle?) {
        super.initData(bundle)
        mViewModel?.items?.observe(this, observer)
    }

    override fun onItemClick(view: View, item: NotificationMessageBean, position: Int) {
        super.onItemClick(view, item, position)
        showToast("点击的是第" + position + "行，内容是：" + item.title)
//        val bundle = Bundle()
//        bundle.putString(PagingDetailActivity.ARGS, item.title)
//        bundle.putInt(PagingDetailActivity.LINE, position)
//        findNavController(view).navigate(
//            R.id.navigate_to_paging_detail,
//            bundle
//        )
        val navigate = DemoPagingFragmentDirections.navigateToPagingDetail(item.title, position)
        findNavController(view).navigate(navigate)
    }

    override fun onItemLongClick(view: View?, item: NotificationMessageBean?, position: Int) {
        super.onItemLongClick(view, item, position)
        ConfirmDialog(requireContext())
            .setPositiveText("确认删除")
            .setMessage("是否确认删除此行？")
            .setOnPositiveClickListener { dialog: Dialog? ->
                adapter.notifyItemRemoved(
                    position
                )
            }
            .builder()
            .show()
    }
}
