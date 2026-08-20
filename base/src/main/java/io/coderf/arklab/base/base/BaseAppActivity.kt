package io.coderf.arklab.base.base

import androidx.databinding.ViewDataBinding
import io.coderf.arklab.base.R
import io.coderf.arklab.common.base.BaseActivity
import io.coderf.arklab.common.base.BaseViewModel
import io.coderf.arklab.common.bean.base.ToolbarConfig
import io.coderf.arklab.common.utils.common.DensityUtil

/**
 * 宿主/业务 Activity 通用基类：统一 Toolbar / 状态栏样式。
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
            .setTextColor(android.R.color.black)
            .setBgColor(R.color.base_default_background)
            .setBackIconRes(R.drawable.base_icon_fh)
            .setHeight(DensityUtil.dp2px(this, 31f))
            .setStatusBarColor(R.color.base_default_background)
            .setLightMode(false)
            .applyStatusBar()
    }
}
