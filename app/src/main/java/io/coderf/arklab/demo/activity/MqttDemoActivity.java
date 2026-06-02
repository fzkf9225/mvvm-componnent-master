package io.coderf.arklab.demo.activity;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import org.eclipse.paho.mqttv5.common.MqttException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.base.BaseActivity;
import io.coderf.arklab.common.viewmodel.EmptyViewModel;
import io.coderf.arklab.demo.databinding.ActivityMqttDemoBinding;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.mqttcomponent.mqtt.AbstractMqttConnectionListener;
import io.coderf.arklab.mqttcomponent.mqtt.MqttConnection;
import io.coderf.arklab.mqttcomponent.mqtt.MqttConnectionConfig;
import io.coderf.arklab.mqttcomponent.presence.HeartbeatAction;
import io.coderf.arklab.mqttcomponent.presence.HeartbeatScheduler;
import io.coderf.arklab.mqttcomponent.presence.PresenceConnectionInfo;
import io.coderf.arklab.mqttcomponent.presence.PresenceConnectionListener;
import io.coderf.arklab.mqttcomponent.presence.PresenceMqttClient;

/**
 * MQTT 组件演示页（Java 接入示例）。
 * <p>
 * 演示内容：
 * <ul>
 *     <li>{@link MqttConnection} + {@link MqttConnectionConfig.Builder} 建立连接、订阅、发布</li>
 *     <li>{@link AbstractMqttConnectionListener} 接收连接与下行消息</li>
 *     <li>{@link PresenceMqttClient} + {@link HeartbeatScheduler} 在线心跳（独立通道示例）</li>
 * </ul>
 * <p>
 * 默认 Broker 为 EMQX 公共测试节点，仅供联调；生产环境请替换为服务端下发的连接参数。
 */
@AndroidEntryPoint
public class MqttDemoActivity extends BaseActivity<EmptyViewModel, ActivityMqttDemoBinding> {

    /** 业务推送 / 订阅通道（核心 API 示例） */
    private MqttConnection mqttConnection;

    /** 在线心跳通道（与上方独立实例，互不影响） */
    @Nullable
    private PresenceMqttClient presenceClient;

    @Nullable
    private HeartbeatScheduler heartbeatScheduler;

    private final StringBuilder logBuffer = new StringBuilder();
    private final SimpleDateFormat timeFormat =
            new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    @Override
    protected int getLayoutId() {
        return R.layout.activity_mqtt_demo;
    }

    @Override
    public String setTitleBar() {
        return "MQTT 组件示例";
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        binding.editClientId.setText("android_demo_" + System.currentTimeMillis());
        appendLog("请填写 Broker 与账号，点击「连接」开始演示。");

        binding.btnConnect.setOnClickListener(v -> connectMqtt());
        binding.btnDisconnect.setOnClickListener(v -> disconnectMqtt());
        binding.btnPublish.setOnClickListener(v -> publishMessage());
        binding.btnPresenceHeartbeat.setOnClickListener(v -> demoPresenceHeartbeat());
        binding.btnClearLog.setOnClickListener(v -> {
            logBuffer.setLength(0);
            binding.tvLog.setText("");
        });
    }

    @Override
    public void initData(Bundle bundle) {
        // 本页无额外数据加载
    }

    /**
     * 核心层示例：Java 通过 Builder 组装 {@link MqttConnectionConfig} 并连接。
     */
    private void connectMqtt() {
        String broker = textOf(binding.editBroker);
        String clientId = textOf(binding.editClientId);
        String username = textOf(binding.editUsername);
        String password = textOf(binding.editPassword);
        String subscribeTopic = textOf(binding.editSubscribeTopic);

        if (TextUtils.isEmpty(broker) || TextUtils.isEmpty(clientId)
                || TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            showToast("Broker、ClientId、用户名、密码不能为空");
            return;
        }

        MqttConnectionConfig.Builder builder = MqttConnectionConfig.builder()
                .brokerAddress(broker)
                .clientId(clientId)
                .username(username)
                .password(password)
                .keepAliveSeconds(60)
                .automaticReconnect(true)
                .resubscribeOnReconnect(true)
                .dispatchConnectOnMainThread(true);

        if (!TextUtils.isEmpty(subscribeTopic)) {
            builder.subscribeTopics(subscribeTopic);
        }

        MqttConnectionConfig config = builder.build();

        if (mqttConnection == null) {
            mqttConnection = new MqttConnection("MqttDemo", (tag, message) -> appendLog("[Paho] " + message));
        }

        appendLog("正在连接 " + broker + " ...");
        updateStatus("连接中...");

        mqttConnection.connect(config, new AbstractMqttConnectionListener() {
            @Override
            public void onConnected(boolean reconnect) {
                appendLog(reconnect ? "自动重连成功" : "连接成功");
                updateStatus("已连接");
            }

            @Override
            public void onDisconnected() {
                appendLog("连接已断开");
                updateStatus("已断开");
            }

            @Override
            public void onError(@Nullable MqttException exception) {
                appendLog("MQTT 错误: " + (exception != null ? exception.getMessage() : "unknown"));
                updateStatus("错误");
            }

            @Override
            public void onMessage(String topic, String payload) {
                appendLog("收到消息 [" + topic + "]: " + payload);
            }

            @Override
            public void onDeliveryComplete() {
                appendLog("消息投递完成");
            }
        });
    }

