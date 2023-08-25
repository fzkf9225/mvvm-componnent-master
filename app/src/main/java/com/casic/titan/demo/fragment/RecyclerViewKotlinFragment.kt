package com.casic.titan.demo.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.View
import com.casic.titan.demo.adapter.RecyclerViewKotlinAdapter
import com.casic.titan.demo.viewmodel.RecyclerViewKotlinViewModel
import dagger.hilt.android.AndroidEntryPoint
import pers.fz.mvvm.base.BaseView
import pers.fz.mvvm.base.kotlin.BaseRecyclerViewFragment
import pers.fz.mvvm.bean.PopupWindowBean
import pers.fz.mvvm.databinding.SmartrecyclerviewBinding
import pers.fz.mvvm.wight.dialog.ConfirmDialog

/**
 * @author Titan
 */
@AndroidEntryPoint
open class RecyclerViewKotlinFragment :
    BaseRecyclerViewFragment<RecyclerViewKotlinViewModel, SmartrecyclerviewBinding?, PopupWindowBean<Any?>?, BaseView>() {
    private var recyclerViewSampleAdapter: RecyclerViewKotlinAdapter? = null
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
    }

    override fun initData(bundle: Bundle?) {
        super.initData(bundle)
        requestData()
    }

    override fun requestData() {
        super.requestData()
        mViewModel!!.loadData(mCurrentPage)
    }

    override val recyclerAdapter: RecyclerViewKotlinAdapter?
        protected get() {
            recyclerViewSampleAdapter = RecyclerViewKotlinAdapter(requireContext())
            recyclerViewSampleAdapter!!.setOnItemClickListener(this)
            recyclerViewSampleAdapter!!.setOnItemLongClickListener(this)
            return recyclerViewSampleAdapter
        }

    override fun onItemClick(view: View, position: Int) {
        super.onItemClick(view, position)
        showToast("点击内容是：" + recyclerViewSampleAdapter!!.list[position]?.name)
    }

    override fun onItemLongClick(view: View, position: Int) {
        super.onItemLongClick(view, position)
        ConfirmDialog(requireContext())
            .setSureText("确认删除")
            .setMessage("是否确认删除此行？")
            .setOnSureClickListener { dialog: Dialog? ->
                recyclerViewSampleAdapter!!.list.removeAt(position)
                recyclerViewSampleAdapter!!.notifyItemRemoved(position)
            }
            .builder()
            .show()
    }
}