package com.casic.otitan.common.database;

import android.content.Context;
import android.text.TextUtils;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.casic.otitan.common.bean.AttachmentBean;
import com.casic.otitan.common.dao.AttachmentDao;

/**
 * created by fz on 2024/11/6 10:44
 * describe:
 */
@Database(entities = {AttachmentBean.class}, version = 1, exportSchema = false)
public abstract class AttachmentDatabase extends RoomDatabase {
    // 规定格式的写法，(写成别的，根据room的版本的不同，可能有运行异常)
    // Java编码规范中的约定
    public abstract AttachmentDao getAttachmentDao();

    protected static volatile AttachmentDatabase attachmentDatabase;

    // 获得UserInfoDatabase的实例

    /// 写法和SQLiteOpenHelper一致，用单例模式
    // room默认不能在主线程中操作数据库，因为数据库是一个耗时操作
    // 实际项目中，默认使用异步 --自学
    public static synchronized AttachmentDatabase getInstance(Context context, String attachmentDatabaseName) {
        if (attachmentDatabase == null) {
            if (TextUtils.isEmpty(attachmentDatabaseName)) {
                attachmentDatabaseName = "casic_titan_attachment";
            }

            // 数据库的名字
            attachmentDatabase = Room.databaseBuilder(context.getApplicationContext(),
                            AttachmentDatabase.class, attachmentDatabaseName)
                    // 强制开启在主线程中操作数据库
                    .allowMainThreadQueries()
                    .build();
        }
        return attachmentDatabase;
    }
}