    private void disconnectMqtt() {
        stopPresenceDemo();
        if (mqttConnection != null) {
            mqttConnection.disconnect();
            appendLog("已主动断开连接");
        }
        updateStatus("未连接");
    }

    private void publishMessage() {
        if (mqttConnection == null || !mqttConnection.isConnected()) {
            showToast("请先连接 MQTT");
            return;
        }
        String topic = textOf(binding.editPublishTopic);
        String payload = textOf(binding.editPublishMessage);
        if (TextUtils.isEmpty(topic) || TextUtils.isEmpty(payload)) {
            showToast("发布主题与消息不能为空");
            return;
        }
        boolean ok = mqttConnection.publish(topic, payload, 1, false);
        appendLog(ok ? "发布成功 -> " + topic : "发布失败");
    }

    /**
     * Presence 层示例：独立 {@link PresenceMqttClient} 建连并启动应用层心跳。
     * <p>
     * 与上方 {@link #mqttConnection} 使用不同 clientId，可同时在线。
     */
    private void demoPresenceHeartbeat() {
        String broker = textOf(binding.editBroker);
        String username = textOf(binding.editUsername);
        String password = textOf(binding.editPassword);
        if (TextUtils.isEmpty(broker) || TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            showToast("请先填写 Broker 与账号");
            return;
        }

        stopPresenceDemo();

        String presenceClientId = textOf(binding.editClientId) + "_presence";
        String heartbeatTopic = textOf(binding.editPublishTopic) + "/heartbeat";

        PresenceConnectionInfo info = new PresenceConnectionInfo(
                broker,
                presenceClientId,
                username,
                password,
                60,
                heartbeatTopic + "/lwt",
                "{\"online\":false}",
                heartbeatTopic
        );

        presenceClient = new PresenceMqttClient();
        appendLog("Presence 通道连接中 clientId=" + presenceClientId);

        presenceClient.connect(info, new PresenceConnectionListener() {
            @Override
            public void onConnected(boolean reconnect) {
                appendLog("Presence 连接成功 reconnect=" + reconnect);
                startPresenceHeartbeat(info);
            }
        });
    }

    private void startPresenceHeartbeat(PresenceConnectionInfo info) {
        if (presenceClient == null) {
            return;
        }
        heartbeatScheduler = new HeartbeatScheduler();
        HeartbeatAction action = () -> {
            String payload = presenceClient.buildHeartbeatPayload(10001L);
            boolean ok = presenceClient.publishHeartbeat(info.heartbeatTopic, payload);
            runOnUiThread(() -> appendLog(ok
                    ? "Presence 心跳已发送: " + payload
                    : "Presence 心跳发送失败"));
        };
        heartbeatScheduler.start(action);
        action.run();
        appendLog("Presence 心跳调度已启动（30s 间隔）");
    }

    private void stopPresenceDemo() {
        if (heartbeatScheduler != null) {
            heartbeatScheduler.stop();
            heartbeatScheduler = null;
        }
        if (presenceClient != null) {
            presenceClient.disconnect();
            presenceClient = null;
            appendLog("Presence 通道已断开");
        }
    }

    private void updateStatus(String status) {
        binding.tvStatus.setText("状态：" + status);
    }

    private void appendLog(String line) {
        runOnUiThread(() -> {
            logBuffer.append('[')
                    .append(timeFormat.format(new Date()))
                    .append("] ")
                    .append(line)
                    .append('\n');
            binding.tvLog.setText(logBuffer.toString());
        });
    }

    private static String textOf(android.widget.EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    @Override
    protected void onDestroy() {
        stopPresenceDemo();
        if (mqttConnection != null) {
            mqttConnection.disconnect();
            mqttConnection = null;
        }
        super.onDestroy();
    }
}
