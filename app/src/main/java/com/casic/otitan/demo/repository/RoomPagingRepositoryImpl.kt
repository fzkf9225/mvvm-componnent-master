package com.casic.otitan.demo.repository;

import androidx.annotation.NonNull;

import com.casic.otitan.demo.bean.Person;
import com.casic.otitan.demo.database.PersonDao;

import com.casic.otitan.common.base.BaseView;
import com.casic.otitan.common.repository.RoomRepositoryImpl;

/**
 * Created by fz on 2023/12/1 15:25
 * describe :
 */
public class RoomPagingRepositoryImpl extends RoomRepositoryImpl<Person, PersonDao, BaseView> {

    public RoomPagingRepositoryImpl(@NonNull PersonDao roomDao, BaseView baseView) {
        super(roomDao, baseView);
    }


}
