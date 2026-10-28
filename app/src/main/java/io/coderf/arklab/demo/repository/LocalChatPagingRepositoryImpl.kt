package io.coderf.arklab.demo.repository

import io.coderf.arklab.core.db.RoomDataRepository
import io.coderf.arklab.demo.bean.LocalChatMessage
import io.coderf.arklab.demo.database.LocalChatMessageDao

/**
 * Demo：本地聊天消息仓库（协程栈 [RoomDataRepository]）。
 * 分页走 [io.coderf.arklab.core.db.RoomPagingSource]。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/10/28
 */
class LocalChatPagingRepositoryImpl(
    roomDao: LocalChatMessageDao
) : RoomDataRepository<LocalChatMessage, LocalChatMessageDao>(roomDao)
