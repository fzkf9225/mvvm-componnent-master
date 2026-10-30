package io.coderf.arklab.demo.activity;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.base.BaseActivity;
import io.coderf.arklab.common.viewmodel.EmptyViewModel;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.databinding.ActivityWsDemoBinding;
import io.coderf.arklab.ws.AbstractWsListener;
import io.coderf.arklab.ws.Ws;
import io.coderf.arklab.ws.WsClient;
import io.coderf.arklab.ws.WsConfig;
import io.coderf.arklab.ws.WsConnectCallback;
import io.coderf.arklab.ws.WsException;
import io.coderf.arklab.ws.WsSendCallback;
import io.coderf.arklab.ws.presence.HeartbeatScheduler;
import io.coderf.arklab.ws.presence.PresenceClient;
import io.coderf.arklab.ws.presence.PresenceConfig;
import io.coderf.arklab.ws.presence.PresenceConnectCallback;
import io.coderf.arklab.ws.session.WsSession;
import io.coderf.arklab.ws.widget.WsReconnectDialog;

/**
 * WebSocket 组件演示页（Java 接入示例）。
 * <p>
 * 演示内容：
 * <ul>
 *     <li>{@link WsClient} + {@link WsConfig.Builder} 建立连接、发送</li>
 *     <li>Header / Query / 匿名鉴权</li>
 *     <li>{@link WsSession} + ChannelBinder（逻辑频道由宿主编码订退帧）</li>
 *     <li>有限重连 + {@link WsReconnectDialog}</li>
 *     <li>{@link PresenceClient} + {@link HeartbeatScheduler}</li>
 * </ul>
 * <p>
 * 默认 URL 为公共 echo 服务，仅供联调；生产环境请替换为服务端地址。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/30
 */
@AndroidEntryPoint
public class WsDemoActivity extends BaseActivity<EmptyViewModel, ActivityWsDemoBinding> {

    private WsClient wsClient;
    private WsSession wsSession;

    @Nullable
    private PresenceClient presenceClient;

    @Nullable
    private HeartbeatScheduler heartbeatScheduler;

    @Nullable
    private WsReconnectDialog reconnectDialog;

    private final StringBuilder logBuffer = new StringBuilder();
    private final SimpleDateFormat timeFormat =
            new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    @Override
    protected int getLayoutId() {
        return R.layout.activity_ws_demo;
    }

