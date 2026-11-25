package io.coderf.arklab.speak.file

import android.content.Context
import android.os.Environment
import io.coderf.arklab.core.utils.FileUtil
import io.coderf.arklab.core.utils.StringUtil
import java.io.File

/**
 * 默认语音文件存储：`files/Voice/{userId|All}/voice_*.amr`。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/25 13:42
 */
class DefaultVoiceFileStore(
    context: Context,
    private val folderName: String? = null,
    private val extension: String = ".amr"
) : VoiceFileStore {

    private val appContext = context.applicationContext

    override fun createRecordingFile(userId: String?): File {
        val dir = voiceDir(userId)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val name = FileUtil.getNoRepeatFileName(dir, "voice_", extension)
        return File(dir, name + extension)
    }

    override fun exists(path: String?): Boolean {
        if (path.isNullOrBlank()) return false
        return File(path).isFile
    }

    override fun delete(path: String?): Boolean {
        if (path.isNullOrBlank()) return true
        return FileUtil.deleteFile(path)
    }

    override fun readBytes(path: String?): ByteArray {
        if (path.isNullOrBlank()) return ByteArray(0)
        return FileUtil.readBytes(path)
    }

    /**
     * 语音根目录：`getExternalFilesDir(MUSIC)/{appId}/voice/{userId|All}`。
     */
    fun voiceDir(userId: String?): File {
        val music = appContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            ?: File(appContext.filesDir, Environment.DIRECTORY_MUSIC)
        val appId = FileUtil.getDefaultBasePath(appContext, folderName)
        val uid = userId?.takeIf { it.isNotBlank() } ?: "All"
        return File(File(music, appId), "voice${File.separator}$uid")
    }

    companion object {
        /** 生成可与 Room 主键对齐的 id。 */
        fun newVoiceId(): String = StringUtil.getUUID()
    }
}
