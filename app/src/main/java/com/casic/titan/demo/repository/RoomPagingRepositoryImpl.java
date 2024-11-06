package com.casic.titan.demo.repository;

import androidx.annotation.NonNull;

import com.casic.titan.demo.api.ApiServiceHelper;
import com.casic.titan.demo.bean.ForestBean;
import com.casic.titan.demo.bean.Person;
import com.casic.titan.demo.database.PersonDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.bean.base.PageBean;
import pers.fz.mvvm.inter.RetryService;
import pers.fz.mvvm.repository.PagingRepositoryImpl;
import pers.fz.mvvm.repository.RoomRepositoryImpl;

/**
 * Created by fz on 2023/12/1 15:25
 * describe :
 */
public class RoomPagingRepositoryImpl extends RoomRepositoryImpl<Person, PersonDao, BaseView> {

    public RoomPagingRepositoryImpl(@NonNull PersonDao roomDao, BaseView baseView) {
        super(roomDao, baseView);
    }


}
