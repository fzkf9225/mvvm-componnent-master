package com.casic.titan.demo.repository;

import com.casic.titan.demo.api.ApiServiceHelper;
import com.casic.titan.demo.bean.ForestBean;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Function;
import pers.fz.mvvm.api.ApiRetrofit;
import pers.fz.mvvm.bean.RequestConfigEntity;
import pers.fz.mvvm.bean.base.PageBean;
import pers.fz.mvvm.inter.RetryService;
import pers.fz.mvvm.repository.PagingRepository;

/**
 * Created by fz on 2023/12/1 15:25
 * describe :
 */
public class DemoPagingRepository extends PagingRepository<ForestBean> {
    private final ApiServiceHelper apiServiceHelper;

    public DemoPagingRepository(RetryService retryService) {
        super(retryService);
        apiServiceHelper = ApiRetrofit.getInstance().getApiService(ApiServiceHelper.class);
    }

    @Override
    public Observable<List<ForestBean>> requestPaging(int currentPage, int pageSize) {
        return sendRequest(apiServiceHelper.forestList(currentPage, pageSize),
                getRequestConfigEntity())
                .map(forestBeanPageBean -> {
                    if (forestBeanPageBean == null) {
                        return new ArrayList<>();
                    }
                    return forestBeanPageBean.getList();
                });
    }
}
