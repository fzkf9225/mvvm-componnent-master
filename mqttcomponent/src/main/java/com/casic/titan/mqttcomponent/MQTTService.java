package com.casic.titan.mqttcomponent;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;


import com.jeremyliao.liveeventbus.LiveEventBus;

import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * created by fz on 2023/4/18 9:35
 * describe:mqtt服务
 */
public class MQTTService extends Service implements org.eclipse.paho.mqttv5.client.MqttCallback {
    public static final String TAG = MQTTService.class.getSimpleName();
    public String clientId = "mqtt_android" + new Random().nextLong();
    /**
     * 订阅的主题
     */
    public static MqttClient mqttClient;
    private String[] topic;

    private final MutableLiveData<MQTTException> mqttExceptionMutableLiveData = new MutableLiveData<>();

    private final MutableLiveData<Boolean> stateMutableLiveData = new MutableLiveData<>();

    private final List<Observer<ServerMessage>> observers = new ArrayList<>();

    public MQTTService() {

    }

    public void addObserver(Observer<ServerMessage> observer) {
        observers.add(observer);
    }

    public void removeObserver(Observer<ServerMessage> observer) {
        observers.remove(observer);
    }

    public void notifyObservers(ServerMessage serverMessage) {
        if (observers.isEmpty()) {
            return;
        }
        for (Observer<ServerMessage> observer : observers) {
            observer.onChanged(serverMessage);
        }
    }

    private final IBinder iBinder = new MQTTServiceBinder();

    @Override
    public void disconnected(MqttDisconnectResponse disconnectResponse) {
        Log.i(TAG, "MqttDisconnectResponse:" + disconnectResponse);
        stateMutableLiveData.postValue(false);
        if (mqttCallback != null) {
            mqttCallback.disConnection();
        }
    }

