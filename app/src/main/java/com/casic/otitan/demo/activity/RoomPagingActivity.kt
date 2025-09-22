package com.casic.otitan.demo.activity

import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.casic.otitan.demo.bean.UseCase
import com.casic.otitan.demo.fragment.RoomSmartPagingFragment
import dagger.hilt.android.AndroidEntryPoint
import com.casic.otitan.common.base.BaseSearchActivity
import com.casic.otitan.common.utils.common.DensityUtil
import com.casic.otitan.common.utils.common.DrawableUtil
import com.casic.otitan.common.viewmodel.EmptyViewModel

@AndroidEntryPoint
class RoomPagingActivity : BaseSearchActivity<EmptyViewModel>() {

    private var useCase: UseCase? = null
    private val mCurrentFragment by lazy {
        RoomSmartPagingFragment()
    }
    override fun setTitleBar() = ""
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        supportFragmentManager.beginTransaction().add(com.casic.otitan.common.R.id.search_view_container, mCurrentFragment).commit()
        binding.searchView.search.setBackgroundDrawable(DrawableUtil.createShapeDrawable(
            ContextCompat.getColor(this,com.casic.otitan.common.R.color.themeColor), DensityUtil.dp2px(this,6f).toFloat()))
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