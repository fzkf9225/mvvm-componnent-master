package io.coderf.arklab.demo.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.coderf.arklab.common.base.BaseRepository
import io.coderf.arklab.common.base.BaseViewModel
import io.coderf.arklab.common.base.launchUiRequest
import io.coderf.arklab.common.utils.download.DownloadClient
import io.coderf.arklab.common.utils.download.DownloadOptions
import io.coderf.arklab.common.utils.download.core.BatchDownloadConfig
import io.coderf.arklab.common.utils.download.core.DownloadConfig
import kotlinx.coroutines.CancellationException

/**
 * 下载 Demo：走协程 [DownloadClient] + 可选 [io.coderf.arklab.core.request.RequestUi]。
 * <p>
 * 传入 [networkRequestUiHost] 后，失败由框架 Toast；业务通常只处理成功。
 *
 * @author fz
 * @version 1.0
 * @since 1.2.0
 */
class DownloadViewModel(application: Application) : BaseViewModel<BaseRepository>(application) {

    private val _toast = MutableLiveData<String>()
    val toast: LiveData<String> = _toast

    override fun createRepository(): BaseRepository? = null

    fun download(activity: Activity, url: String) {
        val config = DownloadConfig.Builder(activity, url.trim()).build()
        launchUiRequest {
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
        launchUiRequest {
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
