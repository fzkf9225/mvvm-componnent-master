package com.casic.titan.demo.database;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.casic.titan.demo.bean.Person;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;
import pers.fz.mvvm.database.BaseRoomDao;

/**
 * created by fz on 2024/11/6 10:31
 * describe:
 */
@Dao
public abstract class PersonDao extends BaseRoomDao<Person> {
    @NonNull
    @Override
    public String getTableName() {
        return Person.class.getSimpleName();
    }

    @RawQuery(observedEntities = Person.class)
    @Override
    protected abstract Flowable<List<Person>> doQueryByParams(SupportSQLiteQuery query);
}

