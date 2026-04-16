package io.coderf.arklab.common.database;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import io.coderf.arklab.common.bean.AttachmentBean;
import io.coderf.arklab.common.dao.AttachmentDao;

/**
 * created by fz on 2024/11/6 10:44
 * describe:
 */
@Database(entities = {AttachmentBean.class}, version = 2, exportSchema = false)
public abstract class AttachmentDatabase extends RoomDatabase {
    // 规定格式的写法，(写成别的，根据room的版本的不同，可能有运行异常)
    // Java编码规范中的约定
    public abstract AttachmentDao getAttachmentDao();

    protected static volatile AttachmentDatabase attachmentDatabase;

    // 定义从版本1到版本2的迁移策略
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // 检查并添加thumbnailPath列
            if (isColumnExists(database, "thumbnailPath")) {
                database.execSQL("ALTER TABLE AttachmentBean ADD COLUMN thumbnailPath TEXT");
            }
            // 检查并添加俯仰角字段
            if (isColumnExists(database, "pitch")) {
                database.execSQL("ALTER TABLE AttachmentBean ADD COLUMN pitch REAL");
            }
            // 检查并添加偏航角字段
            if (isColumnExists(database, "yaw")) {
                database.execSQL("ALTER TABLE AttachmentBean ADD COLUMN yaw REAL");
            }
            // 检查并添加翻滚角字段
            if (isColumnExists(database, "roll")) {
                database.execSQL("ALTER TABLE AttachmentBean ADD COLUMN roll REAL");
            }
            // 检查并添加拍照时所在经度字段
            if (isColumnExists(database, "longitude")) {
                database.execSQL("ALTER TABLE AttachmentBean ADD COLUMN longitude REAL");
            }
            // 检查并添加拍照时所在纬度字段
            if (isColumnExists(database, "latitude")) {
                database.execSQL("ALTER TABLE AttachmentBean ADD COLUMN latitude REAL");
            }
            // 检查并添加拍照时所在海拔高程字段
            if (isColumnExists(database, "height")) {
                database.execSQL("ALTER TABLE AttachmentBean ADD COLUMN height REAL");
            }
        }

        /**
         * 检查表中是否存在指定列
         *
         * @param database   SQLite数据库
         * @param columnName 列名
         * @return 存在返回true，否则返回false
         */
        private boolean isColumnExists(@NonNull SupportSQLiteDatabase database,
                                       @NonNull String columnName) {
            String query = "SELECT COUNT(*) FROM sqlite_master " +
                    "WHERE type = 'table' " +
                    "AND name = ? " +
                    "AND sql LIKE ?";
            String likePattern = "%" + columnName + "%";
            try (android.database.Cursor cursor = database.query(query, new String[]{"AttachmentBean", likePattern})) {
                if (cursor.moveToFirst()) {
                    return cursor.getInt(0) <= 0;
                }
                return true;
            }
        }
    };

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
                    // 添加迁移策略
                    .addMigrations(MIGRATION_1_2)
                    // 强制开启在主线程中操作数据库
                    .allowMainThreadQueries()
                    .build();
        }
        return attachmentDatabase;
    }
}