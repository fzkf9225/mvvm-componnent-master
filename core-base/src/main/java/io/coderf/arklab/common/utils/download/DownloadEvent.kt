package io.coderf.arklab.common.utils.download

import java.io.File

/**
 * 协程下载事件（[DownloadClient.download] 的 Flow 元素）。
 * <p>
 * 主动取消发 [Canceled] 并正常结束流，不走 [Failed]（对齐 Rx 栈 cancel → onComplete）。
 *
 * @author fz
 * @version 1.0
 * @since 1.2.0
 */
sealed class DownloadEvent {
    data object Started : DownloadEvent()
    data class Progress(val percent: Int) : DownloadEvent()
    data class Success(val file: File) : DownloadEvent()
    data class Failed(val error: Throwable) : DownloadEvent()
    data object Canceled : DownloadEvent()
}
