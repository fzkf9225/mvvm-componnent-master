package io.coderf.arklab.core.utils.ext

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

/**
 * 显示 Toast。
 * 仅依赖 Android SDK 基础 API，无业务 R / DataBinding 依赖。
 * @param message  消息内容
 * @param duration 显示时长，默认 [Toast.LENGTH_SHORT]
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * 显示 Toast（字符串资源）。
 *
 * @param resId    字符串资源 ID
 * @param duration 显示时长，默认 [Toast.LENGTH_SHORT]
 */
fun Context.showToast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, resId, duration).show()
}

/**
 * 快速读取 assets 目录下的文本文件。
 *
 * @param fileName assets 内相对路径，例如 `"config/demo.json"`
 * @return 文件全文；调用方需自行处理 IO 异常场景（本方法在 open 失败时会抛出）
 */
fun Context.readAssetsFile(fileName: String): String {
    return assets.open(fileName).bufferedReader().use { it.readText() }
}
