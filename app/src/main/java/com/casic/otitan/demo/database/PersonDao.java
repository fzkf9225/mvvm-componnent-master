package com.casic.otitan.demo.database;

import androidx.annotation.NonNull;
import androidx.room.Dao;

import com.casic.otitan.demo.bean.Person;

import com.casic.otitan.common.dao.BaseRoomDao;

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

