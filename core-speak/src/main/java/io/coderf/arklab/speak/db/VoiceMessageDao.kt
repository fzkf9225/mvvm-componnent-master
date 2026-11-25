package io.coderf.arklab.speak.db

import androidx.room.Dao
import androidx.room.Query
import io.coderf.arklab.common.annotation.RoomObservedEntity

/**
 * 语音消息 Dao。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/25 13:42
 */
@Dao
@RoomObservedEntity(VoiceMessage::class)
abstract class VoiceMessageDao : VoiceMessageDaoRawQueryBridge() {

    override fun getTableName(): String = "VoiceMessage"

    @Query("SELECT * FROM VoiceMessage ORDER BY createdAt DESC")
    abstract suspend fun loadAllNewestFirst(): List<VoiceMessage>
}