    @Override
    public String setTitleBar() {
        return "WebSocket 组件示例";
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        appendLog("填写可达的 ws/wss 地址后连接。Token 为空时走匿名直连。");
        appendLog("公共 echo 若 DNS 失败（Unable to resolve host），请改成本机/内网服务端。");
        appendLog("逻辑频道变更只会回调 binder（本页打印日志），业务信封请宿主自行编码。");

        binding.btnConnect.setOnClickListener(v -> connectWs());
        binding.btnDisconnect.setOnClickListener(v -> disconnectWs());
        binding.btnSend.setOnClickListener(v -> sendMessage());
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

    private void connectWs() {
        String url = textOf(binding.editUrl);
        String token = textOf(binding.editToken);
        int maxReconnectAttempts = parsePositiveInt(textOf(binding.editMaxReconnectAttempts), 20);
        int reconnectIntervalSeconds = parsePositiveInt(textOf(binding.editReconnectIntervalSeconds), 5);
        List<String> channels = parseChannels(textOf(binding.editChannels));

        if (TextUtils.isEmpty(url)) {
            showToast("URL 不能为空");
            return;
        }

        if (wsClient == null) {
            wsClient = Ws.createClient("WsDemo");
            wsClient.setChannelBinder((desired, added, removed) ->
                    appendLog("ChannelBinder desired=" + desired
                            + " added=" + added
                            + " removed=" + removed));
            wsSession = Ws.createSession(wsClient);
            wsSession.observe(this, () -> channels);
        } else if (wsSession != null) {
            wsSession.updateChannels(this, channels);
        }

        WsConfig.Builder builder = WsConfig.builder()
                .url(url)
                .authToken(token)
                .requireAuth(!TextUtils.isEmpty(token))
                .authInQuery(binding.switchAuthQuery.isChecked())
                .authHeaderName(binding.switchAuthQuery.isChecked() ? "Blade-Auth" : "Authorization")
                .pingIntervalSeconds(30)
                .automaticReconnect(true)
                .maxReconnectAttempts(maxReconnectAttempts)
                .reconnectIntervalSeconds(reconnectIntervalSeconds)
                .resubscribeOnReconnect(true);

        if (!channels.isEmpty()) {
            builder.subscribeChannels(channels.toArray(new String[0]));
        }

        dismissReconnectDialog();
        appendLog("正在连接 " + url + " ...");
        appendLog(TextUtils.isEmpty(token) ? "鉴权：匿名直连" : ("鉴权：Token via "
                + (binding.switchAuthQuery.isChecked() ? "Query" : "Header")));
        updateStatus("连接中...");

        wsClient.connect(builder.build(), new AbstractWsListener() {
            @Override
            public void onConnected(boolean reconnect) {
                dismissReconnectDialog();
                appendLog(reconnect ? "自动重连成功" : "连接成功");
                updateStatus("已连接");
                if (wsSession != null) {
                    wsSession.flushPendingSubscriptions();
                }
            }

            @Override
            public void onDisconnected() {
                appendLog("意外断连");
                updateStatus("已断连，准备重连");
            }

            @Override
            public void onReconnecting(int attempt, int maxAttempts, int nextRetryDelaySeconds) {
                appendLog("重连中 " + attempt + "/" + maxAttempts
                        + "，约 " + nextRetryDelaySeconds + " 秒后");
                ensureReconnectDialog().updateReconnectState(
                        attempt, maxAttempts, nextRetryDelaySeconds);
                if (!ensureReconnectDialog().isShowing()) {
                    ensureReconnectDialog().builder().show();
                }
            }

            @Override
            public void onReconnectExhausted() {
                dismissReconnectDialog();
                appendLog("重连次数耗尽");
                updateStatus("重连失败");
            }

            @Override
            public void onError(WsException exception) {
                appendLog("错误: " + (exception != null ? exception.getMessage() : "unknown"));
            }

            @Override
            public void onMessage(String text) {
                appendLog("收到: " + text);
            }
        }, (WsConnectCallback) ok -> {
            if (!ok) {
                appendLog("本次连接未成功");
                updateStatus("未连接");
            }
        });
    }


    private void disconnectWs() {
        dismissReconnectDialog();
        if (heartbeatScheduler != null) {
            heartbeatScheduler.stop();
            heartbeatScheduler = null;
        }
        if (presenceClient != null) {
            presenceClient.disconnect();
            presenceClient = null;
        }
        if (wsClient != null) {
            wsClient.disconnect();
        }
        updateStatus("已断开");
        appendLog("已主动断开");
    }

    private void sendMessage() {
        if (wsClient == null || !wsClient.isConnected()) {
            showToast("请先连接");
            return;
        }
        String msg = textOf(binding.editSendMessage);
        if (TextUtils.isEmpty(msg)) {
            showToast("消息不能为空");
            return;
        }
        wsClient.send(msg, (WsSendCallback) success ->
                appendLog(success ? "已发送" : "发送失败"));
    }

    private void demoPresenceHeartbeat() {
        String url = textOf(binding.editUrl);
        String token = textOf(binding.editToken);
        if (TextUtils.isEmpty(url)) {
            showToast("URL 不能为空");
            return;
        }
        if (presenceClient == null) {
            presenceClient = new PresenceClient();
        }
        if (heartbeatScheduler == null) {
            heartbeatScheduler = new HeartbeatScheduler();
        }
        PresenceConfig info = new PresenceConfig(
                url,
                token,
                binding.switchAuthQuery.isChecked() ? "Blade-Auth" : "Authorization",
                binding.switchAuthQuery.isChecked(),
                !TextUtils.isEmpty(token)
        );
        PresenceClient client = presenceClient;
        HeartbeatScheduler scheduler = heartbeatScheduler;
        client.connect(info, (PresenceConnectCallback) reconnect -> {
            appendLog(reconnect ? "Presence 重连成功" : "Presence 已连接");
            scheduler.start(() -> {
                boolean ok = client.sendHeartbeat(client.buildHeartbeatPayload(1L));
                appendLog(ok ? "Presence 心跳已发" : "Presence 心跳失败");
            });
        });
    }

    private WsReconnectDialog ensureReconnectDialog() {
        if (reconnectDialog == null) {
            reconnectDialog = new WsReconnectDialog(this);
            reconnectDialog.setOnExitClickListener(() -> {
                dismissReconnectDialog();
                disconnectWs();
                finish();
            });
        }
        return reconnectDialog;
    }

    private void dismissReconnectDialog() {
        if (reconnectDialog != null && reconnectDialog.isShowing()) {
            reconnectDialog.dismiss();
        }
    }

    private void updateStatus(String status) {
        binding.tvStatus.setText("状态：" + status);
    }

    private void appendLog(String line) {
        runOnUiThread(() -> {
            String stamp = timeFormat.format(new Date());
            logBuffer.append('[').append(stamp).append("] ").append(line).append('\n');
            binding.tvLog.setText(logBuffer.toString());
        });
    }

    private static String textOf(android.widget.EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private static int parsePositiveInt(String raw, int fallback) {
        try {
            int v = Integer.parseInt(raw);
            return v > 0 ? v : fallback;
        } catch (Exception ignore) {
            return fallback;
        }
    }

    private static List<String> parseChannels(String raw) {
        if (TextUtils.isEmpty(raw)) {
            return new ArrayList<>();
        }
        List<String> list = new ArrayList<>();
        for (String part : raw.split(",")) {
            String t = part.trim();
            if (!t.isEmpty()) {
                list.add(t);
            }
        }
        return list;
    }

    @Override
    protected void onDestroy() {
        dismissReconnectDialog();
        if (heartbeatScheduler != null) {
            heartbeatScheduler.stop();
        }
        if (presenceClient != null) {
            presenceClient.disconnect();
        }
        if (wsClient != null) {
            wsClient.shutdown();
        }
        super.onDestroy();
    }
}
