package com.casic.titan.usercomponent.viewmodel

import android.app.Application
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.casic.titan.userapi.bean.UserInfo
import com.casic.titan.usercomponent.api.UserAccountHelper
import com.casic.titan.usercomponent.api.UserApiService
import com.casic.titan.usercomponent.bean.GraphicVerificationCodeBean
import com.casic.titan.usercomponent.bean.RequestLoginBean
import com.casic.titan.usercomponent.databinding.ActivityLoginBinding
import com.casic.titan.usercomponent.repository.LoginRepositoryImpl
import com.casic.titan.usercomponent.view.UserView
import dagger.hilt.android.lifecycle.HiltViewModel
import pers.fz.mvvm.R
import pers.fz.mvvm.activity.WebViewActivity
import pers.fz.mvvm.api.AppManager
import pers.fz.mvvm.api.RepositoryFactory
import pers.fz.mvvm.base.BaseViewModel
import pers.fz.mvvm.util.encode.SM3Utils
import pers.fz.mvvm.wight.dialog.ConfirmDialog
import javax.inject.Inject
import kotlin.getValue
import kotlin.random.Random

/**
 * Create by CherishTang on 2020/3/19 0019
 * describe:loginViewModel
 */
@HiltViewModel
class LoginViewModel @Inject constructor(application: Application) : BaseViewModel<LoginRepositoryImpl, UserView>(application) {

    val liveData: MutableLiveData<UserInfo> by lazy {
        MutableLiveData()
    }
    val imageLiveData: MutableLiveData<GraphicVerificationCodeBean> by lazy {
        MutableLiveData()
    }

    @Inject
    lateinit var userApiService: UserApiService

    private val agreement = "登录/注册表示您同意 《用户协议》 和 《隐私政策》"

    override fun createRepository(): LoginRepositoryImpl? {
        return RepositoryFactory.create(LoginRepositoryImpl::class.java,userApiService)
    }

    fun imageCodeClick(v: View, loginBean: RequestLoginBean) {
        getImageCode(loginBean)
    }

    public
    fun loginClick(binding: ActivityLoginBinding) {
        if (binding.loginBean?.userName.isNullOrEmpty()) {
            baseView?.showToast("请填写用户名")
            return
        }
        if (binding.editPassword.text.isNullOrEmpty()) {
            baseView?.showToast("请填写密码")
            return
        }
        if (binding.loginBean?.code.isNullOrEmpty()) {
            baseView?.showToast("请填写验证码")
            return
        }
        if (!binding.cbAgreement.isChecked) {
            ConfirmDialog(binding.root.context)
                .setSpannableContent(
                    agreementSpannableString(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.themeColor
                        )
                    )
                )
                .setNegativeText("拒绝")
                .setPositiveText("同意")
                .setCanOutSide(false)
                .setPositiveTextColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.themeColor
                    )
                )
                .setOnPositiveClickListener { _ ->
                    binding.cbAgreement.isChecked = true
                }
                .builder()
                .show()
            return
        }
        binding.loginBean?.password =
            SM3Utils.hashToHex(
                SM3Utils.hash(
                    binding.editPassword.text.toString().toByteArray()
                )
            )
        iRepository.login(binding.loginBean!!, liveData)
    }

    fun loginCallback(userInfo: UserInfo?, userName: String) {
//        viewModelScope.launch {
//            with(Dispatchers.IO) {
//                JPUS
//            }
//        }
        //存储登录信息和登录状态
        UserAccountHelper.saveLoginState(userInfo, true)
        UserAccountHelper.setAgreement(true)
        //这里只是判断本地账号和上次账号是否为同一个，如果不是同一个则不能继续之前操作，则需要返回App首页刷新，并且同事判断下当前app是不是只有当前登录页一个页面
        if (TextUtils.isEmpty(userName) || userName != UserAccountHelper.getAccount() || AppManager.getAppManager().activityStack.size == 1) {
            UserAccountHelper.saveAccount(userName)
            UserAccountHelper.savePassword(userInfo?.password)
            //打开MainActivity
            baseView?.toMain()
            return
        }
        //存储本地登录的账号
        UserAccountHelper.saveAccount(userName)
        UserAccountHelper.savePassword(userInfo?.password)
        if (baseView?.hasTarget() == true) {
            baseView?.toTarget()
            return
        }
        baseView?.toLast()
    }

    public fun getImageCode(loginBean: RequestLoginBean) {
        loginBean.num = Random.nextInt(10000000, 100000000).toString()
        iRepository.getImageCode(loginBean.num!!, imageLiveData)
    }

    public fun agreementSpannableString(color: Int = Color.BLACK): SpannableString {
        val spannableString = SpannableString(agreement)

// 设置《用户协议》的颜色和点击事件
        val userAgreementClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // 处理点击事件
                WebViewActivity.show(widget.context, "file:///android_asset/用户协议.html","用户协议")
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = color // 设置颜色
                ds.isUnderlineText = true
            }
        }
        val userAgreementStart = agreement.indexOf("用户协议")
        val userAgreementEnd = userAgreementStart + "用户协议".length
        spannableString.setSpan(
            userAgreementClickableSpan,
            userAgreementStart,
            userAgreementEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // 设置《隐私政策》的颜色和点击事件
        val privacyPolicyClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // 处理点击事件
                WebViewActivity.show(widget.context, "file:///android_asset/隐私政策.html","隐私政策")
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = color // 设置颜色
                ds.isUnderlineText = true
            }
        }
        val privacyPolicyStart = agreement.indexOf("隐私政策")
        val privacyPolicyEnd = privacyPolicyStart + "隐私政策".length
        spannableString.setSpan(
            privacyPolicyClickableSpan,
            privacyPolicyStart,
            privacyPolicyEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannableString
    }
}
