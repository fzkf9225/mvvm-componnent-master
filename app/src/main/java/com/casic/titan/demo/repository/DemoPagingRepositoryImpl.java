package com.casic.titan.demo.repository;

import com.casic.titan.demo.api.ApiServiceHelper;
import com.casic.titan.demo.bean.ForestBean;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import pers.fz.mvvm.api.ApiRetrofit;
import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.inter.PagingView;
import pers.fz.mvvm.inter.RetryService;
import pers.fz.mvvm.repository.PagingRepositoryImpl;

/**
 * Created by fz on 2023/12/1 15:25
 * describe :
 */
public class DemoPagingRepositoryImpl extends PagingRepositoryImpl<ForestBean, PagingView> {
    private final ApiServiceHelper apiServiceHelper;

    public DemoPagingRepositoryImpl(RetryService retryService, PagingView baseView) {
        super(retryService, baseView);
        apiServiceHelper = ApiRetrofit.getInstance().getApiService(ApiServiceHelper.class);
    }


    @Override
    public Observable<List<ForestBean>> requestPaging(int currentPage, int pageSize) {
        return sendRequest(apiServiceHelper.forestList(currentPage, pageSize),
                getRequestConfigEntity());
    }
}
