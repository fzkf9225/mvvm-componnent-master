package io.coderf.arklab.demo.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.coderf.arklab.common.base.BaseViewModel
import io.coderf.arklab.common.utils.download.DownloadClient
import io.coderf.arklab.common.utils.download.DownloadOptions
import io.coderf.arklab.common.utils.download.core.BatchDownloadConfig
import io.coderf.arklab.common.utils.download.core.DownloadConfig
import io.coderf.arklab.core.bean.RequestOptions
import kotlinx.coroutines.CancellationException

/**
 * 下载 Demo：走协程 [DownloadClient] + 可选 [io.coderf.arklab.core.request.RequestUi]。
 * DownloadClient 自行管理 RequestUi，故 [launchRequest] 使用 silent。
 *
 * @author fz
 * @version 1.1
 * @since 1.2.0
 * @updated 2026/11/13
 */
class DownloadViewModel(application: Application) : BaseViewModel(application) {

    private val _toast = MutableLiveData<String>()
    val toast: LiveData<String> = _toast

    fun download(activity: Activity, url: String) {
        val config = DownloadConfig.Builder(activity, url.trim()).build()
        launchRequest(RequestOptions.silent()) {
            try {
                val file = DownloadClient.getInstance().downloadAwait(
                    config = config,
                    options = DownloadOptions.defaults(),
                    requestUi = networkRequestUiHost
                )
                _toast.postValue("下载成功！\n${file.absolutePath}")
            } catch (_: CancellationException) {
                // Loading 取消 / 主动取消：不提示失败
            }
        }
    }

    fun downloadBatch(activity: Activity, urls: List<String>) {
        val config = BatchDownloadConfig.Builder(activity, urls).build()
        launchRequest(RequestOptions.silent()) {
            try {
                val files = DownloadClient.getInstance().downloadBatchAwait(
                    config = config,
                    options = DownloadOptions.defaults(),
                    requestUi = networkRequestUiHost
                )
                _toast.postValue("批量下载成功：${files.size} 个文件")
            } catch (_: CancellationException) {
            }
        }
    }
}
