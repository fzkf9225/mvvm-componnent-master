package io.coderf.arklab.demo.database;

import androidx.annotation.NonNull;
import androidx.room.Dao;

import io.coderf.arklab.demo.bean.Person;

import io.coderf.arklab.common.dao.BaseRoomDao;

/**
 * created by fz on 2024/11/6 10:31
 * describe:
 */
@Dao
public abstract class PersonDao extends BaseRoomDao<Person> {
    /**
     * 这个必须重写
     * @return
     */
    @NonNull
    @Override
    public String getTableName() {
        return Person.class.getSimpleName();
    }

}

