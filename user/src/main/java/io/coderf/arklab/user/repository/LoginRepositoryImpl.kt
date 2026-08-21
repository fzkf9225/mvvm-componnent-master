package io.coderf.arklab.user.repository

import io.coderf.arklab.core.network.BaseNetworkRepository
import io.coderf.arklab.core.request.RequestOptions
import io.coderf.arklab.core.request.RequestResult
import io.coderf.arklab.core.request.TokenRefresher
import io.coderf.arklab.user.api.UserAccountHelper
import io.coderf.arklab.user.api.UserApiService
import io.coderf.arklab.user.bean.GraphicVerificationCodeBean
import io.coderf.arklab.user.bean.RequestLoginBean
import io.coderf.arklab.user.view.UserView
import io.coderf.arklab.userapi.bean.UserInfo
import kotlinx.coroutines.flow.Flow

/**
 * 登录仓库（新版 [BaseNetworkRepository]）。
 * 登录 / 验证码关闭鉴权刷新，避免与 refresh-token 递归。
 */
class LoginRepositoryImpl(
    private val api: UserApiService
) : BaseNetworkRepository<UserView>() {

    fun getImageCode(randomNumber: String): Flow<RequestResult<GraphicVerificationCodeBean>> {
        return request(
            RequestOptions.builder()
                .showLoading(false)
                .enableAuthRetry(false)
                .build()
        ) {
            api.getImageCodeSuspend(randomNumber)
        }
    }

    fun login(requestLoginBean: RequestLoginBean): Flow<RequestResult<UserInfo>> {
        return request(
            RequestOptions.builder()
                .loadingMessage("登录中，请稍后...")
                .enableAuthRetry(false)
                .build()
        ) {
            val tokenBean = api.getTokenSuspend(requestLoginBean)
            UserAccountHelper.setToken(tokenBean.tokenId ?: tokenBean.access_token)
            UserAccountHelper.setRefreshToken(tokenBean.refresh_token)
            api.getUserInfoSuspend()
        }
    }

    fun logout(
        options: RequestOptions = RequestOptions.defaults()
    ): Flow<RequestResult<Any?>> {
        return request(options) {
            api.logoutSuspend()
        }
    }
}
