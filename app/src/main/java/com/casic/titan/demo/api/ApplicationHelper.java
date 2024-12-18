package com.casic.titan.demo.api;

import com.casic.titan.commonui.helper.CalendarDataSource;
import com.casic.titan.demo.BuildConfig;
import com.casic.titan.googlegps.common.AppSettings;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;
import io.reactivex.rxjava3.disposables.Disposable;
import pers.fz.mvvm.api.DefaultActivityLifecycleCallback;
import pers.fz.mvvm.api.BaseApplication;
import pers.fz.mvvm.api.Config;
import pers.fz.mvvm.inter.ErrorService;
import pers.fz.mvvm.util.log.LogUtil;

/**
 * Created by fz on 2023/5/7 15:03
 * describe:
 */
@HiltAndroidApp
public class ApplicationHelper extends BaseApplication {
    @Inject
    ErrorService errorService;

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new DefaultActivityLifecycleCallback(errorService));
        Config.getInstance().init(this);
        if (BuildConfig.LOG_DEBUG) {
            Config.getInstance().enableDebug(true);
        }
        AppSettings.getInstance().onCreate(this);
        Disposable disposable = CalendarDataSource.observableCalendarData()
                .toList()
                .subscribe(CalendarDataSource.calendarObservableField::set,
                        throwable -> LogUtil.show("CalendarView", "日历异常：" + throwable)
                );
    }

}
