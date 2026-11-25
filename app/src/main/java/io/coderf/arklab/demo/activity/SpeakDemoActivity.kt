package io.coderf.arklab.demo.activity

import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import io.coderf.arklab.common.base.BaseActivity
import io.coderf.arklab.common.viewmodel.EmptyViewModel
import io.coderf.arklab.demo.R
import io.coderf.arklab.demo.databinding.ActivitySpeakDemoBinding
import io.coderf.arklab.demo.fragment.SpeakSmartPagingFragment
import io.coderf.arklab.speak.model.SpeakFinishAction
import io.coderf.arklab.speak.permission.SpeakPermissionConfig
import io.coderf.arklab.speak.style.SpeakUiStyle

/**
 * core-speak Demo：NavHost 嵌入 [SpeakSmartPagingFragment] + 底部按住说话。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/25 14:05
 * @updated 2026/11/25 对齐 room / live Demo 的 navigation 写法
 */
@AndroidEntryPoint
class SpeakDemoActivity : BaseActivity<EmptyViewModel, ActivitySpeakDemoBinding>() {

    override fun getLayoutId(): Int = R.layout.activity_speak_demo

    override fun setTitleBar(): String = "语音 Speak"

    override fun initView(savedInstanceState: Bundle?) {
        binding.btnSpeak.setUserId("demo")
        binding.btnSpeak.setPermissionConfig(
            SpeakPermissionConfig.defaults(this) {
                showRationaleDialog = true
            }
        )
        binding.btnSpeak.setSpeakUiStyle(
            SpeakUiStyle.defaults(this) {
                // 可按需：editEnabled / editArcHeightPx / cancelTextColor / sendTextSizePx 等
            }
        )
        binding.btnSpeak.setOnSpeakListener { action, result ->
            when (action) {
                SpeakFinishAction.Send -> {
                    if (result != null) {
                        findListFragment()?.saveSend(result) {
                            showToast("已保存 ${result.durationMs / 1000}″")
                        }
                    }
                }
                SpeakFinishAction.Edit -> {
                    showToast("编辑回调：${result?.filePath ?: "null"}（本期无 ASR）")
                }
                SpeakFinishAction.Cancel -> Unit
            }
        }
    }

    override fun initData(bundle: Bundle?) = Unit

    private fun findListFragment(): SpeakSmartPagingFragment? {
        val navHost = supportFragmentManager
            .findFragmentById(R.id.speak_smart_paging_fragment_view)
        if (navHost !is NavHostFragment) return null
        val primary = navHost.childFragmentManager.primaryNavigationFragment
        if (primary is SpeakSmartPagingFragment) return primary
        return navHost.childFragmentManager.fragments
            .firstOrNull { it is SpeakSmartPagingFragment } as? SpeakSmartPagingFragment
    }
}
