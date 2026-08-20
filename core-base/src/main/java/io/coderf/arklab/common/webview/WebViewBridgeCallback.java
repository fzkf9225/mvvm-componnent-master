package io.coderf.arklab.common.webview;

import androidx.annotation.NonNull;

/**
 * {@link WebViewJsBridge} 与原生能力之间的回调接口，由 {@link io.coderf.arklab.common.activity.WebViewActivity} 实现。
 * <p>将扫码、定位等能力从 WebView 层解耦，便于单测与替换实现。</p>
 */
public interface WebViewBridgeCallback {

    /**
     * H5 调用扫码：启动 {@link io.coderf.arklab.common.activity.CaptureActivity}，结果通过 JS 回调返回。
     */
    void startQrScan();

    /**
     * H5 请求单次定位，结果通过 {@link WebViewJsBridge#dispatchLocationResult} 回传。
     *
     * @param requestId H5 生成的请求 id，用于与异步结果对应
     */
    void requestSingleLocation(@NonNull String requestId);

    /**
     * 在主线程执行一段 JS（用于 Bridge 向页面派发事件）。
     *
     * @param script 完整 JS 语句，如 {@code javascript:...}
     */
    void evaluateJavascript(@NonNull String script);
}
