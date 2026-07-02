package io.coderf.arklab.user.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import io.coderf.arklab.common.api.RepositoryFactory
import io.coderf.arklab.common.base.BaseViewModel
import io.coderf.arklab.userapi.bean.UserInfo
import io.coderf.arklab.user.api.UserAccountHelper
import io.coderf.arklab.user.api.UserApiService
import io.coderf.arklab.user.bean.GraphicVerificationCodeBean
import io.coderf.arklab.user.bean.RequestLoginBean
import io.coderf.arklab.user.domain.model.LoginSubmitResult
import io.coderf.arklab.user.domain.model.PostLoginRoute
import io.coderf.arklab.user.domain.usecase.HashLoginPasswordUseCase
import io.coderf.arklab.user.domain.usecase.PersistLoginAndDecideNavigationUseCase
import io.coderf.arklab.user.domain.usecase.ValidateLoginFormUseCase
import io.coderf.arklab.user.repository.LoginRepositoryImpl
import io.coderf.arklab.user.view.UserView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.random.Random

/**
 * 登录页 ViewModel：只做状态持有、调用 [LoginRepositoryImpl] 与领域用例；协议富文本与系统弹窗在 Activity 处理。
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    application: Application,
    private val validateLoginForm: ValidateLoginFormUseCase,
    private val hashLoginPassword: HashLoginPasswordUseCase,
    private val persistLoginAndDecideNavigation: PersistLoginAndDecideNavigationUseCase
) : BaseViewModel<LoginRepositoryImpl, UserView>(application) {

    @Inject
    lateinit var userApiService: UserApiService

    val liveData: MutableLiveData<UserInfo> by lazy { MutableLiveData() }

    private val _loginState = MutableStateFlow(RequestLoginBean(UserAccountHelper.getAccount()))
    val loginState: StateFlow<RequestLoginBean> = _loginState.asStateFlow()

    val imageLiveData: MutableLiveData<GraphicVerificationCodeBean> by lazy { MutableLiveData() }

    override fun createRepository(): LoginRepositoryImpl {
        return RepositoryFactory.create(LoginRepositoryImpl::class.java, userApiService)
    }

    fun updateUserName(name: String) {
        _loginState.update { it.copy(userName = name) }
    }

    fun updateCode(code: String) {
        _loginState.update { it.copy(code = code) }
    }

    /**
     * 校验并发起登录；由 Activity 根据 [LoginSubmitResult] 展示 Toast 或协议弹窗。
     */
    fun attemptLogin(rawPassword: String?, agreementChecked: Boolean): LoginSubmitResult {
        val state = _loginState.value
        when (
            val v = validateLoginForm(
                state.userName,
                rawPassword,
                state.code,
                agreementChecked
            )
        ) {
            is ValidateLoginFormUseCase.Outcome.Invalid ->
                return LoginSubmitResult.Toast(v.message)
            ValidateLoginFormUseCase.Outcome.AgreementRequired ->
                return LoginSubmitResult.NeedAgreementDialog
            ValidateLoginFormUseCase.Outcome.Ok -> { /* continue */ }
        }

        val plain = rawPassword ?: return LoginSubmitResult.Toast("请填写密码")
        val hashed = hashLoginPassword(plain)
        _loginState.update { it.copy(password = hashed) }
        iRepository.login(_loginState.value, liveData)
        return LoginSubmitResult.Submitted
    }

    /**
     * 登录接口成功后的持久化与路由（栈大小、是否含目标页由 Activity 传入，避免在领域层依赖 [AppManager]）。
     */
    fun onLoginSuccess(
        userInfo: UserInfo?,
        userName: String,
        activityStackSize: Int,
        hasTarget: Boolean
    ) {
        when (
            persistLoginAndDecideNavigation.execute(
                userInfo,
                userName,
                activityStackSize,
                hasTarget
            )
        ) {
            PostLoginRoute.OPEN_MAIN -> baseView?.toMain()
            PostLoginRoute.OPEN_TARGET -> baseView?.toTarget()
            PostLoginRoute.FINISH_TO_LAST -> baseView?.toLast()
        }
    }

    fun refreshCaptchaAndLoadImage() {
        val num = Random.nextInt(10000000, 100000000).toString()
        _loginState.update { it.copy(num = num) }
        iRepository.getImageCode(num, imageLiveData)
    }
}
