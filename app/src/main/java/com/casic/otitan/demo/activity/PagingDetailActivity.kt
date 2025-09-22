package com.casic.otitan.demo.activity

import android.os.Bundle
import com.casic.otitan.demo.R
import com.casic.otitan.demo.databinding.ActivityPagingDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import com.casic.otitan.common.base.BaseActivity
import com.casic.otitan.common.viewmodel.EmptyViewModel

@AndroidEntryPoint
class PagingDetailActivity : BaseActivity<EmptyViewModel, ActivityPagingDetailBinding>() {
    override fun getLayoutId(): Int {
        return R.layout.activity_paging_detail
    }

    override fun setTitleBar(): String {
        return "详情"
    }

    override fun initView(savedInstanceState: Bundle?) {
    }

    override fun initData(bundle: Bundle?) {
        val args = PagingDetailActivityArgs.fromBundle(intent.extras ?: Bundle())
        val title: String = args.args
        val position: Int = args.line
        binding.tvArgs.text = "这是第${position}个页面传递过来的参数：$title"
    }

}