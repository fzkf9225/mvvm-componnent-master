package io.coderf.arklab.common.webview;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.coderf.arklab.common.utils.log.LogUtil;

/**
 * WebView 与 H5 的 JSBridge：扫码、单次定位等。
 * <p>
 * 注入名：{@link #BRIDGE_NAME}。页面需实现全局回调对象 {@code window.__arkWebViewCallbacks}，
 * 详见 assets 中 {@code webview/hybrid_demo.html} 示例。
 * </p>
 *
 * <pre>
 * // H5 调用示例
 * ArkWebView.startScan();
 * ArkWebView.requestLocation('req-001');
 * </pre>
 */
public class WebViewJsBridge {

    /** {@link WebView#addJavascriptInterface} 使用的名称，勿与页面其它对象冲突 */
    public static final String BRIDGE_NAME = "ArkWebView";

    private static final String TAG = "WebViewJsBridge";

    /** 注入到页面中的回调容器脚本（在 onPageFinished 后执行一次） */
    public static final String INIT_CALLBACK_SCRIPT =
            "window.__arkWebViewCallbacks=window.__arkWebViewCallbacks||{};"
                    + "if(!window.__arkWebViewCallbacks.onScanResult){"
                    + "window.__arkWebViewCallbacks.onScanResult=function(){};}"
                    + "if(!window.__arkWebViewCallbacks.onLocationResult){"
                    + "window.__arkWebViewCallbacks.onLocationResult=function(){};}";

    private final WebView webView;
    private final WebViewBridgeCallback callback;

    public WebViewJsBridge(@NonNull WebView webView, @NonNull WebViewBridgeCallback callback) {
        this.webView = webView;
        this.callback = callback;
    }

    /**
     * 启动原生二维码扫描（结果见 {@link #dispatchScanResult}）。
     */
    @JavascriptInterface
    public void startScan() {
        LogUtil.logger(TAG, "startScan from JS");
        webView.post(callback::startQrScan);
    }

    /**
     * 请求单次 GPS 定位（结果见 {@link #dispatchLocationResult}）。
     *
     * @param requestId 非空请求 id，由 H5 生成
     */
    @JavascriptInterface
    public void requestLocation(@Nullable String requestId) {
        if (TextUtils.isEmpty(requestId)) {
            LogUtil.logger(TAG, "requestLocation: empty requestId");
            return;
        }
        LogUtil.logger(TAG, "requestLocation: " + requestId);
        webView.post(() -> callback.requestSingleLocation(requestId));
    }

    /**
     * 将扫码结果派发到 H5。
     *
     * @param text 扫码文本，取消或失败时传空字符串
     */
    public void dispatchScanResult(@NonNull String text) {
        String escaped = escapeJsString(text);
        dispatchJs("javascript:window.__arkWebViewCallbacks.onScanResult && "
                + "window.__arkWebViewCallbacks.onScanResult('" + escaped + "');");
    }

    /**
     * 将定位结果派发到 H5。
     *
     * @param requestId 与 {@link #requestLocation} 一致
     * @param locationJson 定位 JSON，失败传 {@code null} 时在 JS 侧收到 null
     */
    public void dispatchLocationResult(@NonNull String requestId, @Nullable String locationJson) {
        String req = escapeJsString(requestId);
        String payload = locationJson == null ? "null" : locationJson;
        dispatchJs("javascript:window.__arkWebViewCallbacks.onLocationResult && "
                + "window.__arkWebViewCallbacks.onLocationResult('" + req + "'," + payload + ");");
    }

    /**
     * 在主线程执行任意 JS。
     */
    public void dispatchJs(@NonNull String script) {
        callback.evaluateJavascript(script);
    }

    /**
     * 页面加载完成后注入回调占位，避免 H5 尚未注册监听时丢事件。
     */
    public void ensureCallbacksInitialized() {
        dispatchJs(INIT_CALLBACK_SCRIPT);
    }

    /**
     * 构建定位结果 JSON 字符串。
     */
    @NonNull
    public static String buildLocationJson(double latitude, double longitude, float accuracy) {
        return "{\"latitude\":" + latitude
                + ",\"longitude\":" + longitude
                + ",\"accuracy\":" + accuracy + "}";
    }

    @NonNull
    private static String escapeJsString(@NonNull String raw) {
        return raw.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
