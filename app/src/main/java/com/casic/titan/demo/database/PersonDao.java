package com.casic.titan.demo.database;

import androidx.annotation.NonNull;
import androidx.room.Dao;

import com.casic.titan.demo.bean.Person;

import pers.fz.mvvm.database.BaseRoomDao;

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

