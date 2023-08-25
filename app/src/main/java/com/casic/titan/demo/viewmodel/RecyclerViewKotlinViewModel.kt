package com.casic.titan.demo.viewmodel

import android.app.Application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pers.fz.mvvm.base.BaseView
import pers.fz.mvvm.base.kotlin.BaseRecyclerViewModel
import pers.fz.mvvm.bean.PopupWindowBean
import pers.fz.mvvm.bean.base.PageBean

/**
 * Created by fz on 2023/8/14 10:56
 * describe :
 */

class RecyclerViewKotlinViewModel(application: Application) :
    BaseRecyclerViewModel<BaseView, PopupWindowBean<Any?>?>(application) {

    fun loadData(mCurrentPage: Int) {
        val pageBean = PageBean<PopupWindowBean<Any?>?>()
        val dataList: MutableList<PopupWindowBean<Any?>?> = ArrayList()
        for (i in 0..19) {
            dataList.add(
                PopupWindowBean<Any?>(
                    (mCurrentPage * 20 + i).toString(),
                    "这是" + (mCurrentPage * 20 + i) + "行的数据哦！！！"
                )
            )
        }
        pageBean.setList(dataList)
        pageBean.responseCount = dataList.size
        GlobalScope.launch(Dispatchers.Main) { pageMutableStateFlow.value = pageBean}
    }
}