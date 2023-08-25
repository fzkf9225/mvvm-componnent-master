package com.casic.titan.demo.adapter

import android.content.Context
import pers.fz.mvvm.R
import pers.fz.mvvm.base.BaseRecyclerViewAdapter
import pers.fz.mvvm.base.BaseViewHolder
import pers.fz.mvvm.bean.PopupWindowBean
import pers.fz.mvvm.databinding.OptionTextViewBinding

/**
 * Created by fz on 2023/8/14 10:39
 * describe :
 */
class RecyclerViewKotlinAdapter :
    BaseRecyclerViewAdapter<PopupWindowBean<Any?>?, OptionTextViewBinding?> {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, list: List<PopupWindowBean<Any?>?>?) : super(context, list)

    override fun onBindHolder(holder: BaseViewHolder<OptionTextViewBinding?>?, pos: Int) {
        holder?.binding?.item = mList[pos]
    }

    override fun getLayoutId(): Int {
        return R.layout.option_text_view
    }
}