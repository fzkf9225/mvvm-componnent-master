package io.coderf.arklab.common.utils.download

import android.app.Activity
import android.text.TextUtils
import android.widget.Toast
import io.coderf.arklab.common.base.BaseException
import io.coderf.arklab.common.utils.download.core.DownloadConfig
import io.coderf.arklab.common.utils.download.core.UpdateConfig
import io.coderf.arklab.common.utils.download.listener.ApkUpdateListener
import io.coderf.arklab.common.utils.download.listener.DownloadListener
import io.coderf.arklab.common.utils.download.util.DownloadNotificationUtil
import io.coderf.arklab.common.utils.download.util.DownloadUtil
import io.coderf.arklab.common.widget.dialog.UpdateMessageDialog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.concurrent.Volatile

/**
 * APK 版本更新管理器（单例）。
 *
 * - **静默更新**：[update] — 直接下载并调起安装，不弹说明框
 * - **对话框更新**：[checkUpdateInfo] — 展示更新说明，用户确认后下载
 *
 * 实际下载委托 [DownloadClient]（协程真内核）；防重复查询合并 Client 与存量 [DownloadManager]。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/11/12
 */
class UpdateManager private constructor() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /**
     * 正在下载的 URL 列表（合并协程 Client + 存量 Rx Manager，供更新对话框防重复）。
     */
    fun getDownloadingUrls(): List<String> {
        val urls = LinkedHashSet<String>()
        urls.addAll(DownloadClient.getInstance().getDownloadingUrls())
        urls.addAll(DownloadManager.getInstance().downloadingUrls)
        return ArrayList(urls)
    }

    // ========== 推荐 API ==========

    /**
     * 静默下载并安装 APK（不弹更新说明框）。
     *
     * @return 可 [Job.cancel] 的任务，用于页面销毁时取消下载
     */
    fun update(config: DownloadConfig): Job {
        return executeUpdate(resolveUpdateDownloadConfig(config))
    }

    /** 显示框架内置更新说明对话框。 */
    fun checkUpdateInfo(config: UpdateConfig) {
        showUpdateDialog(
            context = config.context,
            apkUrl = config.fileUrl,
            saveFileName = config.saveFileName,
            updateMsg = config.updateMessage,
            currentVersionName = config.currentVersionName,
            cancelEnable = config.isCancelEnable,
            verifyRepeatDownload = config.isVerifyRepeatDownload,
            headers = config.headers,
            downloadListener = config.downloadListener,
            showToast = config.isShowToast
        )
    }

    // ========== 便捷方法（向后兼容） ==========

    @JvmOverloads
    fun update(
        context: Activity,
        apkUrl: String,
        saveFileName: String? = null,
        verifyRepeatDownload: Boolean = true,
        headers: Map<String, String>? = null,
        downloadListener: DownloadListener? = null
    ): Job {
        return executeUpdate(
            DownloadConfig.Builder(context, apkUrl)
                .setSaveFileName(
                    if (TextUtils.isEmpty(saveFileName)) {
                        UpdateConfig.DEFAULT_APK_FILE_NAME
                    } else {
                        saveFileName
                    }
                )
                .setVerifyRepeatDownload(verifyRepeatDownload)
                .setHeaders(headers)
                .setDownloadListener(downloadListener)
                .setShowToast(true)
                .build()
        )
    }

    fun update(context: Activity, apkUrl: String, headers: Map<String, String>?): Job =
        update(context, apkUrl, null, true, headers, null)

    fun update(context: Activity, apkUrl: String, downloadListener: DownloadListener?): Job =
        update(context, apkUrl, null, true, null, downloadListener)

    fun update(context: Activity, apkUrl: String, verifyRepeatDownload: Boolean): Job =
        update(context, apkUrl, null, verifyRepeatDownload, null, null)

    fun update(
        context: Activity,
        apkUrl: String,
        verifyRepeatDownload: Boolean,
        headers: Map<String, String>?
    ): Job = update(context, apkUrl, null, verifyRepeatDownload, headers, null)

    fun update(
        context: Activity,
        apkUrl: String,
        verifyRepeatDownload: Boolean,
        downloadListener: DownloadListener?
    ): Job = update(context, apkUrl, null, verifyRepeatDownload, null, downloadListener)

    fun checkUpdateInfo(
        config: DownloadConfig,
        updateMsg: String?,
        currentVersionName: String?,
        cancelEnable: Boolean
    ) {
        checkUpdateInfo(
            context = config.context,
            apkUrl = config.fileUrl,
            saveFileName = config.saveFileName,
            updateMsg = updateMsg,
            currentVersionName = currentVersionName,
            cancelEnable = cancelEnable,
            verifyRepeatDownload = config.isVerifyRepeatDownload,
            headers = config.headers,
            downloadListener = config.downloadListener
        )
    }

    fun checkUpdateInfo(
        config: DownloadConfig,
        updateMsg: String?,
        currentVersionName: String?
    ) {
        checkUpdateInfo(config, updateMsg, currentVersionName, false)
    }

    fun checkUpdateInfo(
        context: Activity,
        apkUrl: String,
        saveFileName: String?,
        updateMsg: String?,
        currentVersionName: String?,
        verifyRepeatDownload: Boolean
    ) {
        checkUpdateInfo(
            context, apkUrl, saveFileName, updateMsg, currentVersionName,
            false, verifyRepeatDownload, null, null
        )
    }

    fun checkUpdateInfo(
        context: Activity,
        apkUrl: String,
        updateMsg: String?,
        currentVersionName: String?,
        verifyRepeatDownload: Boolean,
        downloadListener: DownloadListener?
    ) {
        checkUpdateInfo(
            context, apkUrl, null, updateMsg, currentVersionName,
            false, verifyRepeatDownload, null, downloadListener
        )
    }

    fun checkUpdateInfo(
        context: Activity,
        apkUrl: String,
        updateMsg: String?,
        currentVersionName: String?,
        verifyRepeatDownload: Boolean,
        headers: Map<String, String>?
    ) {
        checkUpdateInfo(
            context, apkUrl, null, updateMsg, currentVersionName,
            false, verifyRepeatDownload, headers, null
        )
    }

    fun checkUpdateInfo(
        context: Activity,
        apkUrl: String,
        saveFileName: String?,
        updateMsg: String?,
        currentVersionName: String?
    ) {
        checkUpdateInfo(
            context, apkUrl, saveFileName, updateMsg, currentVersionName,
            false, true, null, null
        )
    }

    fun checkUpdateInfo(
        context: Activity,
        apkUrl: String,
        updateMsg: String?,
        currentVersionName: String?,
        headers: Map<String, String>?
    ) {
        checkUpdateInfo(
            context, apkUrl, null, updateMsg, currentVersionName,
            false, true, headers, null
        )
    }

    fun checkUpdateInfo(
        context: Activity,
        apkUrl: String,
        updateMsg: String?,
        currentVersionName: String?,
        downloadListener: DownloadListener?
    ) {
        checkUpdateInfo(
            context, apkUrl, null, updateMsg, currentVersionName,
            false, true, null, downloadListener
        )
    }

    fun checkUpdateInfo(
        context: Activity,
        apkUrl: String,
        saveFileName: String?,
        updateMsg: String?,
        currentVersionName: String?,
        cancelEnable: Boolean,
        verifyRepeatDownload: Boolean
    ) {
        checkUpdateInfo(
            context, apkUrl, saveFileName, updateMsg, currentVersionName,
            cancelEnable, verifyRepeatDownload, null, null
        )
    }

    fun checkUpdateInfo(
        context: Activity,
        apkUrl: String,
        updateMsg: String?,
        currentVersionName: String?,
        cancelEnable: Boolean,
        verifyRepeatDownload: Boolean,
        headers: Map<String, String>?
    ) {
        checkUpdateInfo(
            context, apkUrl, null, updateMsg, currentVersionName,
            cancelEnable, verifyRepeatDownload, headers, null
        )
    }

    fun checkUpdateInfo(
        context: Activity,
        apkUrl: String,
        updateMsg: String?,
        currentVersionName: String?,
        cancelEnable: Boolean,
        verifyRepeatDownload: Boolean,
        downloadListener: DownloadListener?
    ) {
        checkUpdateInfo(
            context, apkUrl, null, updateMsg, currentVersionName,
            cancelEnable, verifyRepeatDownload, null, downloadListener
        )
    }

    /** 显示更新对话框（完整参数版，兼容旧代码）。 */
    fun checkUpdateInfo(
        context: Activity,
        apkUrl: String,
        saveFileName: String?,
        updateMsg: String?,
        currentVersionName: String?,
        cancelEnable: Boolean,
        verifyRepeatDownload: Boolean,
        headers: Map<String, String>?,
        downloadListener: DownloadListener?
    ) {
        showUpdateDialog(
            context, apkUrl, saveFileName, updateMsg, currentVersionName,
            cancelEnable, verifyRepeatDownload, headers, downloadListener, true
        )
    }

    // ========== 内部实现 ==========

    private fun executeUpdate(config: DownloadConfig): Job {
        val context = config.context
        return scope.launch {
            try {
                val file = DownloadClient.getInstance().downloadAwait(config)
                DownloadUtil.installApk(context.applicationContext, file)
            } catch (_: CancellationException) {
                // 主动取消：不弹失败通知
            } catch (e: Exception) {
                handleUpdateFailure(context, e)
            }
        }
    }

    private fun resolveUpdateDownloadConfig(config: DownloadConfig): DownloadConfig {
        if (!TextUtils.isEmpty(config.saveFileName)) {
            return config
        }
        return DownloadConfig.Builder(config.context, config.fileUrl)
            .setSaveBasePath(config.saveBasePath)
            .setSaveFileName(UpdateConfig.DEFAULT_APK_FILE_NAME)
            .setVerifyRepeatDownload(config.isVerifyRepeatDownload)
            .setHeaders(config.headers)
            .setDownloadListener(config.downloadListener)
            .setShowToast(config.isShowToast)
            .setTimeoutsSec(
                config.connectTimeoutSec,
                config.readTimeoutSec,
                config.writeTimeoutSec
            )
            .build()
    }

    private fun showUpdateDialog(
        context: Activity,
        apkUrl: String,
        saveFileName: String?,
        updateMsg: String?,
        currentVersionName: String?,
        cancelEnable: Boolean,
        verifyRepeatDownload: Boolean,
        headers: Map<String, String>?,
        downloadListener: DownloadListener?,
        showToast: Boolean
    ) {
        val resolvedName = if (TextUtils.isEmpty(saveFileName)) {
            UpdateConfig.DEFAULT_APK_FILE_NAME
        } else {
            saveFileName
        }
        UpdateMessageDialog(context)
            .setOnUpdateListener(
                ApkUpdateListener(
                    context, apkUrl, resolvedName,
                    getDownloadingUrls(), verifyRepeatDownload, headers, downloadListener, showToast
                )
            )
            .setCanCancel(cancelEnable)
            .setUpdateMsgString(updateMsg)
            .setVersionName(currentVersionName)
            .builder()
            .show()
    }

    private fun handleUpdateFailure(context: Activity, throwable: Throwable) {
        if (throwable is BaseException) {
            val code = throwable.errorCode
            if (BaseException.ErrorType.DOWNLOAD_URL_404.code == code
                || BaseException.ErrorType.DOWNLOADING_ERROR.code == code
                || BaseException.ErrorType.PERMISSION_DENIED.code == code
                || BaseException.ErrorType.DOWNLOAD_NOT_PERMISSION.code == code
                || BaseException.ErrorType.STORAGE_PERMISSION_DENIED.code == code
            ) {
                Toast.makeText(context, throwable.errorMsg, Toast.LENGTH_SHORT).show()
                return
            }
        }
        val message = if (throwable is BaseException) {
            throwable.errorMsg
        } else {
            throwable.message
        }
        DownloadNotificationUtil(context.applicationContext)
            .sendNotificationFullScreen(Random().nextInt(), "新版本下载失败", message, null)
    }

    companion object {
        @Volatile
        private var instance: UpdateManager? = null

        @JvmStatic
        fun getInstance(): UpdateManager {
            return instance ?: synchronized(this) {
                instance ?: UpdateManager().also { instance = it }
            }
        }
    }
}
