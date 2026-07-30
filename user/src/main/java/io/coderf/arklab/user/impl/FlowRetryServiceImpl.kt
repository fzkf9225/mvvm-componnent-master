package io.coderf.arklab.user.impl

import io.coderf.arklab.common.api.ApiRetrofit
import io.coderf.arklab.common.api.ConstantsHelper
import io.coderf.arklab.common.base.BaseException
import io.coderf.arklab.common.inter.FlowRetryService
import io.coderf.arklab.common.utils.log.LogUtil
import io.coderf.arklab.user.api.UserAccountHelper
import io.coderf.arklab.user.api.UserApiService
import kotlinx.coroutines.CompletableDeferred
import retrofit2.HttpException
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Kotlin 协程版本的重试机制实现。
 *
 * 演示 case：多播等待（[CompletableDeferred.await]）+ Single-Flight，
 * 避免多个独立协程请求并发 401 时各自刷 token 形成竞态。
 *
 * 与 RxJava 版 [RetryServiceImpl] 的对应关系：
 * - RxJava：`share()` 多播 + 共享 [Observable] 指针
 * - 协程：`CompletableDeferred` 多路 await + 共享 Deferred 指针
 *
 * 服务端 refresh_token 通常只能消费一次：先成功者拿到新 token，
 * 后发起者仍携带旧 refresh_token 会被判定无效，导致误登出。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/7/30 15:25
 */
@Singleton
class FlowRetryServiceImpl @Inject constructor(
    private val userApiService: UserApiService
) : FlowRetryService {
    /**
     * 最大出错重试次数
     */
    private var maxRetries = ConstantsHelper.RETRY_WHEN_MAX_COUNT

    /**
     * 当前出错重试次数（多请求并发时共享）
     */
    private val retryCount = AtomicInteger(0)

    /**
     * Single-Flight 专用锁，仅用于「创建 / 复用 / 清空」[refreshFlight] 指针，不在锁内等待网络。
     */
    private val refreshFlightLock = Any()

    /**
     * 当前正在进行的 refresh Deferred。
     *
     * Single-Flight 策略：
     * - 第一个进入的调用创建 [CompletableDeferred] 并真正执行 refresh；
     * - 后续并发调用直接 await 同一 Deferred，不再重复打 refresh 接口；
     * - refresh 结束（成功或失败）后清空指针，允许下一轮 refresh。
     *
     * 为何不在 synchronized 内等待网络：锁内只做指针交换，网络请求在锁外 suspend 完成。
     */
    @Volatile
    private var refreshFlight: CompletableDeferred<Unit>? = null

    override suspend fun shouldRetry(throwable: Throwable): Boolean {
        return when (throwable) {
            is BaseException -> {
                val count = retryCount.incrementAndGet()
                LogUtil.logger(ApiRetrofit.TAG, "第 $count 次重试，BaseException：$throwable")
                val isLoginPastOrNoPermission = true
                if (isLoginPastOrNoPermission && count <= maxRetries) {
                    true
                } else {
                    resetRetryState()
                    false
                }
            }

            is HttpException -> {
                val count = retryCount.incrementAndGet()
                LogUtil.logger(ApiRetrofit.TAG, "第 $count 次重试，HttpException：$throwable")
                if (401 == throwable.code() && count <= maxRetries) {
                    true
                } else {
                    resetRetryState()
                    false
                }
            }

            else -> {
                LogUtil.logger(ApiRetrofit.TAG, "不满足重试条件！")
                resetRetryState()
                false
            }
        }
    }

    /**
     * Token 刷新入口：走 Single-Flight，并发调用复用同一次 refresh。
     *
     * 并发时序示例：
     * ```
     * 协程A 401 ──┐
     *              ├──► 共享 refreshFlight（Deferred 多路 await）──► 成功 ──► A 重试原接口
     * 协程B 401 ──┘                                              └──► B 重试原接口
     * ```
     */
    override suspend fun refreshToken() {
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

    /**
     * 真正发起 refresh 网络请求；仅由 [refreshToken] 在「新建 flight」时调用一次。
     */
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
