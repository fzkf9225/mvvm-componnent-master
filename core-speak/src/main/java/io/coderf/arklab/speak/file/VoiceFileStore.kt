package io.coderf.arklab.speak.file

import java.io.File

/**
 * 语音文件管理对外契约。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/25 13:42
 */
interface VoiceFileStore {

    /**
     * 创建新的录音目标文件（不会写入内容，仅保证父目录存在）。
     *
     * @param userId 可选用户分目录，空则落到 `All`
     */
    fun createRecordingFile(userId: String? = null): File

    /** 文件是否存在。 */
    fun exists(path: String?): Boolean

    /** 删除文件；路径空或不存在视为成功。 */
    fun delete(path: String?): Boolean

    /** 读取全部字节；失败返回空数组。 */
    fun readBytes(path: String?): ByteArray
}
