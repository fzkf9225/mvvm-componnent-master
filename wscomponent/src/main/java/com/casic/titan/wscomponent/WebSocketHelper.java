package com.casic.titan.wscomponent;

import static android.content.Context.BIND_AUTO_CREATE;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.List;

/**
 * Created by fz on 2023/5/10 11:41
 * describe :WebSocket连接帮助类
 */
public class WebSocketHelper {
    private volatile static WebSocketHelper webSocketHelper;
    private static Application application;
    /**
     * 回调方法
     */
    private Callback<?> callback;
    /**
     * websocket地址
     */
    private String webSocketUrl;
    /**
     * 用户id
     */
    private String userId;

    /**
     * 单例对象初始化
     *
     * @param application Application视图
     */
    public static void init(Application application) {
        WebSocketHelper.application = application;
        if (webSocketHelper == null) {
            synchronized (WebSocketHelper.class) {
                if (webSocketHelper == null) {
                    webSocketHelper = new WebSocketHelper();
                }
            }
        }
    }

    /**
     * 获取单例对象,必须先调用init
     *
     * @return 单例对象
     */
    public static WebSocketHelper getInstance() {
        return webSocketHelper;
    }

    /**
     * WebSocketService绑定对象
     */
    private static WebSocketService.WebSocketBinder pushBinder;

    public WebSocketService.WebSocketBinder getPushBinder() {
        return pushBinder;
    }

    public WebSocketService getWebSocketService() {
        if (pushBinder == null) {
            return null;
        }
        return pushBinder.getService();
    }

    public boolean isConnected() {
        if (getWebSocketService() == null) {
            return false;
        }
        return getWebSocketService().getSocketChannel() != null && getWebSocketService().getSocketChannel().isActive();
    }

    public void startConnection(String webSocketUrl, String userId, Callback<?> callback) {
        this.callback = callback;
        this.webSocketUrl = webSocketUrl;
        this.userId = userId;
        //如果服务Service和绑定binder为空的话，重启service服务
        if (getWebSocketService() == null) {
            Intent bindIntent = new Intent(application, WebSocketService.class);
            application.bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                application.startForegroundService(bindIntent);
            } else {
                application.startService(bindIntent);
            }
        } else {
            //如果SocketChannel为空的话说明Websocket连接断开了，或者连接未成功
            if (getWebSocketService().getSocketChannel() == null || !getWebSocketService().getSocketChannel().isActive()) {
                pushBinder.onStartConnect(webSocketUrl, userId, this.callback);
                return;
            }
            //防止重复连接，和重复连接后没有回调消息
            if (callback != null) {
                callback.onEvent(ResponseCode.SOCKET_SUCCESS, "通道已连接,请勿重复连接！", null);
            }
        }
    }

    /**
     * 退出WebSocket
     */
    public void stop() throws Exception {
        try {
            //可能未有异常，因此直接捕获，但不能影响StopService
            if (getWebSocketService() != null && getWebSocketService().getSocketChannel() != null) {
                getWebSocketService().getSocketChannel().shutdown();
                getWebSocketService().getSocketChannel().close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        pushBinder = null;
        if(!isServiceRunning(application,WebSocketService.class)){
            return;
        }
        Intent bindIntent = new Intent(application, WebSocketService.class);
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

    public MutableLiveData<WebSocketException> getExceptionMutableLiveData() {
        if (getWebSocketService() == null) {
            return null;
        }
        return getWebSocketService().getExceptionMutableLiveData();
    }

    /**
     * 发送订阅信息
     *
     * @param sn                  sn号
     * @param sendMessageListener 发送是否成功
     */
    public void subscribe(String sn, SendMessageListener sendMessageListener) {
        if (getPushBinder() == null || getWebSocketService() == null || getWebSocketService().getSocketChannel() == null
                || !getWebSocketService().getSocketChannel().isActive()) {
            if (sendMessageListener != null) {
                sendMessageListener.sendResult(ResponseCode.SEND_MESSAGE_FAILURE, "连接已断开");
            }
            return;
        }
        getPushBinder().subscribe(sn, sendMessageListener);
    }

    /**
     * 全局的ServiceContention
     */
    ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            pushBinder = (WebSocketService.WebSocketBinder) service;
            pushBinder.onStartConnect(webSocketUrl, userId, callback);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            pushBinder = null;
        }
    };

    public void addObserver(Observer<WebSocketEvent<String>> observer) {
        if (getWebSocketService() == null) {
            return;
        }
        getWebSocketService().addObserver(observer);
    }

    public void removeObserver(Observer<WebSocketEvent<String>> observer) {
        if (getWebSocketService() == null) {
            return;
        }
        getWebSocketService().removeObserver(observer);
    }
}
