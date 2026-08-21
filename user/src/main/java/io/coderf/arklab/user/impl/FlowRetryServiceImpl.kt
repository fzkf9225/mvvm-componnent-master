package io.coderf.arklab.user.impl

import io.coderf.arklab.common.api.ApiRetrofit
import io.coderf.arklab.common.api.ConstantsHelper
import io.coderf.arklab.common.base.BaseException
import io.coderf.arklab.common.inter.FlowRetryService
import io.coderf.arklab.common.utils.log.LogUtil
import io.coderf.arklab.core.request.TokenRefresher
import io.coderf.arklab.user.api.UserAccountHelper
import io.coderf.arklab.user.api.UserApiService
import kotlinx.coroutines.CompletableDeferred
import retrofit2.HttpException
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 协程 Token 刷新：同时实现旧 [FlowRetryService] 与新 [TokenRefresher]。
 *
 * Single-Flight：[CompletableDeferred] 多路 await，等价 RxJava `share()`，
 * 避免并发 401 时重复消费 refresh_token。
 */
@Singleton
class FlowRetryServiceImpl @Inject constructor(
    private val userApiService: UserApiService
) : FlowRetryService, TokenRefresher {

    private var maxRetries = ConstantsHelper.RETRY_WHEN_MAX_COUNT
    private val retryCount = AtomicInteger(0)
    private val refreshFlightLock = Any()

    @Volatile
    private var refreshFlight: CompletableDeferred<Unit>? = null

    override suspend fun shouldRetry(throwable: Throwable): Boolean = shouldRefresh(throwable)

    override suspend fun shouldRefresh(throwable: Throwable): Boolean {
        return when {
            isAuthFailure(throwable) -> {
                val count = retryCount.incrementAndGet()
                LogUtil.logger(ApiRetrofit.TAG, "第 $count 次鉴权重试：$throwable")
                if (count <= maxRetries) {
                    true
                } else {
                    resetRetryState()
                    false
                }
            }
            else -> {
                LogUtil.logger(ApiRetrofit.TAG, "不满足鉴权重试条件！")
                false
            }
        }
    }

    override fun isAuthFailure(throwable: Throwable): Boolean {
        return when (throwable) {
            is BaseException -> UserAccountHelper.isLoginPast(throwable.errorCode)
            is HttpException -> throwable.code() == 401
            else -> {
                val cause = throwable.cause
                cause != null && cause !== throwable && isAuthFailure(cause)
            }
        }
    }

    override suspend fun refreshToken() = refresh()

    override suspend fun refresh() {
        val existing: CompletableDeferred<Unit>?
        val created: CompletableDeferred<Unit>?
        synchronized(refreshFlightLock) {
            val inFlight = refreshFlight
            if (inFlight != null) {
                existing = inFlight
                created = null
            } else {
                val flight = CompletableDeferred<Unit>()
                refreshFlight = flight
                existing = null
                created = flight
            }
        }

        if (existing != null) {
            LogUtil.logger(ApiRetrofit.TAG, "refresh single-flight: reuse in-flight refresh")
            existing.await()
            return
        }

        val flight = created!!
        try {
            LogUtil.logger(ApiRetrofit.TAG, "refresh single-flight: start new refresh")
            doRefreshToken()
            flight.complete(Unit)
        } catch (e: Exception) {
            flight.completeExceptionally(e)
            throw e
        } finally {
            synchronized(refreshFlightLock) {
                if (refreshFlight === flight) {
                    refreshFlight = null
                }
            }
        }
    }

    private suspend fun doRefreshToken() {
        LogUtil.logger(
            ApiRetrofit.TAG,
            "Refreshing token... Attempt ${retryCount.get()}/$maxRetries"
        )
        UserAccountHelper.saveLoginPast(false)

        val tokenBean = userApiService.refreshTokenSuspend(UserAccountHelper.getRefreshToken())
        UserAccountHelper.setToken(tokenBean.access_token)
        UserAccountHelper.setRefreshToken(tokenBean.refresh_token)

        val userInfo = userApiService.getUserInfoSuspend()
        UserAccountHelper.saveLoginState(userInfo, true)
        resetRetryState()
        LogUtil.logger(ApiRetrofit.TAG, "Token refreshed successfully")
    }

    private fun resetRetryState() {
        retryCount.set(0)
    }

    override fun setMaxRetryCount(maxRetryCount: Int) {
        this.maxRetries = maxRetryCount
    }

    override fun getMaxRetryCount(): Int = maxRetries
}
