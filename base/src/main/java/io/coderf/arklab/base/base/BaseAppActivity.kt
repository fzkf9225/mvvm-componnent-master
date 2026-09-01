package io.coderf.arklab.base.base

import androidx.databinding.ViewDataBinding
import io.coderf.arklab.base.R
import io.coderf.arklab.common.R as CommonR
import io.coderf.arklab.common.base.BaseActivity
import io.coderf.arklab.common.base.BaseViewModel
import io.coderf.arklab.common.bean.base.ToolbarConfig
import io.coderf.arklab.common.utils.common.DensityUtil

/**
 * 宿主/业务 Activity 通用基类：统一 MaterialToolbar / 状态栏样式。
 * 属于 case `:base`，不是框架封装。
 */
abstract class BaseAppActivity<VM : BaseViewModel<*, *>?, VDB : ViewDataBinding?> :
    BaseActivity<VM, VDB>(), SystemUiRestoreable {

    override fun restoreSystemUiAfterFullscreen() {
        createdToolbarConfig().applyStatusBar()
    }

    override fun createdToolbarConfig(): ToolbarConfig {
        return ToolbarConfig(this)
            .setTitle(setTitleBar())
            .setTextColor(CommonR.color.cardOnSurface)
            .setBgColor(CommonR.color.cardSurface)
            .setBackIconRes(R.drawable.base_icon_fh)
            .setHeight(DensityUtil.dp2px(this, 31f))
            .setStatusBarColor(CommonR.color.cardSurface)
            .setLightMode(false)
            .applyStatusBar()
    }
}

