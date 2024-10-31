package com.casic.titan.demo.viewmodel;

import android.app.Application;
import android.view.View;

import androidx.annotation.NonNull;

import com.casic.titan.demo.R;
import com.casic.titan.demo.bean.HiltTestBean;
import com.casic.titan.demo.impl.UserServiceEntryPoint;
import com.casic.titan.demo.inter.HiltUserService;
import com.casic.titan.demo.module.HiltUserServiceModule;

import javax.inject.Inject;

import dagger.hilt.android.EntryPointAccessors;
import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.base.BaseViewModel;
import pers.fz.mvvm.inter.RetryService;
import pers.fz.mvvm.repository.RepositoryImpl;

/**
 * Created by fz on 2024/5/31 11:31
 * describe :
 */
@dagger.hilt.android.lifecycle.HiltViewModel
public class HiltViewModel extends BaseViewModel<RepositoryImpl, BaseView> {
    @Inject
    @HiltUserServiceModule.HiltUser
    HiltUserService hiltUserService;

    @Inject
    HiltTestBean hiltTestBean;

    @Inject
    @HiltUserServiceModule.NewHiltUser
    HiltUserService newHiltUserService;

    @Inject
    @HiltUserServiceModule.NewHiltUser
    HiltUserService contextHiltUserService;

    @Inject
    public RetryService retryService;

    @Inject
    public HiltViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected RepositoryImpl createRepository() {
        return new RepositoryImpl(retryService,baseView);
    }

    public void onClick(View view) {
        if (R.id.button_inter == view.getId()) {
            hiltUserService.onLogin("张三", "123456");
        } else if (R.id.button_entity == view.getId()) {
            System.out.println(hiltTestBean.toString());
        } else if (R.id.button_multi_impl == view.getId()) {
            newHiltUserService.onLogin("李四", "000000");
        } else if (R.id.button_context_impl == view.getId()) {
            contextHiltUserService.onLogin("王五", "666666");
        } else if (R.id.button_entryPoint == view.getId()) {
            UserServiceEntryPoint userServiceEntryPoint = EntryPointAccessors.fromApplication(view.getContext(), UserServiceEntryPoint.class);
            if (userServiceEntryPoint == null) {
                System.out.println("myEntryPoint为空");
            } else {
                userServiceEntryPoint.getHiltUserServiceImpl().onLogin("赵六", "888888");
            }
        }
    }
}
