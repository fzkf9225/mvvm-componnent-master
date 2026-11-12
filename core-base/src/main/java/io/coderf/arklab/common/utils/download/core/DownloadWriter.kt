package io.coderf.arklab.common.utils.download.core

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import io.coderf.arklab.common.api.Config
import io.coderf.arklab.common.utils.download.listener.DownloadListener
import io.coderf.arklab.common.utils.download.util.DownloadNotificationUtil
import io.coderf.arklab.core.utils.FileUtil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.Headers
import okhttp3.ResponseBody
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import kotlin.coroutines.coroutineContext

/**
 * 协程写盘：断点追加临时文件、主线程进度/通知、完成后重命名。
 * <p>
 * 与 [DownloadObservable] 行为对齐；取消时保留 `.download` 临时文件以便续传。
 *
 * @author fz
 * @version 1.0
 * @since 1.2.0
 */
object DownloadWriter {

    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * 将 [body] 追加写入 [tempFile]，成功后 rename 为目标文件。
     *
     * @throws CancellationException 协程取消（会回调 [DownloadListener.onCancel]）
     * @throws IOException 读写失败（会回调 [DownloadListener.onError]）
     */
    @JvmStatic
    suspend fun write(
        body: ResponseBody,
        headers: Headers,
        fileUrl: String,
        tempFile: File,
        saveBasePath: String,
        saveFileName: String?,
        downloadListener: DownloadListener?,
        showToast: Boolean
    ): File = withContext(Dispatchers.IO) {
        val notificationUtil = DownloadNotificationUtil(Config.getInstance().application)
        val notifyId = fileUrl.hashCode()
        var canceled = false

        postMain {
            notificationUtil.showNotification(notifyId)
            downloadListener?.onStart()
        }

        try {
            body.use { responseBody ->
                val totalByte = responseBody.contentLength()
                var downloadByte = 0L
                val parent = tempFile.parentFile
                if (parent != null && !parent.exists()) {
                    parent.mkdirs()
                }

                val buffer = ByteArray(1024 * 4)
                RandomAccessFile(tempFile, "rwd").use { raf ->
                    raf.seek(tempFile.length())
                    val input = responseBody.byteStream()
                    while (true) {
                        coroutineContext.ensureActive()
                        val len = input.read(buffer)
                        if (len == -1) break
                        raf.write(buffer, 0, len)
                        downloadByte += len
                        val progress = if (totalByte > 0) {
                            ((downloadByte * 100) / totalByte).toInt().coerceIn(0, 100)
                        } else {
                            0
                        }
                        postMain {
                            notificationUtil.updateNotification(
                                notifyId, progress, FileUtil.getFileName(fileUrl)
                            )
                            downloadListener?.onProgress(progress)
                        }
                    }
                }

                val fileName = DownloadFileNames.resolveSaveFileName(
                    saveBasePath, saveFileName, fileUrl, headers, responseBody
                )
                val newFile = File(saveBasePath, fileName)
                val resultFile = if (tempFile.renameTo(newFile)) newFile else tempFile

                postMain {
                    notificationUtil.cancelNotification(notifyId)
                    if (showToast) {
                        Toast.makeText(
                            Config.getInstance().application,
                            "文件已保存至${resultFile.absolutePath}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    downloadListener?.onFinish(resultFile)
                }
                resultFile
            }
        } catch (e: CancellationException) {
            canceled = true
            postMain {
                notificationUtil.cancelNotification(notifyId)
                downloadListener?.onCancel()
            }
            throw e
        } catch (e: IOException) {
            if (!canceled) {
                postMain {
                    notificationUtil.cancelNotification(notifyId)
                    downloadListener?.onError(e)
                }
            }
            throw e
        } catch (e: Exception) {
            if (!canceled) {
                postMain {
                    notificationUtil.cancelNotification(notifyId)
                    downloadListener?.onError(e)
                }
            }
            throw e
        }
    }

    private fun postMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
        } else {
            mainHandler.post(block)
        }
    }
}
