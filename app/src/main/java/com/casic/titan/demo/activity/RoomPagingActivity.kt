package com.casic.titan.demo.activity

import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.casic.titan.demo.bean.UseCase
import com.casic.titan.demo.fragment.RoomSmartPagingFragment
import dagger.hilt.android.AndroidEntryPoint
import pers.fz.mvvm.base.BaseSearchActivity
import pers.fz.mvvm.util.common.DensityUtil
import pers.fz.mvvm.util.common.DrawableUtil
import pers.fz.mvvm.viewmodel.EmptyViewModel

@AndroidEntryPoint
class RoomPagingActivity : BaseSearchActivity<EmptyViewModel>() {

    private var useCase: UseCase? = null
    private val mCurrentFragment by lazy {
        RoomSmartPagingFragment()
    }
    override fun setTitleBar() = ""
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        supportFragmentManager.beginTransaction().add(pers.fz.mvvm.R.id.search_view_container, mCurrentFragment).commit()
        binding.searchView.search.setBackgroundDrawable(DrawableUtil.createShapeDrawable(
            ContextCompat.getColor(this,pers.fz.mvvm.R.color.themeColor), DensityUtil.dp2px(this,6f).toFloat()))
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