package com.casic.otitan.usercomponent.impl

import com.casic.otitan.common.api.ApiRetrofit
import com.casic.otitan.common.api.ConstantsHelper
import com.casic.otitan.common.base.BaseException
import com.casic.otitan.common.inter.FlowRetryService
import com.casic.otitan.common.utils.log.LogUtil
import com.casic.otitan.usercomponent.api.UserAccountHelper
import com.casic.otitan.usercomponent.api.UserApiService
import retrofit2.HttpException
import javax.inject.Inject

/**
 * Kotlin协程版本的重试机制实现
 */
class FlowRetryServiceImpl @Inject constructor(
    private val userApiService: UserApiService
) : FlowRetryService {
    /**
     * 最大出错重试次数
     */
    private var maxRetries = ConstantsHelper.RETRY_WHEN_MAX_COUNT

    /**
     * 当前出错重试次数
     */
    private var retryCount = 0

    override suspend fun shouldRetry(throwable: Throwable): Boolean {
        return when (throwable) {
            is BaseException -> {
                LogUtil.show(ApiRetrofit.TAG, "第 $retryCount 次重试，BaseException：$throwable")
                val isLoginPastOrNoPermission = true
                if (isLoginPastOrNoPermission && ++retryCount <= maxRetries) {
                    return true
                } else {
                    retryCount = 0
                    return false
                }
            }

            is HttpException -> {
                LogUtil.show(ApiRetrofit.TAG, "第 $retryCount 次重试，HttpException：$throwable")
                if (401 == throwable.code() && ++retryCount <= maxRetries) {
                    return true
                } else {
                    retryCount = 0
                    return false
                }
            }

            else -> {
                LogUtil.show(ApiRetrofit.TAG, "不满足重试条件！")
                retryCount = 0
                false
            }
        }
    }

    override suspend fun refreshToken() {
        try {
            LogUtil.show(ApiRetrofit.TAG, "Refreshing token... Attempt $retryCount/$maxRetries")
            UserAccountHelper.saveLoginPast(false)

            // 使用Kotlin协程版本的API
            val tokenBean = userApiService.refreshTokenSuspend(UserAccountHelper.getRefreshToken())
            UserAccountHelper.setToken(tokenBean.access_token)
            UserAccountHelper.setRefreshToken(tokenBean.refresh_token)

            // 获取用户信息
            val userInfo = userApiService.getUserInfoSuspend()
            UserAccountHelper.saveLoginState(userInfo, true)
            retryCount = 0
            LogUtil.show(ApiRetrofit.TAG, "Token refreshed successfully")
        } catch (e: Exception) {
            LogUtil.show(ApiRetrofit.TAG, "Failed to refresh token: $e")
            throw e
        }
    }

    override fun setMaxRetryCount(maxRetryCount: Int) {
        this.maxRetries = maxRetryCount
    }

    override fun getMaxRetryCount(): Int = maxRetries
}