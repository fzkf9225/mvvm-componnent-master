package com.casic.titan.demo.repository;

import com.casic.titan.demo.api.ApiServiceHelper;
import com.casic.titan.demo.bean.ForestBean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.bean.base.PageBean;
import pers.fz.mvvm.inter.RetryService;
import pers.fz.mvvm.repository.PagingRepositoryImpl;

/**
 * Created by fz on 2023/12/1 15:25
 * describe :
 */
public class DemoPagingRepositoryImpl extends PagingRepositoryImpl<ForestBean, BaseView> {
    private ApiServiceHelper apiServiceHelper;

    public DemoPagingRepositoryImpl(ApiServiceHelper apiServiceHelper, RetryService retryService, BaseView baseView) {
        super(retryService, baseView);
        this.apiServiceHelper = apiServiceHelper;
    }

    @Override
    public Observable<List<ForestBean>> requestPaging(int currentPage, int pageSize) {
        Map<String,String> body = new HashMap<>();
        body.put("maintainClass","MAINTAIN_TREE_BODY");
        return sendRequest(
                apiServiceHelper.forestList(currentPage, pageSize,body)
                        .map(PageBean::getList),
                getRequestConfigEntity());
    }
}
