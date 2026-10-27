package io.coderf.arklab.demo.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import io.coderf.arklab.core.db.RoomLog;
import io.coderf.arklab.demo.bean.LocalChatMessage;

/**
 * 本地聊天 Demo 库（与 PersonDatabase 隔离，避免影响既有 Room 分页示例）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/27
 */
@Database(entities = {LocalChatMessage.class}, version = 1, exportSchema = false)
public abstract class LocalChatDatabase extends RoomDatabase {

    public abstract LocalChatMessageDao getLocalChatMessageDao();

    private static volatile LocalChatDatabase instance;

    public static synchronized LocalChatDatabase getInstance(Context context) {
        if (instance == null) {
            instance = RoomLog.attachTo(
                    Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    LocalChatDatabase.class,
                                    "io_coderf_arklab_local_chat_demo")
                            .allowMainThreadQueries()
            ).build();
        }
        return instance;
    }
}
