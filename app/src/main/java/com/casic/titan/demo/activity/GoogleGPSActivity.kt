package com.casic.titan.demo.activity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;

import com.casic.titan.demo.R;
import com.casic.titan.demo.bean.UseCase;
import com.casic.titan.demo.databinding.ActivityGoogleGpsBinding;
import com.casic.titan.demo.viewmodel.GoogleGpsViewModel;
import com.casic.titan.googlegps.GpsLoggingService;
import com.casic.titan.googlegps.common.Session;
import com.casic.titan.googlegps.common.events.CommandEvents;
import com.casic.titan.googlegps.common.events.ServiceEvents;
import com.casic.titan.googlegps.socket.GPSSocketServer;

import dagger.hilt.android.AndroidEntryPoint;
import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.util.common.DateUtil;
import pers.fz.mvvm.util.log.LogUtil;

@AndroidEntryPoint
public class GoogleGPSActivity extends BaseActivity<GoogleGpsViewModel, ActivityGoogleGpsBinding> {
    private UseCase useCase;
    private final Session session = Session.getInstance();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_google_gps;
    }

    @Override
    public String setTitleBar() {
        return "GPS工具类";
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        binding.setGoogleGPSViewModel(mViewModel);
        binding.startService.setOnClickListener(v -> {
            binding.tvMessage.setText("正在开启service服务");
            ContextCompat.startForegroundService(this, mViewModel.getGpsServiceIntent(this));
            this.bindService(mViewModel.getGpsServiceIntent(this), mViewModel.gpsServiceConnection, Context.BIND_AUTO_CREATE);
            session.setBoundToService(true);
        });
        binding.endService.setOnClickListener(v -> {
            if (mViewModel.getGpsLoggingService() == null) {
                binding.tvMessage.setText("服务已关闭");
                return;
            }
            binding.tvMessage.setText("正在关闭service服务");
            mViewModel.getGpsLoggingService().stopLogging();
            this.unbindService(mViewModel.gpsServiceConnection);
            this.stopService(mViewModel.getGpsServiceIntent(this));
            session.setBoundToService(false);
            mViewModel.setGpsLoggingService(null);
        });
        binding.startServiceOnce.setOnClickListener(v -> {
            if (mViewModel.getGpsLoggingService() == null) {
                binding.tvMessage.setText("请先开启定位服务");
                return;
            }
            binding.tvMessage.setText("正在开启监听，等待结果返回");
            mViewModel.getGpsLoggingService().logOnce();
        });
        binding.startServiceAlways.setOnClickListener(v -> {
            if (mViewModel.getGpsLoggingService() == null) {
                binding.tvMessage.setText("请先开启定位服务");
                return;
            }
            binding.tvMessage.setText("正在开启监听，等待结果返回");
            mViewModel.getGpsLoggingService().logAlways();
        });
        binding.startSocket.setOnClickListener(v -> {
            if (mViewModel.getGpsLoggingService() == null) {
                binding.tvMessage.setText("请先开启定位服务");
                return;
            }
            GPSSocketServer.getInstance().connect((isConnect, errorMsg) -> {
                binding.tvMessage.setText("Socket连接：" + isConnect + "，" + errorMsg);
                LogUtil.show(TAG, "Socket连接：" + isConnect + "，" + errorMsg);
            });
        });
        binding.endSocket.setOnClickListener(v -> {
            if (mViewModel.getGpsLoggingService() == null) {
                binding.tvMessage.setText("请先开启定位服务");
                return;
            }
            GPSSocketServer.getInstance().disconnect((isConnect, errorMsg) -> {
                binding.tvMessage.setText("Socket断开连接：" + isConnect + "，" + errorMsg);
                LogUtil.show(TAG, "Socket断开连接：" + isConnect + "，" + errorMsg);
            });
        });
    }

    @Override
    public void initData(Bundle bundle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            useCase = bundle.getParcelable("args", UseCase.class);
        } else {
            useCase = bundle.getParcelable("args");
        }
        toolbarBind.getToolbarConfig().setTitle(useCase.getName());
        GpsLoggingService.addLocationObserver(observerLocation);
        GpsLoggingService.addStatusObserver(observerStatus);
    }

    Observer<ServiceEvents.LocationUpdate> observerLocation = locationUpdate -> {
        String stringBuilder = "定位信息：" +
                "\n" +
                "时间：" +
                DateUtil.getDateTimeFromMillis(System.currentTimeMillis()) +
                "\n" +
                "经度：" +
                locationUpdate.location.getLongitude() +
                "\n" +
                "纬度：" +
                locationUpdate.location.getLatitude();
        binding.tvMessage.setText(stringBuilder);
    };
    Observer<CommandEvents.RequestStartStop> observerStatus = requestStartStop -> {
        binding.tvMessage.setText("状态：" + requestStartStop.start);
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GPSSocketServer.getInstance().disconnect((isConnect, errorMsg) -> {
            LogUtil.show(TAG, "GPSSocket连接已断开：" + isConnect + "，" + errorMsg);
        });
        try {
            if (session.isBoundToService()) {
                this.unbindService(mViewModel.gpsServiceConnection);
                session.setBoundToService(false);
            }
            if(mViewModel.getGpsLoggingService()!=null){
                mViewModel.getGpsLoggingService().stopLogging();
            }
            LogUtil.show(TAG, "关闭GpsLoggingService服务绑定");
            getApplication().stopService(mViewModel.getGpsServiceIntent(this));
        } catch (Exception e) {
            LogUtil.e(TAG, "停止GpsLoggingService异常：" + e);
        }
    }
}