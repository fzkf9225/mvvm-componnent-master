package com.casic.titan.mqttcomponent;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

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
 * created by fz on 2023/11/22 13:30
 * describe:mqtt服务
 */
public class MQTTServer implements org.eclipse.paho.mqttv5.client.MqttCallback {
    public static final String TAG = MQTTServer.class.getSimpleName();
    public String clientId = "mqtt_android" + new Random().nextLong();
    private volatile static MQTTServer instance = null;
    /**
     * 订阅的主题
     */
    public static MqttClient mqttClient;
    private String[] topic;

    private final MutableLiveData<MQTTException> mqttExceptionMutableLiveData = new MutableLiveData<>();

    private final MutableLiveData<Boolean> stateMutableLiveData = new MutableLiveData<>();

    private final List<Observer<ServerMessage>> observers = new ArrayList<>();

    private MQTTServer(Context context) {
        clientId = HelperUtil.createMqttClientId(context);
    }

    public static MQTTServer getInstance(Context context) {
        if (instance == null) {
            synchronized (MQTTServer.class) {
                if (instance == null) {
                    instance = new MQTTServer(context);
                }
            }
        }
        return instance;
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

    @Override
    public void disconnected(MqttDisconnectResponse disconnectResponse) {
        LogUtil.show(TAG, "MqttDisconnectResponse:" + disconnectResponse);
        stateMutableLiveData.postValue(false);
        if (mqttCallback != null) {
            mqttCallback.disConnection();
        }
    }

    @Override
    public void mqttErrorOccurred(MqttException exception) {
        LogUtil.show(TAG, "exception:" + exception);
        if (mqttCallback != null) {
            mqttCallback.mqttException(new MQTTException(exception.getMessage(), exception.getCause(), exception.getReasonCode()));
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String msg = new String(message.getPayload());
        LogUtil.show(TAG, "topic：" + topic);
        LogUtil.show(TAG, "messageArrived:" + msg);
        //观察者模式
        notifyObservers(new ServerMessage(topic, msg));
    }

    @Override
    public void deliveryComplete(IMqttToken token) {
        LogUtil.show(TAG, "消息成功发送");
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        LogUtil.show(TAG, "连接完成:" + reconnect);
        LogUtil.show(TAG, "连接mqtt服务器成功");
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
        LogUtil.show(TAG, "authPacketArrived:" + properties);
    }

    public MqttCallback mqttCallback;

    public MqttCallback getPushCallback() {
        return mqttCallback;
    }

    public MqttClient getMqttClient() {
        return mqttClient;
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
            if (isConnected()) {
                this.mqttCallback.onConnectedSuccess(false);
                return;
            }
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
            LogUtil.show(TAG, "onStartCommand: before connect");
            //客户端下线，其它客户端或者自己再次上线可以接收"遗嘱"消息
//            MqttTopic topic1 = mqttClient.getTopic(TOPIC);
//            mqttOptions.setWill(topic1, "close".getBytes(), 2, true);
            mqttClient.connect(mqttOptions);
            LogUtil.show(TAG, "onStartCommand: after connect");
        } catch (MqttException e) {
            e.printStackTrace();
            LogUtil.show(TAG, "异常：" + e.getMessage());
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

    /**
     * mqtt服务是否连接
     *
     * @return true:连接
     */
    public boolean isConnected() {
        if (instance == null) {
            return false;
        }
        if (getMqttClient() == null) {
            return false;
        }
        return getMqttClient().isConnected();
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
            getMqttClient().publish(topic, message);
            return true;
        } catch (MqttException e) {
            LogUtil.show(TAG, "发送消息异常:" + e);
            e.printStackTrace();
        }
        return false;
    }

    public boolean publish(MqttMessage message) {
        if (mqttClient == null) {
            LogUtil.show(TAG, "发送消息时mqttClient为空:");
            return false;
        }
        if (!mqttClient.isConnected()) {
            LogUtil.show(TAG, "发送消息时mqttClient未连接:");
            return false;
        }
        try {
            mqttClient.publish("location", message);
            return true;
        } catch (MqttException e) {
            LogUtil.show(TAG, "发送消息异常:" + e);
            e.printStackTrace();
        }
        return false;
    }

    public boolean publish(String topic, MqttMessage message) {
        if (mqttClient == null) {
            LogUtil.show(TAG, "发送消息时mqttClient为空:");
            return false;
        }
        if (!mqttClient.isConnected()) {
            LogUtil.show(TAG, "发送消息时mqttClient未连接:");
            return false;
        }
        try {
            mqttClient.publish(topic, message);
            return true;
        } catch (MqttException e) {
            LogUtil.show(TAG, "发送消息异常:" + e);
            e.printStackTrace();
        }
        return false;
    }

    public void onStop() {
        try {
            if (getMqttClient() != null) {
                if (topic != null) {
                    getMqttClient().unsubscribe(topic);
                }
                getMqttClient().disconnect();
                getMqttClient().close();
            }
            instance = null;
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


}
