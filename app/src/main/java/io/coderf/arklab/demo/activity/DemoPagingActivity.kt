package io.coderf.arklab.demo.activity

import android.os.Build
import android.os.Bundle
import io.coderf.arklab.demo.R
import io.coderf.arklab.demo.bean.UseCase
import io.coderf.arklab.demo.databinding.ActivityDemoPagingBinding
import dagger.hilt.android.AndroidEntryPoint
import io.coderf.arklab.common.base.BaseActivity
import io.coderf.arklab.common.viewmodel.EmptyViewModel

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