    @Override
    public void mqttErrorOccurred(MqttException exception) {
        Log.i(TAG, "exception:" + exception);
        if (mqttCallback != null) {
            mqttCallback.mqttException(new MQTTException(exception.getMessage(), exception.getCause(), exception.getReasonCode()));
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String msg = new String(message.getPayload());
        Log.i(TAG, "topic：" + topic);
        Log.i(TAG, "messageArrived:" + msg);
        //观察者模式
        notifyObservers(new ServerMessage(topic, msg));
        //LiveEventBus直接推送消息
        LiveEventBus.get(MQTTService.class.getSimpleName()).post(new ServerMessage(topic, msg));
    }

    @Override
    public void deliveryComplete(IMqttToken token) {
        Log.i(TAG, "消息成功发送");
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        Log.i(TAG, "连接完成:" + reconnect);
        Log.i(TAG, "连接mqtt服务器成功");
        stateMutableLiveData.postValue(true);
        if (mqttCallback != null) {
            mqttCallback.onConnectedSuccess(reconnect);
        }
        if (topic == null) {
            return;
        }
        if (!reconnect) {
            try {
                subscribe(topic);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void authPacketArrived(int reasonCode, MqttProperties properties) {
        Log.i(TAG, "authPacketArrived:" + properties);
    }

    public class MQTTServiceBinder extends Binder {
        public MQTTService getService() {
            return MQTTService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    public MqttCallback mqttCallback;

    public MqttCallback getPushCallback() {
        return mqttCallback;
    }


    public MqttClient getMqttClient() {
        return mqttClient;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        clientId = HelperUtil.createMqttClientId(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String channelId = PropertiesUtil.getInstance().getProperties(getApplicationContext()).getPropertyValue(Constants.MQTT_CHANNEL_ID, Constants.CHANNEL_ID);
        String channelName = PropertiesUtil.getInstance().getProperties(getApplicationContext()).getPropertyValue(Constants.MQTT_CHANNEL_NAME, Constants.CHANNEL_NAME);
        String notifyId = PropertiesUtil.getInstance().getProperties(getApplicationContext()).getPropertyValue(Constants.MQTT_NOTIFY_ID, String.valueOf(Constants.NOTIFY_ID));
        //设置service为前台服务，提高优先级
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Android8.0以上app启动后通知栏会出现一条"正在运行"的通知
            NotificationChannel channel = new NotificationChannel(channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Notification notification = new Notification.Builder(getApplicationContext(), channelId).build();
                startForeground(Integer.parseInt(notifyId), notification);
            }
        }

        return START_STICKY;
    }

    /**
     * 在mqtt中用QoS来标识服务质量
     * QoS=0时，报文最多发送一次，有可能丢失
     * QoS=1时，报文至少发送一次，有可能重复
     * QoS=2时，报文只发送一次，并且确保消息只到达一次。
     *
     * @param topic mqtt客户端订阅主题
     * @throws MqttException
     */
    public void subscribe(String[] topic) throws MqttException {
        mqttClient.subscribe(topic, new int[]{2});
    }

    public void connect(String[] topic, MqttCallback mqttCallback) {
        try {
            this.mqttCallback = mqttCallback;
            this.topic = topic;
            //第三个参数代表持久化客户端，如果为null，则不持久化
            mqttClient = new MqttClient(CloudDataHelper.getAddress(), clientId, new MemoryPersistence());
            //mqtt连接配置
            MqttConnectionOptions mqttOptions = new MqttConnectionOptions();
            mqttOptions.setCleanStart(true);
            mqttOptions.setUserName(CloudDataHelper.getUserName());
            mqttOptions.setPassword(CloudDataHelper.getPassword().getBytes());
            mqttOptions.setAutomaticReconnect(true);
            mqttOptions.setConnectionTimeout(30);
            mqttOptions.setKeepAliveInterval(20);
            mqttClient.setCallback(this);
            Log.i(TAG, "onStartCommand: before connect");
            //客户端下线，其它客户端或者自己再次上线可以接收"遗嘱"消息
//            MqttTopic topic1 = mqttClient.getTopic(TOPIC);
//            mqttOptions.setWill(topic1, "close".getBytes(), 2, true);
            mqttClient.connect(mqttOptions);
            Log.i(TAG, "onStartCommand: after connect");
        } catch (MqttException e) {
            e.printStackTrace();
            Log.i(TAG, "异常：" + e.getMessage());
            MQTTException mqttException = new MQTTException(e.getMessage(), e.getCause(), e.getReasonCode());
            mqttExceptionMutableLiveData.postValue(mqttException);
            if (mqttCallback != null) {
                mqttCallback.mqttException(mqttException);
            }
        }
    }

    public MutableLiveData<Boolean> getStateMutableLiveData() {
        return stateMutableLiveData;
    }

    public MutableLiveData<MQTTException> getMqttExceptionMutableLiveData() {
        return mqttExceptionMutableLiveData;
    }

    public boolean publish(MqttMessage message) {
        if (mqttClient == null) {
            Log.i(TAG, "发送消息时mqttClient为空:");
            return false;
        }
        if (!mqttClient.isConnected()) {
            Log.i(TAG, "发送消息时mqttClient未连接:");
            return false;
        }
        try {
            mqttClient.publish("location", message);
            return true;
        } catch (MqttException e) {
            Log.i(TAG, "发送消息异常:" + e);
            e.printStackTrace();
        }
        return false;
    }

    public boolean publish(String topic, MqttMessage message) {
        if (mqttClient == null) {
            Log.i(TAG, "发送消息时mqttClient为空:");
            return false;
        }
        if (!mqttClient.isConnected()) {
            Log.i(TAG, "发送消息时mqttClient未连接:");
            return false;
        }
        try {
            mqttClient.publish(topic, message);
            return true;
        } catch (MqttException e) {
            Log.i(TAG, "发送消息异常:" + e);
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onDestroy() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_DETACH);
                stopSelf();
            }
            if (mqttClient == null) {
                return;
            }
            mqttClient.disconnect();
            if (topic == null) {
                return;
            }
            mqttClient.unsubscribe(topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


}
