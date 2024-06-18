package com.casic.titan.demo.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.annotation.NonNull;

import com.casic.titan.googlegps.GpsLoggingService;

import pers.fz.mvvm.base.BaseView;
import pers.fz.mvvm.base.BaseViewModel;
import pers.fz.mvvm.repository.RepositoryImpl;
import pers.fz.mvvm.util.log.LogUtil;

/**
 * Created by fz on 2024/3/26 10:56
 * describe :
 */
public class GoogleGpsViewModel extends BaseViewModel<RepositoryImpl, BaseView> {
    /**
     * gps的Intent
     */
    private Intent gpsServiceIntent;

    @SuppressLint("StaticFieldLeak")
    private GpsLoggingService gpsLoggingService;
    /**
     * gps连接绑定
     */
    public final GPSServiceConnection gpsServiceConnection = new GPSServiceConnection();

    public GpsLoggingService getGpsLoggingService() {
        return gpsLoggingService;
    }

    public void setGpsLoggingService(GpsLoggingService gpsLoggingService) {
        this.gpsLoggingService = gpsLoggingService;
    }

    public GoogleGpsViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected RepositoryImpl createRepository() {
        return null;
    }

    public Intent getGpsServiceIntent(Context context) {
        if (gpsServiceIntent == null) {
            gpsServiceIntent = new Intent(context, GpsLoggingService.class);
        }
        return gpsServiceIntent;
    }

    /**
     * gpsLogger服务
     */
    private class GPSServiceConnection implements ServiceConnection {
        private boolean isStartConnect = false;

        public GPSServiceConnection() {
        }

        public void setStartConnect(boolean startConnect) {
            isStartConnect = startConnect;
        }

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            LogUtil.show(TAG, "------------GpsLoggingService已连接-------------");
            gpsLoggingService = ((GpsLoggingService.GpsLoggingBinder) iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            LogUtil.show(TAG, "------------GpsLoggingService断开连接-------------");
            gpsLoggingService = null;
        }
    }

}
