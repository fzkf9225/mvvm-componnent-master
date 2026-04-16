package io.coderf.arklab.demo.viewmodel;

import android.app.Application;
import android.view.View;

import androidx.annotation.NonNull;

import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.bean.HiltTestBean;
import io.coderf.arklab.demo.impl.UserServiceEntryPoint;
import io.coderf.arklab.demo.inter.HiltUserService;
import io.coderf.arklab.demo.module.HiltUserServiceModule;

import javax.inject.Inject;

import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.android.lifecycle.HiltViewModel;
import io.coderf.arklab.common.base.BaseRepository;
import io.coderf.arklab.common.base.BaseView;
import io.coderf.arklab.common.base.BaseViewModel;
import io.coderf.arklab.common.inter.RetryService;
import io.coderf.arklab.common.repository.RepositoryImpl;

/**
 * Created by fz on 2024/5/31 11:31
 * describe :
 */
@HiltViewModel
public class TestHiltViewModel extends BaseViewModel<BaseRepository<BaseView>, BaseView> {
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
    public TestHiltViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected RepositoryImpl createRepository() {
        return null;
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
