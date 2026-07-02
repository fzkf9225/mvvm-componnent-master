package io.coderf.arklab.user.domain.usecase

import javax.inject.Inject

/**
 * 登录表单校验（纯规则，无 UI）。
 */
class ValidateLoginFormUseCase @Inject constructor() {

    sealed class Outcome {
        data class Invalid(val message: String) : Outcome()
        object AgreementRequired : Outcome()
        object Ok : Outcome()
    }

    operator fun invoke(
        userName: String?,
        password: String?,
        code: String?,
        agreementChecked: Boolean
    ): Outcome {
        if (userName.isNullOrEmpty()) {
            return Outcome.Invalid("请填写用户名")
        }
        if (password.isNullOrEmpty()) {
            return Outcome.Invalid("请填写密码")
        }
        if (code.isNullOrEmpty()) {
            return Outcome.Invalid("请填写验证码")
        }
        if (!agreementChecked) {
            return Outcome.AgreementRequired
        }
        return Outcome.Ok
    }
}
