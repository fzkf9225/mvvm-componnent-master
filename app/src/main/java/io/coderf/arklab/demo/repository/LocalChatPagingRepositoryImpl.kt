package io.coderf.arklab.demo.repository

import io.coderf.arklab.common.repository.RoomRepositoryImpl
import io.coderf.arklab.demo.bean.LocalChatMessage
import io.coderf.arklab.demo.database.LocalChatMessageDao

/**
 * Demo：本地聊天消息仓库，分页走 [io.coderf.arklab.common.datasource.RxRoomPagingSource]。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/27
 */
class LocalChatPagingRepositoryImpl(
    roomDao: LocalChatMessageDao
) : RoomRepositoryImpl<LocalChatMessage, LocalChatMessageDao>(roomDao)
