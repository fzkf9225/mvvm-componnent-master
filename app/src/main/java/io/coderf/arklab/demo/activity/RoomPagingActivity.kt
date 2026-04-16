package io.coderf.arklab.demo.activity

import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import io.coderf.arklab.demo.bean.UseCase
import io.coderf.arklab.demo.fragment.RoomSmartPagingFragment
import dagger.hilt.android.AndroidEntryPoint
import io.coderf.arklab.common.base.BaseSearchActivity
import io.coderf.arklab.common.utils.common.DensityUtil
import io.coderf.arklab.common.utils.common.DrawableUtil
import io.coderf.arklab.common.viewmodel.EmptyViewModel

@AndroidEntryPoint
class RoomPagingActivity : BaseSearchActivity<EmptyViewModel>() {

    private var useCase: UseCase? = null
    private val mCurrentFragment by lazy {
        RoomSmartPagingFragment()
    }
    override fun setTitleBar() = ""
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        supportFragmentManager.beginTransaction().add(io.coderf.arklab.common.R.id.search_view_container, mCurrentFragment).commit()
        binding.searchView.search.setBackgroundDrawable(DrawableUtil.createShapeDrawable(
            ContextCompat.getColor(this, io.coderf.arklab.common.R.color.themeColor), DensityUtil.dp2px(this,6f).toFloat()))
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