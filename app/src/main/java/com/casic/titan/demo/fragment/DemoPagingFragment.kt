package com.casic.titan.demo.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.navigation.Navigation.findNavController
import com.casic.titan.demo.R
import com.casic.titan.demo.activity.PagingDetailActivity
import com.casic.titan.demo.adapter.PagingHeaderDemoAdapter
import com.casic.titan.demo.api.ApiServiceHelper
import com.casic.titan.demo.bean.NotificationMessageBean
import com.casic.titan.demo.viewmodel.DemoPagingViewModel
import dagger.hilt.android.AndroidEntryPoint
import pers.fz.mvvm.base.BasePagingAdapter
import pers.fz.mvvm.base.BasePagingFragment
import pers.fz.mvvm.databinding.PagingRecyclerViewBinding
import pers.fz.mvvm.listener.OnDialogInterfaceClickListener
import pers.fz.mvvm.widget.dialog.ConfirmDialog
import javax.inject.Inject

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
