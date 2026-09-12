package io.coderf.arklab.user.activity

import io.coderf.arklab.common.utils.theme.ThemeAttrs
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.view.KeyEvent
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import dagger.hilt.android.AndroidEntryPoint
import io.coderf.arklab.common.api.AppManager
import io.coderf.arklab.common.api.Config
import io.coderf.arklab.common.api.ConstantsHelper
import io.coderf.arklab.common.base.BaseActivity
import io.coderf.arklab.common.base.BaseResponse
import io.coderf.arklab.common.inter.ErrorService
import io.coderf.arklab.common.utils.common.KeyBoardUtil
import io.coderf.arklab.common.utils.common.RxView
import io.coderf.arklab.common.widget.dialog.ConfirmDialog
import io.coderf.arklab.common.widget.dialog.MessageDialog
import io.coderf.arklab.userapi.bean.UserInfo
import io.coderf.arklab.user.R
import io.coderf.arklab.user.api.UserAccountHelper
import io.coderf.arklab.user.databinding.ActivityLoginBinding
import io.coderf.arklab.user.domain.model.LoginSubmitResult
import io.coderf.arklab.user.domain.model.PostLoginRoute
import io.coderf.arklab.user.ui.LoginAgreementMarkup
import io.coderf.arklab.user.viewmodel.LoginViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 登录，登录方式：账号密码登录
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2024/10/9 15:56
 * @updated 2026/9/12
 */
@AndroidEntryPoint
class LoginActivity : BaseActivity<LoginViewModel, ActivityLoginBinding>() {
    private var bundle: Bundle? = null

    companion object {
        const val NOT_SUPPORT_LOGIN = "notSupportLogin"
    }

    @Inject
    lateinit var errorService: ErrorService

    override fun setTitleBar(): String {
        return "登录"
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_login
    }

    override fun hasToolBar(): Boolean {
        return false
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.cbAgreement.isChecked = UserAccountHelper.isAgree()
        binding.cbAgreement.text = LoginAgreementMarkup.build(
            ThemeAttrs.primary(this)
        )
        binding.cbAgreement.movementMethod = LinkMovementMethod.getInstance()
        RxView.setOnClickListener(binding.loginSubmit) {
            submitLogin(binding.editPassword.text?.toString(), binding.cbAgreement.isChecked)
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mViewModel.loginState.collect { state ->
                    syncEditTextIfChanged(binding.editAccount, state.userName.orEmpty())
                    syncEditTextIfChanged(binding.editVerificationCode, state.code.orEmpty())
                }
            }
        }

        binding.editAccount.doAfterTextChanged { text ->
            mViewModel.updateUserName(text.toString())
        }
        binding.editVerificationCode.doAfterTextChanged { text ->
            mViewModel.updateCode(text.toString())
        }
        binding.imageVerificationCode.setOnClickListener {
            mViewModel.refreshCaptchaAndLoadImage()
        }
    }

    private fun syncEditTextIfChanged(edit: EditText, newText: String) {
        if (edit.text?.toString() == newText) return
        edit.setText(newText)
        edit.setSelection(newText.length)
    }

    private fun submitLogin(rawPassword: String?, agreementChecked: Boolean) {
        when (val result = mViewModel.attemptLogin(rawPassword, agreementChecked)) {
            is LoginSubmitResult.Toast -> showToast(result.message)
            LoginSubmitResult.NeedAgreementDialog -> showAgreementConsentDialog(rawPassword)
            LoginSubmitResult.Submitted -> { /* 网络结果走 liveData */ }
        }
    }

    private fun showAgreementConsentDialog(rawPassword: String?) {
        val themeColor = ThemeAttrs.primary(this)
        ConfirmDialog(this)
            .setSpannableContent(LoginAgreementMarkup.build(themeColor))
            .setNegativeText("拒绝")
            .setPositiveText("同意")
            .setCanOutSide(false)
            .setPositiveTextColor(themeColor)
            .setOnPositiveClickListener {
                binding.cbAgreement.isChecked = true
                submitLogin(rawPassword, agreementChecked = true)
            }
            .builder()
            .show()
    }

    @SuppressLint("SetTextI18n")
    override fun initData(bundle: Bundle?) {
        this.bundle = bundle
        binding.tvAppVersion.text =
            "版本 ${AppManager.getAppManager().getVersion(this@LoginActivity)}"
        mViewModel.liveData.observe(this) { userInfo: UserInfo? ->
            hideKeyboard()
            mViewModel.onLoginSuccess(
                userInfo,
                binding.editAccount.text.toString(),
                AppManager.getAppManager().activityStack.size,
                hasTarget()
            )
        }
        mViewModel.postLoginRoute.observe(this) { route ->
            when (route) {
                PostLoginRoute.OPEN_MAIN -> navigateToMain()
                PostLoginRoute.OPEN_TARGET -> navigateToTarget()
                PostLoginRoute.FINISH_TO_LAST -> navigateToLast()
                null -> Unit
            }
        }
        mViewModel.imageLiveData.observe(this) { data ->
            Glide.with(this).load(data.imageBase64).apply(
                RequestOptions()
                    .error(Config.getInstance().defaultErrorImageRes)
                    .placeholder(Config.getInstance().defaultPlaceholderRes)
            ).into(binding.imageVerificationCode)
        }
        mViewModel.refreshCaptchaAndLoadImage()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val mIntent = Intent(Intent.ACTION_MAIN)
            mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            mIntent.addCategory(Intent.CATEGORY_HOME)
            startActivity(mIntent)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun hideKeyboard() {
        try {
            KeyBoardUtil.closeKeyboard(binding.editAccount, this)
            KeyBoardUtil.closeKeyboard(binding.editPassword, this)
            KeyBoardUtil.closeKeyboard(binding.editVerificationCode, this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hasTarget(): Boolean {
        val targetActivity = bundle?.getString(ConstantsHelper.TARGET_ACTIVITY)
        if (TextUtils.isEmpty(targetActivity)) {
            return false
        }
        return try {
            Class.forName(targetActivity)
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

    @SuppressLint("UnsafeIntentLaunch")
    private fun navigateToLast() {
        showToast("登录成功！")
        setResult(RESULT_OK, intent.putExtras(bundle!!))
        finish()
    }

    private fun navigateToTarget() {
        val targetActivity = bundle?.getString(ConstantsHelper.TARGET_ACTIVITY)
        if (TextUtils.isEmpty(targetActivity)) {
            navigateToLast()
            return
        }
        try {
            val intent = Intent(this, Class.forName(targetActivity))
            intent.putExtras(bundle!!)
            startActivity(intent)
            finish()
        } catch (e: ClassNotFoundException) {
            navigateToLast()
        }
    }

    private fun navigateToMain() {
        showToast("登录成功！")
        errorService.toMain(this, null)
    }

    override fun onErrorCode(model: BaseResponse<*>?) {
        super.onErrorCode(model)
        if (NOT_SUPPORT_LOGIN == model?.code) {
            MessageDialog(this)
                .setMessage(model.message)
                .setOnPositiveClickListener {
                    it.dismiss()
                }
                .builder()
                .show()
        }
    }
}
