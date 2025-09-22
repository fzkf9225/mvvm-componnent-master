package com.casic.otitan.demo.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.annotation.NonNull;

import com.casic.otitan.googlegps.service.GpsService;

import com.casic.otitan.common.base.BaseRepository;
import com.casic.otitan.common.base.BaseView;
import com.casic.otitan.common.base.BaseViewModel;
import com.casic.otitan.common.repository.RepositoryImpl;
import com.casic.otitan.common.utils.log.LogUtil;

/**
 * Created by fz on 2024/3/26 10:56
 * describe :
 */
public class GoogleGpsViewModel extends BaseViewModel<BaseRepository<BaseView>, BaseView> {
    /**
     * gps的Intent
     */
    private Intent gpsServiceIntent;

    @SuppressLint("StaticFieldLeak")
    private GpsService getService;
    /**
     * gps连接绑定
     */
    public final GPSServiceConnection gpsServiceConnection = new GPSServiceConnection();

    public GoogleGpsViewModel(@NonNull Application application) {
        super(application);
    }

    public GpsService getGetService() {
        return getService;
    }

    public void setGetService(GpsService getService) {
        this.getService = getService;
    }

    @Override
    protected RepositoryImpl createRepository() {
        return null;
    }

    public Intent getGpsServiceIntent(Context context) {
        if (gpsServiceIntent == null) {
            gpsServiceIntent = new Intent(context, GpsService.class);
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
            LogUtil.show(TAG, "------------GpsService已连接-------------");
            getService = ((GpsService.GpsBinder) iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            LogUtil.show(TAG, "------------GpsService断开连接-------------");
            getService = null;
        }
    }

}
