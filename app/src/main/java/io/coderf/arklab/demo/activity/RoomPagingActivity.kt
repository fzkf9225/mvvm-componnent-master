package io.coderf.arklab.demo.activity

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import dagger.hilt.android.AndroidEntryPoint
import io.coderf.arklab.common.base.BaseSearchActivity
import io.coderf.arklab.common.utils.common.DrawableUtil
import io.coderf.arklab.common.utils.theme.ThemeAttrs
import io.coderf.arklab.common.viewmodel.EmptyViewModel
import io.coderf.arklab.core.utils.DensityUtil
import io.coderf.arklab.demo.bean.UseCase
import io.coderf.arklab.demo.fragment.RoomSmartPagingFragment
import io.coderf.arklab.common.R as CommonR

/**
 * RoomPagingActivity：列表新增 / 编辑后刷新。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/11/9
 */
@AndroidEntryPoint
class RoomPagingActivity : BaseSearchActivity<EmptyViewModel>() {

    private var useCase: UseCase? = null
    private val mCurrentFragment by lazy {
        RoomSmartPagingFragment()
    }

    private val formLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            mCurrentFragment.refreshList()
        }
    }

    override fun setTitleBar() = ""

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        supportFragmentManager.beginTransaction()
            .add(CommonR.id.search_view_container, mCurrentFragment)
            .commit()
        binding.searchView.search.setBackgroundDrawable(
            DrawableUtil.createShapeDrawable(
                ThemeAttrs.primary(this),
                DensityUtil.dp2px(this, 6f).toFloat()
            )
        )
        inflateToolbarMenu(CommonR.menu.menu_create) { item ->
            if (item.itemId == CommonR.id.toolbar_create) {
                openCreateForm()
                true
            } else {
                false
            }
        }
    }

    override fun initData(bundle: Bundle?) {
        useCase = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle?.getParcelable("args", UseCase::class.java)
        } else {
            @Suppress("DEPRECATION")
            bundle?.getParcelable("args")
        }
        toolbarBind?.toolbarConfig?.setTitle(useCase?.name)

        keywordsLiveData.observe(this) {
            mCurrentFragment.searcher(it)
        }
    }

    private fun openCreateForm() {
        val intent = Intent(this, VerifyActivity::class.java)
        useCase?.let { intent.putExtra("args", it) }
        formLauncher.launch(intent)
    }
}
