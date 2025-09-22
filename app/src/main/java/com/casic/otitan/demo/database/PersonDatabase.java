package com.casic.otitan.demo.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.casic.otitan.demo.bean.Person;

/**
 * created by fz on 2024/11/6 10:44
 * describe:
 */
@Database(entities = {Person.class}, version = 1, exportSchema = false)
public abstract class PersonDatabase extends RoomDatabase {
    // 规定格式的写法，(写成别的，根据room的版本的不同，可能有运行异常)
    // Java编码规范中的约定
    public abstract PersonDao getPersonDao();

    private static volatile PersonDatabase personDatabase;

    // 获得UserInfoDatabase的实例
    /// 写法和SQLiteOpenHelper一致，用单例模式
    // room默认不能在主线程中操作数据库，因为数据库是一个耗时操作
    // 实际项目中，默认使用异步 --自学
    public static synchronized PersonDatabase getInstance(Context context) {
        if (personDatabase == null) {
            // 数据库的名字
            personDatabase = Room.databaseBuilder(context.getApplicationContext(),
                            PersonDatabase.class, "casic_titan_demo")
                    // 强制开启在主线程中操作数据库
                    .allowMainThreadQueries()
                    .build();
        }
        return personDatabase;
    }
}

