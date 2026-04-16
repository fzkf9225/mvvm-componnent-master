package io.coderf.arklab.demo.activity

import android.os.Bundle
import io.coderf.arklab.demo.R
import io.coderf.arklab.demo.databinding.ActivityPagingDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import io.coderf.arklab.common.base.BaseActivity
import io.coderf.arklab.common.viewmodel.EmptyViewModel

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