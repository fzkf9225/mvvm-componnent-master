package com.casic.otitan.demo.activity

import android.os.Build
import android.os.Bundle
import com.casic.otitan.demo.R
import com.casic.otitan.demo.bean.UseCase
import com.casic.otitan.demo.databinding.ActivityDemoPagingBinding
import dagger.hilt.android.AndroidEntryPoint
import com.casic.otitan.common.base.BaseActivity
import com.casic.otitan.common.viewmodel.EmptyViewModel

@AndroidEntryPoint
class DemoPagingActivity : BaseActivity<EmptyViewModel?, ActivityDemoPagingBinding?>() {
    private var useCase: UseCase? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_demo_paging
    }

    override fun setTitleBar(): String? {
        return null
    }

    override fun initView(savedInstanceState: Bundle?) {
    }

    override fun initData(bundle: Bundle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            useCase = bundle.getParcelable("args", UseCase::class.java)
        } else {
            useCase = bundle.getParcelable("args")
        }
        toolbarBind.toolbarConfig?.title = useCase?.name
    }
}