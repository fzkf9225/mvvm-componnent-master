package com.casic.otitan.demo.activity

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.casic.otitan.common.base.BaseActivity
import com.casic.otitan.common.adapter.CheckBoxAdapter
import com.casic.otitan.common.bean.PopupWindowBean
import com.casic.otitan.common.utils.common.DensityUtil
import com.casic.otitan.common.viewmodel.EmptyViewModel
import com.casic.otitan.demo.R
import com.casic.otitan.demo.databinding.ActivityCheckBoxBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue


@AndroidEntryPoint
class CheckBoxActivity : BaseActivity<EmptyViewModel, ActivityCheckBoxBinding>() {

    val singleDataList: List<PopupWindowBean<*>> by lazy {
        listOf(
            PopupWindowBean<Any>("1", "选项一", true),
            PopupWindowBean<Any>("2", "选项二", true),
            PopupWindowBean<Any>("3", "选项三", true),
            PopupWindowBean<Any>("4", "选项四", true),
            PopupWindowBean<Any>("5", "选项五", true),
            PopupWindowBean<Any>("6", "选项六", true),
            PopupWindowBean<Any>("7", "选项七", true),
            PopupWindowBean<Any>("8", "选项八", true),
            PopupWindowBean<Any>("9", "选项九", true),
            PopupWindowBean<Any>("10", "选项十", true)
        )
    }

    val multiDataList: List<PopupWindowBean<*>> by lazy {
        listOf(
            PopupWindowBean<Any>("1", "选项一", false),
            PopupWindowBean<Any>("2", "选项二", false),
            PopupWindowBean<Any>("3", "选项三", false),
            PopupWindowBean<Any>("4", "选项四", false),
            PopupWindowBean<Any>("5", "选项五", false),
            PopupWindowBean<Any>("6", "选项六", false),
            PopupWindowBean<Any>("7", "选项七", false),
            PopupWindowBean<Any>("8", "选项八", false),
            PopupWindowBean<Any>("9", "选项九", false),
            PopupWindowBean<Any>("10", "选项十", false)
        )
    }

    val multiTotalDataList: List<PopupWindowBean<*>> by lazy {
        listOf(
            PopupWindowBean<Any>("1", "选项一", false),
            PopupWindowBean<Any>("2", "选项二", false),
            PopupWindowBean<Any>("3", "选项三", false),
            PopupWindowBean<Any>("4", "选项四", false),
            PopupWindowBean<Any>("5", "选项五", false),
            PopupWindowBean<Any>("6", "选项六", false),
            PopupWindowBean<Any>("7", "选项七", false),
            PopupWindowBean<Any>("8", "选项八", false),
            PopupWindowBean<Any>("9", "选项九", false),
            PopupWindowBean<Any>("10", "选项十", false)
        )
    }

    val singleAdapter by lazy {
        CheckBoxAdapter<PopupWindowBean<*>>(CheckBoxAdapter.MODE_SINGLE).apply {
//            setCheckedDrawable(checkedDrawable)
//            setUncheckedDrawable(uncheckedDrawable)
            setTextColor(
                ContextCompat.getColor(
                    this@CheckBoxActivity,
                    com.casic.otitan.common.R.color.themeColor
                )
            )
            setTextSizeSp(13f)  // 设置文字大小为13sp
            setTextMargin(
                DensityUtil.dp2px(this@CheckBoxActivity, 16f),  // 左边距16dp
                DensityUtil.dp2px(this@CheckBoxActivity, 8f),   // 上边距8dp
                DensityUtil.dp2px(this@CheckBoxActivity, 8f),   // 右边距8dp
                DensityUtil.dp2px(this@CheckBoxActivity, 8f)    // 下边距8dp
            )
            setIconMargin(
                DensityUtil.dp2px(this@CheckBoxActivity, 8f),   // 图标左边距8dp
                DensityUtil.dp2px(this@CheckBoxActivity, 16f)   // 图标右边距16dp
            )
            itemHeight = DensityUtil.dp2px(this@CheckBoxActivity, 35f).toFloat()  // 设置item高度48dp
            setList(singleDataList);
        }
    }

    val multiAdapter by lazy {
        CheckBoxAdapter<PopupWindowBean<*>>(CheckBoxAdapter.MODE_MULTI).apply {
            setTextColor(
                ContextCompat.getColor(
                    this@CheckBoxActivity,
                    com.casic.otitan.common.R.color.themeColor
                )
            )
            setTextSizeSp(13f)  // 设置文字大小为13sp
            setTextMargin(
                DensityUtil.dp2px(this@CheckBoxActivity, 16f),  // 左边距16dp
                DensityUtil.dp2px(this@CheckBoxActivity, 8f),   // 上边距8dp
                DensityUtil.dp2px(this@CheckBoxActivity, 8f),   // 右边距8dp
                DensityUtil.dp2px(this@CheckBoxActivity, 8f)    // 下边距8dp
            )
            setIconMargin(
                DensityUtil.dp2px(this@CheckBoxActivity, 8f),   // 图标左边距8dp
                DensityUtil.dp2px(this@CheckBoxActivity, 16f)   // 图标右边距16dp
            )
            itemHeight = DensityUtil.dp2px(this@CheckBoxActivity, 48f).toFloat()  // 设置item高度48dp
            setList(multiDataList);
        }
    }

    val multiTotalAdapter by lazy {
        CheckBoxAdapter<PopupWindowBean<*>>(CheckBoxAdapter.MODE_MULTI).apply {
            setTextColor(
                ContextCompat.getColor(
                    this@CheckBoxActivity,
                    com.casic.otitan.common.R.color.themeColor
                )
            )
            setShowHeader(true)
            setHeaderTextSizeSp(13f);
            setTextSizeSp(13f)  // 设置文字大小为13sp
            setTextMargin(
                DensityUtil.dp2px(this@CheckBoxActivity, 16f),  // 左边距16dp
                DensityUtil.dp2px(this@CheckBoxActivity, 8f),   // 上边距8dp
                DensityUtil.dp2px(this@CheckBoxActivity, 8f),   // 右边距8dp
                DensityUtil.dp2px(this@CheckBoxActivity, 8f)    // 下边距8dp
            )
            setIconMargin(
                DensityUtil.dp2px(this@CheckBoxActivity, 8f),   // 图标左边距8dp
                DensityUtil.dp2px(this@CheckBoxActivity, 16f)   // 图标右边距16dp
            )
            itemHeight = DensityUtil.dp2px(this@CheckBoxActivity, 48f).toFloat()  // 设置item高度48dp
            setList(multiDataList);
        }
    }

    override fun getLayoutId() = R.layout.activity_check_box
    override fun setTitleBar() = "复选框示例"
    override fun initView(savedInstanceState: Bundle?) {
        binding.rvSingle.layoutManager = LinearLayoutManager(this)
        binding.rvSingle.setAdapter(singleAdapter);
        binding.rvMulti.layoutManager = LinearLayoutManager(this)
        binding.rvMulti.setAdapter(multiAdapter);
        binding.rvMultiTotal.layoutManager = LinearLayoutManager(this)
        binding.rvMultiTotal.setAdapter(multiTotalAdapter);
    }

    override fun initData(bundle: Bundle?) {

    }
}