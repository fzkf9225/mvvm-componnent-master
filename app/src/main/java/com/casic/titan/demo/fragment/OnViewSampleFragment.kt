package com.casic.titan.demo.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.navigation.Navigation.findNavController
import com.casic.titan.demo.activity.RecyclerViewSampleActivity
import com.casic.titan.demo.adapter.RecyclerViewSampleAdapter
import com.casic.titan.demo.viewmodel.RecyclerViewSampleViewModel
import dagger.hilt.android.AndroidEntryPoint
import pers.fz.mvvm.base.BaseRecyclerViewAdapter
import pers.fz.mvvm.base.BaseRecyclerViewFragment
import pers.fz.mvvm.bean.PopupWindowBean
import pers.fz.mvvm.databinding.OptionTextViewBinding
import pers.fz.mvvm.databinding.SmartrecyclerviewBinding
import pers.fz.mvvm.listener.OnDialogInterfaceClickListener
import pers.fz.mvvm.listener.OnHeaderViewClickListener
import pers.fz.mvvm.widget.dialog.ConfirmDialog

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