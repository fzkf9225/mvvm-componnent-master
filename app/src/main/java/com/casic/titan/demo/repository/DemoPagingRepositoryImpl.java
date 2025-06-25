package com.casic.titan.demo.repository;

import com.casic.titan.demo.api.ApiServiceHelper;
import com.casic.titan.demo.bean.NotificationMessageBean;

import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.inter.RetryService;
import pers.fz.mvvm.repository.PagingRepositoryImpl;

/**
 * Created by fz on 2023/12/1 15:25
 * describe :
 */
public class DemoPagingRepositoryImpl extends PagingRepositoryImpl<ApiServiceHelper,NotificationMessageBean, BaseView> {

    public DemoPagingRepositoryImpl(RetryService retryService, BaseView baseView) {
        super(retryService, baseView);
    }

    public DemoPagingRepositoryImpl(BaseView baseView) {
        super(baseView);
    }

    @Override
    public Observable<List<NotificationMessageBean>> requestPaging(int currentPage, int pageSize) {
        NotificationMessageBean notificationMessageBean = new NotificationMessageBean();
        notificationMessageBean.setType("1");
        return sendRequest(
                apiService.getNewList(currentPage,pageSize,notificationMessageBean).map(response -> {
                    if(response.getList()==null){
                        return Collections.emptyList();
                    }
                    return response.getList();
                }),
                getApiRequestOptions());
    }
}
