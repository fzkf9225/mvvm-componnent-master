package com.casic.titan.demo.activity

import android.os.Build
import android.os.Bundle
import com.casic.titan.demo.R
import com.casic.titan.demo.bean.UseCase
import com.casic.titan.demo.databinding.ActivityRoomPagingBinding
import com.casic.titan.demo.fragment.RoomSmartPagingFragment
import com.casic.titan.demo.viewmodel.DemoRoomPagingViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.core.Flowable
import pers.fz.mvvm.base.BaseActivity
import pers.fz.mvvm.base.BaseSearchActivity

@AndroidEntryPoint
class RoomPagingActivity : BaseSearchActivity<DemoRoomPagingViewModel>() {

    private var useCase: UseCase? = null
    private val mCurrentFragment by lazy {
        RoomSmartPagingFragment()
    }
    override fun setTitleBar() = ""
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        supportFragmentManager.beginTransaction().add(pers.fz.mvvm.R.id.search_view_container, mCurrentFragment).commit()
    }

    override fun initData(bundle: Bundle?) {
        useCase = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle?.getParcelable<UseCase>("args", UseCase::class.java)
        } else {
            bundle?.getParcelable<UseCase>("args")
        }
        toolbarBind.toolbarConfig?.setTitle(useCase?.name)
        keywordsLiveData.observe(this){
            mCurrentFragment.searcher(it)
        }
    }

}