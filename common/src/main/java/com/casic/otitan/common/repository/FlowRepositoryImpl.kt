package com.casic.otitan.common.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.casic.otitan.common.api.ApiRetrofit
import com.casic.otitan.common.api.BaseApiService
import com.casic.otitan.common.api.ErrorConsumer
import com.casic.otitan.common.base.BaseException
import com.casic.otitan.common.base.BaseRepository
import com.casic.otitan.common.base.BaseView
import com.casic.otitan.common.bean.ApiRequestOptions
import com.casic.otitan.common.inter.FlowRetryService
import com.casic.otitan.common.utils.log.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * created by fz on 2023/12/17 14:00
 * Kotlin Flow版本的Repository，简化版，只保留核心方法
 */
abstract class FlowRepositoryImpl<API : BaseApiService, BV : BaseView> : BaseRepository<BV> {

    var apiService: API? = null

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    var flowRetryService: FlowRetryService? = null

    // region 构造函数（保持原有构造以便兼容）
    constructor() : super()

    constructor(baseView: BV) : super(baseView)

    constructor(apiService: API) : super() {
        this.apiService = apiService
    }

    constructor(baseView: BV, apiService: API) : super(baseView) {
        this.apiService = apiService
    }

    constructor(flowRetryService: FlowRetryService) : super() {
        this.flowRetryService = flowRetryService
    }

    constructor(flowRetryService: FlowRetryService, baseView: BV) : super(baseView) {
        this.flowRetryService = flowRetryService
    }

    constructor(flowRetryService: FlowRetryService, apiService: API) : super() {
        this.flowRetryService = flowRetryService
        this.apiService = apiService
    }

    constructor(
        flowRetryService: FlowRetryService,
        baseView: BV,
        apiService: API
    ) : super(baseView) {
        this.apiService = apiService
        this.flowRetryService = flowRetryService
    }

    /**
     * 发送请求的核心方法，返回Flow<T>
     */
    fun <T> sendRequest(
        request: suspend () -> T,
        apiRequestOptions: ApiRequestOptions = ApiRequestOptions.getDefault()
    ): Flow<T> {
        return flow {
            try {
                val result = withContext(Dispatchers.IO) {
                    request()
                }
                emit(result)
            } catch (e: Throwable) {
                throw e
            }
        }
            .onStart {
                withContext(Dispatchers.Main) {
                    if (apiRequestOptions.isShowDialog) {
                        baseView?.showLoading(
                            apiRequestOptions.dialogMessage,
                            apiRequestOptions.isEnableDynamicEllipsis
                        )
                    }
                }
            }
            .onCompletion {
                withContext(Dispatchers.Main) {
                    if (apiRequestOptions.isShowDialog) {
                        baseView?.hideLoading()
                    }
                }
            }
            .retryWhen { cause, attempt ->
                handleRetry(cause, attempt)
            }
            .flowOn(Dispatchers.Main)
            .catch { throwable ->
                handleFlowError(throwable, apiRequestOptions)
            }
    }

    // endregion

    // region 简化版的便捷方法（对应原有各种重载）

    /**
     * 简化的便捷方法：直接返回Flow
     */
    fun <T> sendFlow(
        request: suspend () -> T,
        showDialog: Boolean = true,
        dialogMessage: String = "正在加载，请稍后...",
        enableDynamicEllipsis: Boolean = false,
        showToast: Boolean = true
    ): Flow<T> {
        val options = ApiRequestOptions.Builder()
            .setShowDialog(showDialog)
            .setDialogMessage(dialogMessage)
            .enableDynamicEllipsis(enableDynamicEllipsis)
            .setShowToast(showToast)
            .build()

        return sendRequest(request, options)
    }

    /**
     * 结果存入LiveData的便捷方法
     */
    fun <T> sendToLiveData(
        request: suspend () -> T,
        liveData: MutableLiveData<T>,
        showDialog: Boolean = true,
        dialogMessage: String = "正在加载，请稍后...",
        enableDynamicEllipsis: Boolean = false,
        onError: ((Throwable) -> Unit)? = null
    ): Job {
        val options = ApiRequestOptions.Builder()
            .setShowDialog(showDialog)
            .setDialogMessage(dialogMessage)
            .enableDynamicEllipsis(enableDynamicEllipsis)
            .build()

        return coroutineScope.launch {
            sendRequest(request, options)
                .catch { throwable ->
                    (onError ?: { error ->
                        ErrorConsumer(baseView, options).accept(throwable)
//                        FlowErrorHandler(baseView, options).handleError(error)
                    }).invoke(throwable)
                }
                .collect { result ->
                    liveData.value = result
                }
        }
    }

