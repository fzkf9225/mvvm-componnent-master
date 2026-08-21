package io.coderf.arklab.demo.api;

import android.app.NotificationManager;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;
import io.coderf.arklab.common.api.BaseApplication;
import io.coderf.arklab.common.api.Config;
import io.coderf.arklab.common.api.DefaultActivityLifecycleCallback;
import io.coderf.arklab.common.inter.ErrorService;
import io.coderf.arklab.common.utils.log.LogUtil;
import io.coderf.arklab.demo.BuildConfig;
import io.coderf.arklab.googlegps.common.GpsSettingConfig;
import io.coderf.arklab.googlegps.utils.DebugUtil;
import io.coderf.arklab.log.ArkLog;
import io.coderf.arklab.log.FileLogLevel;
import io.coderf.arklab.log.LogConfig;
import io.coderf.arklab.mqtt.Mqtt;
import io.coderf.arklab.ui.api.FileApiService;
import io.coderf.arklab.ui.api.MediaUploadConfig;
import io.coderf.arklab.ui.helper.CalendarDataSource;
import io.reactivex.rxjava3.disposables.Disposable;

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
        GpsSettingConfig.getInstance().init(this);

        GpsSettingConfig.getInstance()
                .setNotificationChannelId(getApplicationContext().getPackageName() + ".GPSService")
                .setNotificationChannelName("位置服务")
                .setNotificationImportance(NotificationManager.IMPORTANCE_HIGH)
                .setNotificationEnableLights(true)
                .setNotificationShowBadge(true)
                .setMinTimeInterval(1000)
                .setMinDistanceInterval(0)
                .setFilterLargeJump(false)
                .setMinAccuracy(100f)
                .setFilterStaleLocation(false);

        Config.getInstance().setResponseBodyLogConverterJson(true);
        Config.getInstance().setFolderName("arklab");

        ArkLog.init(this, LogConfig.builder()
                .showThreadInfo(true)
                .globalTag("ArkLab")
                .fileLogLevel(BuildConfig.LOG_DEBUG ? FileLogLevel.DEBUG : FileLogLevel.NONE)
                .build());

        if (BuildConfig.LOG_DEBUG) {
            // 各模块只开自己的控制台开关（落盘已由 ArkLog.init 统一配置）
            Config.getInstance().enableDebug(true, FileLogLevel.NONE);
            DebugUtil.enableDebug(this, true);
            io.coderf.arklab.media.utils.DebugUtil.enableDebug(this, true);
            Mqtt.enableDebug(this, true);
        }
        Disposable disposable = CalendarDataSource.observableCalendarData()
                .toList()
                .subscribe(CalendarDataSource.calendarObservableField::set,
                        throwable -> LogUtil.logger("CalendarView", "日历异常：" + throwable)
                );
        MediaUploadConfig.getInstance()
                .setFileApiService(fileApiService)
                .setUploadUrl("minioc/upload");
    }

}
