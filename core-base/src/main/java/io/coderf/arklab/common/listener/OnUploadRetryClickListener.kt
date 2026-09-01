package io.coderf.arklab.common.listener

import android.view.View

/**
 * OnUploadRetryClickListener 接口。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2025/4/28 10:28
 */
interface OnUploadRetryClickListener {
    fun onRetryClick(v: View, pos: Int)
}