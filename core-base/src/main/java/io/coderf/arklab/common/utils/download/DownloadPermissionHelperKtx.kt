package io.coderf.arklab.common.utils.download

import android.app.Activity
import io.coderf.arklab.common.base.BaseException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 协程版存储权限等待。
 *
 * @author fz
 * @version 1.0
 * @since 1.2.0
 */
object DownloadPermissionHelperKtx {

    /**
     * 已有权限立即返回；否则弹窗并挂起，授权后继续，拒绝则抛
     * [BaseException.ErrorType.DOWNLOAD_NOT_PERMISSION]。
     */
    @JvmStatic
    suspend fun awaitStoragePermission(
        activity: Activity,
        requestCode: Int = DownloadPermissionHelper.REQUEST_CODE_DOWNLOAD
    ) {
        if (DownloadPermissionHelper.hasStoragePermission(activity)) {
            return
        }
        suspendCancellableCoroutine { cont ->
            DownloadPermissionHelper.requestPermission(
                activity,
                requestCode,
                {
                    if (cont.isActive) {
                        cont.resume(Unit)
                    }
                },
                { error ->
                    if (cont.isActive) {
                        cont.resumeWithException(
                            error ?: BaseException(BaseException.ErrorType.DOWNLOAD_NOT_PERMISSION)
                        )
                    }
                }
            )
            cont.invokeOnCancellation {
                DownloadPermissionHelper.clearPending(activity, requestCode)
            }
        }
    }
}
