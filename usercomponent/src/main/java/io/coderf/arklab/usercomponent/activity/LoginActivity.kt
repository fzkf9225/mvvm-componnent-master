package io.coderf.arklab.usercomponent.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.view.KeyEvent
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import dagger.hilt.android.AndroidEntryPoint
import io.coderf.arklab.common.api.AppManager
import io.coderf.arklab.common.api.ConstantsHelper
import io.coderf.arklab.common.base.BaseActivity
import io.coderf.arklab.common.base.BaseResponse
import io.coderf.arklab.common.inter.ErrorService
import io.coderf.arklab.common.utils.common.KeyBoardUtil
import io.coderf.arklab.common.utils.common.RxView
import io.coderf.arklab.common.widget.dialog.ConfirmDialog
import io.coderf.arklab.common.widget.dialog.MessageDialog
import io.coderf.arklab.userapi.bean.UserInfo
import io.coderf.arklab.usercomponent.R
import io.coderf.arklab.usercomponent.api.UserAccountHelper
import io.coderf.arklab.usercomponent.databinding.ActivityLoginBinding
import io.coderf.arklab.usercomponent.domain.model.LoginSubmitResult
import io.coderf.arklab.usercomponent.ui.LoginAgreementMarkup
import io.coderf.arklab.usercomponent.view.UserView
import io.coderf.arklab.usercomponent.viewmodel.LoginViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import io.coderf.arklab.common.R as CommonR


/**
 * Created by fz on 2024/10/09 15:56.
 * describe：登录，登录方式：账号密码登录
 */
@AndroidEntryPoint
class LoginActivity : BaseActivity<LoginViewModel, ActivityLoginBinding>(), UserView {
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
            ContextCompat.getColor(this, CommonR.color.themeColor)
        )
        binding.cbAgreement.movementMethod = LinkMovementMethod.getInstance()
        RxView.setOnClickListener(binding.loginSubmit) {
            submitLogin(binding.editPassword.text?.toString(), binding.cbAgreement.isChecked)
        }
        binding.switchPasswordType.setOnClickListener {
            if (it.isSelected) {
                it.isSelected = false
                binding.editPassword.inputType =
                    InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT
            } else {
                it.isSelected = true
                binding.editPassword.inputType =
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mViewModel.loginState.collect { state ->
                    // 仅在外部状态与输入框不一致时写入，避免每次 StateFlow 回灌都 setText 把光标顶到开头
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

    /**
     * 与 [io.coderf.arklab.usercomponent.viewmodel.LoginViewModel.loginState] 同步展示；内容已一致则不再 setText。
     */
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
        val themeColor = ContextCompat.getColor(this, CommonR.color.themeColor)
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
            mViewModel.onLoginSuccess(
                userInfo,
                binding.editAccount.text.toString(),
                AppManager.getAppManager().activityStack.size,
                hasTarget()
            )
        }
        mViewModel.imageLiveData.observe(this) { data ->
            Glide.with(this).load(data.imageBase64).apply(
                RequestOptions().error(CommonR.mipmap.ic_default_image)
                    .placeholder(CommonR.mipmap.ic_default_image)
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

    override fun hideKeyboard() {
        try {
            KeyBoardUtil.closeKeyboard(binding.editAccount, this)
            KeyBoardUtil.closeKeyboard(binding.editPassword, this)
            KeyBoardUtil.closeKeyboard(binding.editVerificationCode, this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("UnsafeIntentLaunch")
    override fun toLast() {
        showToast("登录成功！")
        setResult(RESULT_OK, intent.putExtras(bundle!!))
        finish()
    }

    override fun hasTarget(): Boolean {
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

    override fun toTarget() {
        val targetActivity = bundle?.getString(ConstantsHelper.TARGET_ACTIVITY)
        if (TextUtils.isEmpty(targetActivity)) {
            toLast()
            return
        }
        try {
            val intent = Intent(this, Class.forName(targetActivity))
            intent.putExtras(bundle!!)
            startActivity(intent)
            finish()
        } catch (e: ClassNotFoundException) {
            toLast()
        }
    }

    override fun toMain() {
        showToast("登录成功！")
        AppManager.getAppManager().finishAllActivity()
        startActivity(errorService.mainActivity)
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
