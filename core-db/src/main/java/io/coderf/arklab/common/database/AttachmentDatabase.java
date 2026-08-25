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
            // 列不存在时才 ADD（shouldAddColumn = 列尚不存在）
            if (shouldAddColumn(database, "AttachmentBean", "thumbnailPath")) {
                database.execSQL("ALTER TABLE AttachmentBean ADD COLUMN thumbnailPath TEXT");
            }
            if (shouldAddColumn(database, "AttachmentBean", "pitch")) {
                database.execSQL("ALTER TABLE AttachmentBean ADD COLUMN pitch REAL");
            }
            if (shouldAddColumn(database, "AttachmentBean", "yaw")) {
                database.execSQL("ALTER TABLE AttachmentBean ADD COLUMN yaw REAL");
            }
            if (shouldAddColumn(database, "AttachmentBean", "roll")) {
                database.execSQL("ALTER TABLE AttachmentBean ADD COLUMN roll REAL");
            }
            if (shouldAddColumn(database, "AttachmentBean", "longitude")) {
                database.execSQL("ALTER TABLE AttachmentBean ADD COLUMN longitude REAL");
            }
            if (shouldAddColumn(database, "AttachmentBean", "latitude")) {
                database.execSQL("ALTER TABLE AttachmentBean ADD COLUMN latitude REAL");
            }
            if (shouldAddColumn(database, "AttachmentBean", "height")) {
                database.execSQL("ALTER TABLE AttachmentBean ADD COLUMN height REAL");
            }
        }

        /**
         * 使用 PRAGMA table_info 判断列是否需要添加。
         *
         * @return true 表示列尚不存在，应执行 ADD COLUMN
         */
        private boolean shouldAddColumn(@NonNull SupportSQLiteDatabase database,
                                        @NonNull String tableName,
                                        @NonNull String columnName) {
            try (android.database.Cursor cursor =
                         database.query("PRAGMA table_info(`" + tableName + "`)")) {
                int nameIndex = cursor.getColumnIndex("name");
                if (nameIndex < 0) {
                    return true;
                }
                while (cursor.moveToNext()) {
                    String name = cursor.getString(nameIndex);
                    if (columnName.equalsIgnoreCase(name)) {
                        return false;
                    }
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
                attachmentDatabaseName = "io_coderf_arklab_attachment";
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
