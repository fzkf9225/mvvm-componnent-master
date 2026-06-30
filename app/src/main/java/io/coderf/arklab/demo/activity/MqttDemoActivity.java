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
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.databinding.ActivityMqttDemoBinding;
import io.coderf.arklab.mqttcomponent.mqtt.AbstractMqttConnectionListener;
import io.coderf.arklab.mqttcomponent.mqtt.MqttConnection;
import io.coderf.arklab.mqttcomponent.mqtt.MqttConnectionConfig;
import io.coderf.arklab.mqttcomponent.presence.HeartbeatAction;
import io.coderf.arklab.mqttcomponent.presence.HeartbeatScheduler;
import io.coderf.arklab.mqttcomponent.presence.PresenceConnectionInfo;
import io.coderf.arklab.mqttcomponent.presence.PresenceConnectionListener;
import io.coderf.arklab.mqttcomponent.presence.PresenceMqttClient;
import io.coderf.arklab.mqttcomponent.widget.MqttReconnectDialog;

/**
 * MQTT 组件演示页（Java 接入示例）。
 * <p>
 * 演示内容：
 * <ul>
 *     <li>{@link MqttConnection} + {@link MqttConnectionConfig.Builder} 建立连接、订阅、发布</li>
 *     <li>{@link AbstractMqttConnectionListener} 接收连接与下行消息</li>
 *     <li>意外断连后的自定义自动重连（{@code maxReconnectAttempts} / {@code reconnectIntervalSeconds}）</li>
 *     <li>{@link MqttReconnectDialog} 展示重连进度，支持「直接退出」</li>
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

    @Nullable
    private MqttReconnectDialog reconnectDialog;

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
        appendLog("连接后可关闭网络模拟断连，观察自动重连与弹窗。");

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
     * 配置自定义重连策略后，意外断连将自动重试并在 UI 展示 {@link MqttReconnectDialog}。
     */
    private void connectMqtt() {
        String broker = textOf(binding.editBroker);
        String clientId = textOf(binding.editClientId);
        String username = textOf(binding.editUsername);
        String password = textOf(binding.editPassword);
        String subscribeTopic = textOf(binding.editSubscribeTopic);
        int maxReconnectAttempts = parsePositiveInt(textOf(binding.editMaxReconnectAttempts), 20);
        int reconnectIntervalSeconds = parsePositiveInt(textOf(binding.editReconnectIntervalSeconds), 5);

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
                .maxReconnectAttempts(maxReconnectAttempts)
                .reconnectIntervalSeconds(reconnectIntervalSeconds)
                .resubscribeOnReconnect(true)
                .dispatchConnectOnMainThread(true);

        if (!TextUtils.isEmpty(subscribeTopic)) {
            builder.subscribeTopics(subscribeTopic);
        }

        MqttConnectionConfig config = builder.build();

        if (mqttConnection == null) {
            mqttConnection = new MqttConnection("MqttDemo", (tag, message) -> appendLog("[Paho] " + message));
        }

        dismissReconnectDialog();
        appendLog("正在连接 " + broker + " ...");
        appendLog("重连策略: 最多 " + maxReconnectAttempts + " 次, 间隔 " + reconnectIntervalSeconds + " 秒");
        updateStatus("连接中...");

        mqttConnection.connect(config, new AbstractMqttConnectionListener() {
            @Override
            public void onConnected(boolean reconnect) {
                dismissReconnectDialog();
                appendLog(reconnect ? "自动重连成功" : "连接成功");
                updateStatus("已连接");
            }

            @Override
            public void onDisconnected() {
                appendLog("意外断连，即将自动重连...");
                updateStatus("重连中...");
            }

            @Override
            public void onReconnecting(int attempt, int maxAttempts, int nextRetryDelaySeconds) {
                appendLog("安排第 " + attempt + "/" + maxAttempts + " 次重连，"
                        + nextRetryDelaySeconds + " 秒后尝试");
                showOrUpdateReconnectDialog(attempt, maxAttempts, nextRetryDelaySeconds);
            }

            @Override
            public void onReconnectExhausted() {
                dismissReconnectDialog();
                appendLog("已达最大重连次数，停止重连");
                updateStatus("重连失败");
                showToast("MQTT 重连失败，请检查网络后重新连接");
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
        dismissReconnectDialog();
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

    private void showOrUpdateReconnectDialog(int attempt, int maxAttempts, int nextRetryDelaySeconds) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        if (reconnectDialog == null || !reconnectDialog.isShowing()) {
            reconnectDialog = new MqttReconnectDialog(this)
                    .setOnExitClickListener(() -> {
                        appendLog("用户取消重连，主动断开");
                        disconnectMqtt();
                    })
                    .builder();
            reconnectDialog.show();
        }
        reconnectDialog.updateReconnectState(attempt, maxAttempts, nextRetryDelaySeconds);
    }

    private void dismissReconnectDialog() {
        if (reconnectDialog != null) {
            reconnectDialog.dismiss();
            reconnectDialog = null;
        }
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

    private static int parsePositiveInt(String value, int defaultValue) {
        if (TextUtils.isEmpty(value)) {
            return defaultValue;
        }
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : defaultValue;
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    @Override
    protected void onDestroy() {
        dismissReconnectDialog();
        stopPresenceDemo();
        if (mqttConnection != null) {
            mqttConnection.disconnect();
            mqttConnection = null;
        }
        super.onDestroy();
    }
}
