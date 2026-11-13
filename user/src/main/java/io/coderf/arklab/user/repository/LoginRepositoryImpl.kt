package io.coderf.arklab.user.repository

import io.coderf.arklab.core.bean.RequestOptions
import io.coderf.arklab.core.bean.RequestResult
import io.coderf.arklab.core.network.BaseNetworkRepository
import io.coderf.arklab.user.api.UserAccountHelper
import io.coderf.arklab.user.api.UserApiService
import io.coderf.arklab.user.bean.GraphicVerificationCodeBean
import io.coderf.arklab.user.bean.RequestLoginBean
import io.coderf.arklab.userapi.bean.UserInfo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 登录仓库（[BaseNetworkRepository]）。
 * 登录 / 验证码关闭鉴权刷新，避免与 refresh-token 递归。
 * Loading / 错误 UI 由 ViewModel [io.coderf.arklab.common.base.BaseViewModel.launchRequest] 处理。
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @updated 2026/11/13
 */
class LoginRepositoryImpl @Inject constructor(
    private val api: UserApiService
) : BaseNetworkRepository() {

    fun getImageCode(randomNumber: String): Flow<RequestResult<GraphicVerificationCodeBean>> {
        return request(
            RequestOptions.builder()
                .showLoading(false)
                .deliverErrorToUi(false)
                .enableAuthRetry(false)
                .build()
        ) {
            api.getImageCodeSuspend(randomNumber)
        }
    }

    fun login(requestLoginBean: RequestLoginBean): Flow<RequestResult<UserInfo>> {
        return request(
            RequestOptions.builder()
                .showLoading(false)
                .deliverErrorToUi(false)
                .enableAuthRetry(false)
                .build()
        ) {
            try {
                val tokenBean = api.getTokenSuspend(requestLoginBean)
                UserAccountHelper.setToken(tokenBean.tokenId ?: tokenBean.access_token)
                UserAccountHelper.setRefreshToken(tokenBean.refresh_token)
                api.getUserInfoSuspend()
            } catch (e: CancellationException) {
                // 用户取消 Loading 时可能已写入 token，清掉避免「未跳转却已登录」
                UserAccountHelper.clearToken()
                throw e
            }
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
