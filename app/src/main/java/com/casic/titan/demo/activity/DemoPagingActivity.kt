package com.casic.titan.demo.activity

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.casic.titan.demo.R
import com.casic.titan.demo.bean.UseCase
import com.casic.titan.demo.databinding.ActivityDemoPagingBinding
import com.casic.titan.demo.fragment.DemoPagingFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import pers.fz.mvvm.base.BaseActivity
import pers.fz.mvvm.viewmodel.EmptyViewModel

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