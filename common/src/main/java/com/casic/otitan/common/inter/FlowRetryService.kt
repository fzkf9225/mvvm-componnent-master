package com.casic.otitan.common.inter

/**
 * Kotlin协程版本的错误重试接口
 */
interface FlowRetryService {
    /**
     * 判断是否应该重试
     */
    suspend fun shouldRetry(throwable: Throwable): Boolean

    /**
     * 执行刷新Token等重试操作
     */
    suspend fun refreshToken()

    /**
     * 设置最大重试次数
     */
    fun setMaxRetryCount(maxRetryCount: Int)

    /**
     * 获取最大重试次数
     */
    fun getMaxRetryCount(): Int

}