package com.casic.titan.demo.database;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.casic.titan.demo.bean.Person;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
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

    @Query("SELECT * FROM Person")
    public abstract Flowable<List<Person>> getAllEntities();
    /**
     * 下面这几个可以看情况重写，主要是observedEntities得用法观察数据，在base中已经使用了占位符
     */
    @RawQuery(observedEntities = Person.class)
    @Override
    protected abstract LiveData<Person> doFindLiveData(SupportSQLiteQuery query);

    @RawQuery(observedEntities = Person.class)
    @Override
    protected abstract LiveData<List<Person>> doQueryByParamsLiveData(SupportSQLiteQuery query);

    @RawQuery(observedEntities = Person.class)
    @Override
    protected abstract Single<Person> doFind(SupportSQLiteQuery query);

    @RawQuery(observedEntities = Person.class)
    @Override
    protected abstract Flowable<List<Person>> doQueryByParams(SupportSQLiteQuery query);

    @RawQuery(observedEntities = Person.class)
    @Override
    protected abstract List<Person> findPageList(SupportSQLiteQuery query);
}

