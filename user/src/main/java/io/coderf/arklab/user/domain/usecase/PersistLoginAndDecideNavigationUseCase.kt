package io.coderf.arklab.user.domain.usecase

import android.text.TextUtils
import io.coderf.arklab.userapi.bean.UserInfo
import io.coderf.arklab.user.api.UserAccountHelper
import io.coderf.arklab.user.domain.model.PostLoginRoute
import javax.inject.Inject

/**
 * 登录成功后的本地持久化与导航决策（原 [LoginViewModel.loginCallback] 中的业务分支）。
 */
class PersistLoginAndDecideNavigationUseCase @Inject constructor() {

    fun execute(
        userInfo: UserInfo?,
        userName: String,
        activityStackSize: Int,
        hasTarget: Boolean
    ): PostLoginRoute {
        UserAccountHelper.saveLoginState(userInfo, true)
        UserAccountHelper.setAgreement(true)

        if (TextUtils.isEmpty(userName) ||
            userName != UserAccountHelper.getAccount() ||
            activityStackSize == 1
        ) {
            UserAccountHelper.saveAccount(userName)
            UserAccountHelper.savePassword(userInfo?.password)
            return PostLoginRoute.OPEN_MAIN
        }

        UserAccountHelper.saveAccount(userName)
        UserAccountHelper.savePassword(userInfo?.password)

        return if (hasTarget) {
            PostLoginRoute.OPEN_TARGET
        } else {
            PostLoginRoute.FINISH_TO_LAST
        }
    }
}