    /**
     * 使用回调的便捷方法
     */
    fun <T> sendWithCallback(
        request: suspend () -> T,
        onSuccess: (T) -> Unit,
        onError: ((Throwable) -> Unit)? = null,
        showDialog: Boolean = true,
        dialogMessage: String = "正在加载，请稍后...",
        enableDynamicEllipsis: Boolean = false
    ): Job {
        val options = ApiRequestOptions.Builder()
            .setShowDialog(showDialog)
            .setDialogMessage(dialogMessage)
            .enableDynamicEllipsis(enableDynamicEllipsis)
            .build()

        return coroutineScope.launch {
            sendRequest(request, options)
                .catch { throwable ->
                    (onError ?: { error ->
                        ErrorConsumer(baseView, options).accept(throwable)
                    }).invoke(throwable)
                }
                .collect { result ->
                    onSuccess(result)
                }
        }
    }

    /**
     * 处理重试逻辑
     */
    private suspend fun handleRetry(
        cause: Throwable,
        attempt: Long
    ): Boolean {
        val retryService: FlowRetryService? =
            flowRetryService ?: apiService?.retrofit?.builder?.flowRetryService
        // 判断是否应该重试
        val shouldRetry = retryService?.shouldRetry(cause) ?: false
        if (shouldRetry && (attempt + 1) < (retryService?.getMaxRetryCount() ?: 0)) {
            // 执行重试操作
            retryService?.refreshToken()
            return true
        }
        return false
    }

    /**
     * Flow错误处理
     */
    suspend fun handleFlowError(
        throwable: Throwable,
        apiRequestOptions: ApiRequestOptions
    ) {
        withContext(Dispatchers.Main) {
            ErrorConsumer(baseView, apiRequestOptions).accept(throwable)
        }
    }

    /**
     * 清理协程作用域
     */
    override fun clear() {
        super.clear()
        coroutineScope.cancel("Repository cleared")
    }

    /**
     * 移除单个请求
     */
    fun cancelRequest(job: Job) {
        job.cancel()
    }

    /**
     * 获取当前协程作用域
     */
    fun getCoroutineScope(): CoroutineScope = coroutineScope

    // endregion

    // region 扩展方法

    /**
     * 转换为LiveData
     */
    fun <T> Flow<T>.asLiveData(): LiveData<T> {
        val liveData = MutableLiveData<T>()
        coroutineScope.launch {
            this@asLiveData.collect { value ->
                liveData.value = value
            }
        }
        return liveData
    }

    /**
     * 安全请求（不抛出异常）
     */
    suspend fun <T> safeRequest(
        request: suspend () -> T,
        apiRequestOptions: ApiRequestOptions = ApiRequestOptions.getDefault()
    ): Result<T> {
        return try {
            val result = sendRequest(request, apiRequestOptions).first()
            Result.success(result)
        } catch (e: Exception) {
            LogUtil.e(ApiRetrofit.TAG, "Safe request failed: $e")
            Result.failure(e)
        }
    }

    /**
     * 带超时的请求
     */
    fun <T> sendRequestWithTimeout(
        request: suspend () -> T,
        timeoutMillis: Long = 30000L,
        showDialog: Boolean = true,
        dialogMessage: String = "正在加载，请稍后...",
        enableDynamicEllipsis: Boolean = false
    ): Flow<T> {
        val options = ApiRequestOptions.Builder()
            .setShowDialog(showDialog)
            .setDialogMessage(dialogMessage)
            .enableDynamicEllipsis(enableDynamicEllipsis)
            .build()

        return flow {
            val result = withTimeoutOrNull(timeoutMillis) {
                request()
            }
            if (result != null) {
                emit(result)
            } else {
                throw BaseException(BaseException.ErrorType.CONNECT_TIMEOUT)
            }
        }
            .onStart {
                withContext(Dispatchers.Main) {
                    if (options.isShowDialog) {
                        baseView?.showLoading(
                            options.dialogMessage,
                            options.isEnableDynamicEllipsis
                        )
                    }
                }
            }
            .onCompletion {
                withContext(Dispatchers.Main) {
                    if (options.isShowDialog) {
                        baseView?.hideLoading()
                    }
                }
            }
            .retryWhen { cause, attempt ->
                handleRetry(cause, attempt)
            }
            .flowOn(Dispatchers.Main)
            .catch { throwable ->
                handleFlowError(throwable, options)
            }
    }

    /**
     * 分页请求的便捷方法
     */
    fun <T> sendPagingRequest(
        page: Int,
        pageSize: Int,
        request: suspend (Int, Int) -> T,
        showDialog: Boolean = true,
        dialogMessage: String = "正在加载，请稍后...",
        enableDynamicEllipsis: Boolean = false
    ): Flow<T> {
        return sendFlow(
            request = { request(page, pageSize) },
            showDialog = showDialog,
            dialogMessage = dialogMessage,
            enableDynamicEllipsis = enableDynamicEllipsis
        )
    }

    /**
     * 更新Loading对话框文本
     */
    fun updateLoadingMessage(message: String) {
        baseView?.refreshLoading(message)
    }
}
