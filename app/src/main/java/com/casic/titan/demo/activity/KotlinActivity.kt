package com.casic.titan.demo.activity

import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import com.casic.titan.demo.R
import com.casic.titan.demo.databinding.ActivityKotlinBinding
import com.casic.titan.demo.viewmodel.KotlinViewModel
import dagger.hilt.android.AndroidEntryPoint
import pers.fz.mvvm.base.BaseView
import pers.fz.mvvm.base.kotlin.BaseActivity

@AndroidEntryPoint
class KotlinActivity : BaseActivity<KotlinViewModel, ActivityKotlinBinding, BaseView>() {
    override val layoutId = R.layout.activity_kotlin

    override fun setTitleBar(): String = "继承自kotlin的Base"

    override fun initView(savedInstanceState: Bundle?) {
        //如果xml中是androidx.fragment.app.FragmentContainerView，则使用这种方式获取navController
        val navHostFragment = binding?.kotlinFragmentView?.getFragment<NavHostFragment>()
        //如果xml是fragment则使用这种方式获取navController
//        val navController =  Navigation.findNavController(this, R.id.kotlin_fragment_view)

        binding?.buttonKotlinRequest?.setOnClickListener {
            navHostFragment?.navController?.navigate(R.id.request_kotlin_layout_page)
        }
        binding?.buttonKotlinRecyclerView?.setOnClickListener {
            navHostFragment?.navController?.navigate(R.id.recyclerview_kotlin_layout_page)
        }
    }

    override fun initData(bundle: Bundle?) {

    }


}