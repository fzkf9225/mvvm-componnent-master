package io.coderf.arklab.user.domain.model

/**
 * [io.coderf.arklab.user.viewmodel.LoginViewModel.attemptLogin] 的同步结果，
 * 由 Activity 决定 Toast / 弹窗；成功发起网络请求时返回 [Submitted]。
 */
sealed class LoginSubmitResult {
    data class Toast(val message: String) : LoginSubmitResult()
    object NeedAgreementDialog : LoginSubmitResult()
    object Submitted : LoginSubmitResult()
}
