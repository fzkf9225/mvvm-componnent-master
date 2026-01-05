package com.casic.otitan.usercomponent.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.style.ClickableSpan
import android.view.View
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.casic.otitan.userapi.UserService
import com.casic.otitan.userapi.bean.UserInfo
import com.casic.otitan.usercomponent.api.UserAccountHelper
import com.casic.otitan.usercomponent.api.UserApiService
import com.casic.otitan.usercomponent.bean.GraphicVerificationCodeBean
import com.casic.otitan.usercomponent.bean.RequestLoginBean
import com.casic.otitan.usercomponent.repository.LoginRepositoryImpl
import com.casic.otitan.usercomponent.view.UserView
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.casic.otitan.common.R
import com.casic.otitan.common.activity.WebViewActivity
import com.casic.otitan.common.api.AppManager
import com.casic.otitan.common.api.RepositoryFactory
import com.casic.otitan.common.base.BaseViewModel
import com.casic.otitan.common.utils.encode.SM3Utils
import com.casic.otitan.common.widget.dialog.ConfirmDialog
import com.casic.otitan.usercomponent.repository.LoginFlowRepositoryImpl
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

/**
 * Create by CherishTang on 2020/3/19 0019
 * describe:loginViewModel
 */
@HiltViewModel
class LoginViewModel @Inject constructor(application: Application) :
    BaseViewModel<LoginRepositoryImpl, UserView>(application) {

    @Inject
    lateinit var userService: UserService

    val liveData: MutableLiveData<UserInfo> by lazy {
        MutableLiveData()
    }

    // 使用 StateFlow 替代 LiveData
    private val _loginState by lazy {
        MutableStateFlow(RequestLoginBean(UserAccountHelper.getAccount()))
    }

    val loginState: StateFlow<RequestLoginBean> = _loginState.asStateFlow()

    val imageLiveData: MutableLiveData<GraphicVerificationCodeBean> by lazy {
        MutableLiveData()
    }

    @Inject
    lateinit var userApiService: UserApiService

    private val agreement = "登录/注册表示您同意 《用户协议》 和 《隐私政策》"

    override fun createRepository(): LoginRepositoryImpl? {
        return RepositoryFactory.create(LoginRepositoryImpl::class.java, userApiService)
    }

    // 更新状态的方法
    fun updateUserName(name: String) {
        _loginState.value.userName = name
    }

    fun updateCode(code: String) {
        _loginState.value.code = code
    }

    public
    fun loginClick(content: Context, password: String?, checkBox: AppCompatCheckBox) {
        if (loginState.value.userName.isNullOrEmpty()) {
            baseView?.showToast("请填写用户名")
            return
        }
        if (password.isNullOrEmpty()) {
            baseView?.showToast("请填写密码")
            return
        }
        if (loginState.value.code.isNullOrEmpty()) {
            baseView?.showToast("请填写验证码")
            return
        }
        if (!checkBox.isChecked) {
            ConfirmDialog(content)
                .setSpannableContent(
                    agreementSpannableString(
                        ContextCompat.getColor(
                            content,
                            R.color.themeColor
                        )
                    )
                )
                .setNegativeText("拒绝")
                .setPositiveText("同意")
                .setCanOutSide(false)
                .setPositiveTextColor(
                    ContextCompat.getColor(
                        content,
                        R.color.themeColor
                    )
                )
                .setOnPositiveClickListener { _ ->
                    checkBox.isChecked = true
                }
                .builder()
                .show()
            return
        }
        loginState.value.password =
            SM3Utils.hashToHex(
                SM3Utils.hash(
                    password.toByteArray()
                )
            )
        iRepository.login(loginState.value, liveData)
        // 使用 Flow，不用liveData去接收，这里只是懒得改
//        viewModelScope.launch {
//            iRepository?.login(loginState.value)?.collect {
//                liveData.value =  it
//            }
//        }
    }

    fun loginCallback(userInfo: UserInfo?, userName: String) {
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

    public fun getImageCode() {
        _loginState.value.num =  Random.nextInt(10000000, 100000000).toString()
        iRepository.getImageCode(loginState.value.num!!, imageLiveData)
        // 使用 Flow，不用liveData去接收，这里只是懒得改
//        viewModelScope.launch {
//            iRepository.getImageCode(loginState.value.num!!).collect {
//                imageLiveData.value = it
//            }
//        }
    }

    public fun agreementSpannableString(color: Int = Color.BLACK): SpannableString {
        val spannableString = SpannableString(agreement)

// 设置《用户协议》的颜色和点击事件
        val userAgreementClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // 处理点击事件
                WebViewActivity.show(
                    widget.context,
                    "file:///android_asset/用户协议.html",
                    "用户协议"
                )
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
                WebViewActivity.show(
                    widget.context,
                    "file:///android_asset/隐私政策.html",
                    "隐私政策"
                )
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
