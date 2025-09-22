package com.casic.otitan.demo.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.navigation.Navigation.findNavController
import com.casic.otitan.demo.adapter.RecyclerViewSampleAdapter
import com.casic.otitan.demo.viewmodel.RecyclerViewSampleViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.casic.otitan.common.base.BaseRecyclerViewFragment
import com.casic.otitan.common.bean.PopupWindowBean
import com.casic.otitan.common.databinding.SmartrecyclerviewBinding
import com.casic.otitan.common.listener.OnHeaderViewClickListener
import com.casic.otitan.common.widget.dialog.ConfirmDialog

@AndroidEntryPoint
class OnViewSampleFragment :
    BaseRecyclerViewFragment<RecyclerViewSampleViewModel, SmartrecyclerviewBinding, PopupWindowBean<*>>(),
    OnHeaderViewClickListener {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
    }

    override fun initData(bundle: Bundle?) {
        super.initData(bundle)
        requestData()
    }

    override fun requestData() {
        super.requestData()
        mViewModel?.loadData(mCurrentPage)
    }

    override fun getRecyclerAdapter() = RecyclerViewSampleAdapter().apply {
        setOnItemClickListener(this@OnViewSampleFragment)
        setOnItemLongClickListener(this@OnViewSampleFragment)
        setOnHeaderViewClickListener(this@OnViewSampleFragment)
    }

    override fun onItemClick(view: View, position: Int) {
        super.onItemClick(view, position)
        showToast(
            "点击内容是：" + adapter?.list?.get(position)?.popupName
        )
        val navigate = OnViewSampleFragmentDirections.navigateToPagingDetail(
            args = adapter?.list?.get(
                position
            )?.popupName ?: "", line = position
        )
        findNavController(view).navigate(navigate)
    }

    override fun onItemLongClick(view: View?, position: Int) {
        super.onItemLongClick(view, position)
        ConfirmDialog(requireContext())
            .setPositiveText("确认删除")
            .setMessage("是否确认删除此行？")
            .setOnPositiveClickListener { dialog: Dialog? ->
                adapter?.list?.removeAt(position)
                adapter?.notifyItemRemoved(position + 1)
            }
            .builder()
            .show()
    }

    override fun onHeaderViewClick(view: View?) {
        showToast("头布局点击事件！")
    }

    override fun onHeaderViewLongClick(view: View?) {
        showToast("头布局长按事件！")
    }
}