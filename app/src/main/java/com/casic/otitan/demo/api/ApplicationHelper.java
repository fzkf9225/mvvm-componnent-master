package com.casic.otitan.demo.api;

import com.casic.otitan.commonui.api.FileApiService;
import com.casic.otitan.commonui.api.MediaUploadConfig;
import com.casic.otitan.commonui.helper.CalendarDataSource;
import com.casic.otitan.demo.BuildConfig;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;
import io.reactivex.rxjava3.disposables.Disposable;
import com.casic.otitan.common.api.BaseApplication;
import com.casic.otitan.common.api.Config;
import com.casic.otitan.common.api.DefaultActivityLifecycleCallback;
import com.casic.otitan.common.inter.ErrorService;
import com.casic.otitan.common.utils.log.LogUtil;

/**
 * Created by fz on 2023/5/7 15:03
 * describe:
 */
@HiltAndroidApp
public class ApplicationHelper extends BaseApplication {
    @Inject
    ErrorService errorService;
    @Inject
    FileApiService fileApiService;
    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new DefaultActivityLifecycleCallback(errorService));
        Config.getInstance().init(this);
        Config.getInstance().setResponsePrintJson(true);
        if (BuildConfig.LOG_DEBUG) {
            Config.getInstance().enableDebug(true);
        }
        Disposable disposable = CalendarDataSource.observableCalendarData()
                .toList()
                .subscribe(CalendarDataSource.calendarObservableField::set,
                        throwable -> LogUtil.show("CalendarView", "日历异常：" + throwable)
                );
        MediaUploadConfig.getInstance()
                .setFileApiService(fileApiService)
                .setUploadUrl("minioc/upload");
    }

}
