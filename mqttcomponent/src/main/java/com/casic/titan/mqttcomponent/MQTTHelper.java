package com.casic.titan.mqttcomponent;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import org.eclipse.paho.mqttv5.client.IMqttMessageListener;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by fz on 2023/4/27 15:34
 * describe :
 */
public class MQTTHelper {
    private final static String TAG = MQTTHelper.class.getSimpleName();
    private volatile static MQTTHelper mqttHelper;
    private static Application application;

    private MQTTService mqttService;
    private MqttCallback mqttCallback;
    private String[] topic;

    private MQTTHelper() {

    }

    public static MQTTHelper init(Application application) {
        MQTTHelper.application = application;
        if (mqttHelper == null) {
            synchronized (MQTTHelper.class) {
                if (mqttHelper == null) {
                    mqttHelper = new MQTTHelper();
                }
            }
        }
        return mqttHelper;
    }

    public static MQTTHelper getInstance() {
        return mqttHelper;
    }

    public void startService(String[] topic, MqttCallback mqttCallback) {
        this.topic = topic;
        this.mqttCallback = mqttCallback;
        //如果已经连接成功，那么就直接返回成功！
        if (isConnected()) {
            this.mqttCallback.onConnectedSuccess(false);
            return;
        }
        //如果service没有启动的话则启动service
        if (getMqttService() == null) {
            Intent bindIntent = new Intent(application, MQTTService.class);
            application.bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                application.startForegroundService(bindIntent);
            } else {
                application.startService(bindIntent);
            }
            return;
        }
        //当service启动了但是连接没成功则启动连接
        getMqttService().connect(topic, mqttCallback);
    }

    /**
     * 退出mqtt
     */
    public void stop() throws Exception {
        try {
            //此处可能会有异常，因此直接捕获一下，但是不能印象StopService
            if (getMqttService() != null && getMqttService().getMqttClient() != null) {
                getMqttService().getMqttClient().unsubscribe(topic);
                getMqttService().getMqttClient().disconnect();
                getMqttService().getMqttClient().close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!isServiceRunning(application, MQTTService.class)) {
            return;
        }
        Intent bindIntent = new Intent(application, MQTTService.class);
        application.unbindService(serviceConnection);
        application.stopService(bindIntent);
    }

    public static boolean isServiceRunning(Context mContext, Class<?> clx) {
        if (clx == null) {
            return false;
        }
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo serviceInfo : runningServices) {
            ComponentName componentName = serviceInfo.service;
            if (componentName.getClassName().equals(clx.getName()) && componentName.getPackageName().equals(mContext.getPackageName())) {
                // Service已经注册和启动
                return true;
            }
        }
        // 执行相应的操作
        return false;
    }

    public MQTTService getMqttService() {
        return mqttService;
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i(TAG, "------------ServiceConnection-------------");
            mqttService = ((MQTTService.MQTTServiceBinder) iBinder).getService();
            mqttService.connect(topic, mqttCallback);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(TAG, "------------onServiceDisconnected-------------");
            mqttService = null;
        }
    };

    /**
     * mqtt服务是否连接
     *
     * @return true:连接
     */
    public boolean isConnected() {
        if (mqttService == null) {
            return false;
        }
        if (mqttService.getMqttClient() == null) {
            return false;
        }
        return mqttService.getMqttClient().isConnected();
    }

    /**
     * 发布信息
     *
     * @param topic 主题
     * @param msg   消息内容
     * @return 是否成功
     */
    public boolean publish(String topic, String msg) {
        if (!isConnected()) {
            return false;
        }
        MqttMessage message = new MqttMessage();
        message.setRetained(true);
        message.setPayload(msg.getBytes());
        try {
            mqttService.getMqttClient().publish(topic, message);
            return true;
        } catch (MqttException e) {
            Log.i(TAG, "发送消息异常:" + e);
            e.printStackTrace();
        }
        return false;
    }

    public void addObserver(Observer<ServerMessage> observer) {
        if (getMqttService() == null) {
            return;
        }
        getMqttService().addObserver(observer);
    }

    public void removeObserver(Observer<ServerMessage> observer) {
        if (getMqttService() == null) {
            return;
        }
        getMqttService().removeObserver(observer);
    }
}
