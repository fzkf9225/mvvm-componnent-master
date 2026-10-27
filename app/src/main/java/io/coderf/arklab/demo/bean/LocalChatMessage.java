package io.coderf.arklab.demo.bean;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import io.coderf.arklab.common.bean.BaseDaoBean;

/**
 * 本地聊天消息（Demo）：仅演示 Room + Paging3 插入单条后列表更新，无真实通信。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/27
 */
@Entity(tableName = "LocalChatMessage")
public class LocalChatMessage extends BaseDaoBean {

    @ColumnInfo
    private String content;

    /** 毫秒时间戳，分页按此 DESC，配合 reverseLayout 贴底最新。 */
    @ColumnInfo
    private long createdAt;

    /** true=自己发送（右侧气泡），false=预置对方消息（左侧）。 */
    @ColumnInfo(name = "mine")
    private boolean mine;

    public LocalChatMessage() {
    }

    public LocalChatMessage(@NonNull String content, long createdAt, boolean mine) {
        this.content = content;
        this.createdAt = createdAt;
        this.mine = mine;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isMine() {
        return mine;
    }

    public void setMine(boolean mine) {
        this.mine = mine;
    }
}
