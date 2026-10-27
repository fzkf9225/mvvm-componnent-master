package io.coderf.arklab.demo.database;

import androidx.annotation.NonNull;
import androidx.room.Dao;

import io.coderf.arklab.common.annotation.RoomObservedEntity;
import io.coderf.arklab.demo.bean.LocalChatMessage;

/**
 * 本地聊天消息 Dao（Demo）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/27
 */
@Dao
@RoomObservedEntity(LocalChatMessage.class)
public abstract class LocalChatMessageDao extends LocalChatMessageDaoRawQueryBridge {

    @NonNull
    @Override
    public String getTableName() {
        return "LocalChatMessage";
    }
}
