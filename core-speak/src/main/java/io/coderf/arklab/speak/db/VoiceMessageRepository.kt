package io.coderf.arklab.speak.db

import io.coderf.arklab.common.bean.RoomRequestOptions
import io.coderf.arklab.core.bean.RequestResult
import io.coderf.arklab.core.db.RoomDataRepository
import io.coderf.arklab.speak.file.VoiceFileStore
import io.coderf.arklab.speak.model.VoiceRecordResult
import kotlinx.coroutines.flow.Flow

/**
 * 语音元数据仓库（协程 RoomDataRepository）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/25 13:42
 */
class VoiceMessageRepository(
    dao: VoiceMessageDao,
    private val fileStore: VoiceFileStore
) : RoomDataRepository<VoiceMessage, VoiceMessageDao>(dao) {

    /**
     * 根据录音结果插入一条元数据。
     */
    fun insertRecord(
        result: VoiceRecordResult,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<RequestResult<Unit>> {
        val entity = VoiceMessage().apply {
            id = result.id
            filePath = result.filePath
            durationMs = result.durationMs
            sizeBytes = result.sizeBytes
            userId = result.userId
            createdAt = System.currentTimeMillis()
        }
        return insert(entity, options)
    }

    /**
     * 加载全部（最新在前）。
     */
    fun loadAllNewestFirst(
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<RequestResult<List<VoiceMessage>>> =
        execute(options) { getRoomDao().loadAllNewestFirst() }

    /**
     * 删除消息：先 [VoiceFileStore.delete]，再删库行。
     */
    fun deleteMessage(
        message: VoiceMessage,
        options: RoomRequestOptions = RoomRequestOptions.silent()
    ): Flow<RequestResult<Unit>> =
        execute(options) {
            fileStore.delete(message.filePath)
            getRoomDao().deleteSuspend(message)
        }
}
