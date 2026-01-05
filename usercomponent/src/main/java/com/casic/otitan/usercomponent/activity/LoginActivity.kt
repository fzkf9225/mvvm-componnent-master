package com.casic.otitan.usercomponent.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.view.KeyEvent
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.casic.otitan.userapi.bean.UserInfo
import com.casic.otitan.usercomponent.R
import com.casic.otitan.usercomponent.api.UserAccountHelper
import com.casic.otitan.usercomponent.databinding.ActivityLoginBinding
import com.casic.otitan.usercomponent.view.UserView
import com.casic.otitan.usercomponent.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.casic.otitan.common.api.AppManager
import com.casic.otitan.common.api.ConstantsHelper
import com.casic.otitan.common.base.BaseActivity
import com.casic.otitan.common.base.BaseResponse
import com.casic.otitan.common.inter.ErrorService
import com.casic.otitan.common.utils.common.KeyBoardUtil
import com.casic.otitan.common.utils.common.RxView
import com.casic.otitan.common.widget.dialog.MessageDialog
import javax.inject.Inject


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
        // 设置CheckBox的文本和点击事件
        binding.cbAgreement.text = mViewModel.agreementSpannableString()
        binding.cbAgreement.movementMethod = LinkMovementMethod.getInstance()
        //防止快速点击
        RxView.setOnClickListener(binding.loginSubmit) {
            mViewModel.loginClick(this, binding.editPassword.text?.toString(), binding.cbAgreement)
        }
        binding.switchPasswordType.setOnClickListener {
            if (it.isSelected) {
                it.isSelected = false;
                binding.editPassword.inputType =
                    InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT;//设置密码不可见
            } else {
                it.isSelected = true;
                binding.editPassword.inputType =
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;//设置密码可见
            }
        }
        // 1. ViewModel → UI：监听状态变化并更新UI
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mViewModel.loginState.collect { state ->
                    binding.editAccount.setText(state.userName)
                    binding.editVerificationCode.setText(state.code)
                }
            }
        }

        // 2. UI → ViewModel：监听用户输入
        binding.editAccount.doAfterTextChanged  { text ->
            mViewModel.updateUserName(text.toString())
        }
        binding.editVerificationCode.doAfterTextChanged  { text->
            mViewModel.updateCode(text.toString())
        }
        binding.imageVerificationCode.setOnClickListener {
            mViewModel.getImageCode()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun initData(bundle: Bundle?) {
        this.bundle = bundle
        binding.tvAppVersion.text =
            "版本 ${AppManager.getAppManager().getVersion(this@LoginActivity)}"
        mViewModel.liveData.observe(this) { userInfo: UserInfo? ->
            mViewModel.loginCallback(
                userInfo,
                binding.editAccount.text.toString()
            )
        }
        mViewModel.imageLiveData.observe(this) { data ->
            Glide.with(this).load(data.imageBase64).apply(
                RequestOptions().error(com.casic.otitan.common.R.mipmap.ic_default_image)
                    .placeholder(com.casic.otitan.common.R.mipmap.ic_default_image)
            ).into(binding.imageVerificationCode)
        }
        mViewModel.getImageCode()
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
        try {
            //是否报错，不报错说明目标页面存在
            Class.forName(targetActivity)
            return true
        } catch (e: ClassNotFoundException) {
            return false
        }
    }

    override fun toTarget() {
        val targetActivity = bundle?.getString(ConstantsHelper.TARGET_ACTIVITY)
        if (TextUtils.isEmpty(targetActivity)) {
            toLast()
            return
        }
        try {
            //是否报错，不报错说明目标页面存在
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