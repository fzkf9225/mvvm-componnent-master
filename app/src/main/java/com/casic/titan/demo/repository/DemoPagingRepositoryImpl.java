package com.casic.titan.demo.repository;

import com.casic.titan.demo.api.ApiServiceHelper;
import com.casic.titan.demo.bean.RegionBean;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.inter.RetryService;
import pers.fz.mvvm.repository.PagingRepositoryImpl;

/**
 * Created by fz on 2023/12/1 15:25
 * describe :
 */
public class DemoPagingRepositoryImpl extends PagingRepositoryImpl<RegionBean, BaseView> {
    private ApiServiceHelper apiServiceHelper;

    public DemoPagingRepositoryImpl(ApiServiceHelper apiServiceHelper, RetryService retryService, BaseView baseView) {
        super(retryService, baseView);
        this.apiServiceHelper = apiServiceHelper;
    }

    @Override
    public Observable<List<RegionBean>> requestPaging(int currentPage, int pageSize) {
        return sendRequest(
                apiServiceHelper.getRegionTree(),
                getRequestConfigEntity());
    }
}
