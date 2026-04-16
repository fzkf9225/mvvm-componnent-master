package io.coderf.arklab.demo.api;

import io.coderf.arklab.ui.api.FileApiService;
import io.coderf.arklab.ui.api.MediaUploadConfig;
import io.coderf.arklab.ui.helper.CalendarDataSource;
import io.coderf.arklab.demo.BuildConfig;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;
import io.reactivex.rxjava3.disposables.Disposable;
import io.coderf.arklab.common.api.BaseApplication;
import io.coderf.arklab.common.api.Config;
import io.coderf.arklab.common.api.DefaultActivityLifecycleCallback;
import io.coderf.arklab.common.inter.ErrorService;
import io.coderf.arklab.common.utils.log.LogUtil;

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
        Config.getInstance().setResponseBodyLogConverterJson(true);
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
