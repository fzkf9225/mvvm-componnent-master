package io.coderf.arklab.common.utils.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.net.Uri;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.webkit.WebViewAssetLoader;

import io.coderf.arklab.common.BuildConfig;
import io.coderf.arklab.common.helper.WebViewLocalUrlResolver;

/**
 * {@link io.coderf.arklab.common.widget.customview.ConfigurableWebView} 专用 WebViewClient。
 * <p>职责：AssetLoader 拦截、外链/电话/邮件跳转、加载错误与 SSL 策略。</p>
 */
public class ConfigurableWebViewClient extends WebViewClient {

    private final WebViewAssetLoader assetLoader;

    @Nullable
    private OnPageLoadListener pageLoadListener;

    @Nullable
    private Context appContext;

    /** 与 ConfigurableWebView 一致的虚拟域名，用于解析 local:// 页内跳转 */
    @Nullable
    private String virtualDomain;

    public ConfigurableWebViewClient(@NonNull WebViewAssetLoader assetLoader) {
        this.assetLoader = assetLoader;
    }

    /**
     * 配置页内 {@code local://}、{@code file:///android_asset/} 等跳转解析。
     */
    public void setLocalNavigationContext(@NonNull Context context, @NonNull String domain) {
        this.appContext = context.getApplicationContext();
        this.virtualDomain = domain;
    }

    public void setOnPageLoadListener(@Nullable OnPageLoadListener listener) {
        this.pageLoadListener = listener;
    }

    @Nullable
    @Override
    public WebResourceResponse shouldInterceptRequest(
            @NonNull WebView view,
            @NonNull WebResourceRequest request
    ) {
        return assetLoader.shouldInterceptRequest(request.getUrl());
    }

    @Override
    public boolean shouldOverrideUrlLoading(@NonNull WebView view, @NonNull WebResourceRequest request) {
        if (!request.isForMainFrame()) {
            return false;
        }
        Uri uri = request.getUrl();
        if (uri == null) {
            return false;
        }
        String scheme = uri.getScheme();
        if (scheme == null) {
            return false;
        }
        String lower = scheme.toLowerCase();
        // http(s) 及虚拟域名（assets/本地）在 WebView 内打开
        if ("http".equals(lower) || "https".equals(lower)) {
            if (virtualDomain != null) {
                String upgraded = WebViewLocalUrlResolver.upgradeToSecureVirtualUrl(
                        virtualDomain, uri.toString());
                if (!upgraded.equals(uri.toString())) {
                    view.loadUrl(upgraded);
                    return true;
                }
            }
            return false;
        }
        // H5 内跳转：local://、file:///android_asset/ 转为虚拟 https 地址
        if (appContext != null && virtualDomain != null
                && ("local".equals(lower) || uri.toString().startsWith(WebViewLocalUrlResolver.ASSET_LEGACY_PREFIX))) {
            String resolved = WebViewLocalUrlResolver.resolveLoadUrl(
                    appContext,
                    virtualDomain,
                    io.coderf.arklab.common.enums.WebViewUrlTypeEnum.AUTO.type,
                    uri.toString()
            );
            view.loadUrl(resolved);
            return true;
        }
        // tel / mailto / sms 等交给系统
        if ("tel".equals(lower) || "mailto".equals(lower) || "sms".equals(lower) || "smsto".equals(lower)) {
            try {
                android.content.Intent intent = new android.content.Intent(
                        android.content.Intent.ACTION_VIEW, uri);
                view.getContext().startActivity(intent);
            } catch (Exception ignored) {
            }
            return true;
        }
        // 其他 scheme（如 intent://）尝试交给系统
        if (!"file".equals(lower) && !"about".equals(lower)) {
            try {
                android.content.Intent intent = new android.content.Intent(
                        android.content.Intent.ACTION_VIEW, uri);
                view.getContext().startActivity(intent);
                return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    @Override
    public void onPageStarted(@NonNull WebView view, @NonNull String url, @Nullable Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        if (pageLoadListener != null) {
            pageLoadListener.onPageStarted(url);
        }
    }

    @Override
    public void onPageFinished(@NonNull WebView view, @NonNull String url) {
        super.onPageFinished(view, url);
        if (pageLoadListener != null) {
            pageLoadListener.onPageFinished(url);
        }
    }

    @Override
    public void onReceivedError(
            @NonNull WebView view,
            @NonNull WebResourceRequest request,
            @NonNull WebResourceError error
    ) {
        super.onReceivedError(view, request, error);
        if (request.isForMainFrame() && pageLoadListener != null) {
            pageLoadListener.onMainFrameError(
                    error.getErrorCode(),
                    String.valueOf(error.getDescription())
            );
        }
    }

    @SuppressLint("WebViewClientOnReceivedSslError")
    @Override
    public void onReceivedSslError(
            @NonNull WebView view,
            @NonNull SslErrorHandler handler,
            @NonNull SslError error
    ) {
        if (pageLoadListener != null) {
            pageLoadListener.onSslError(error);
        }
        // Debug 便于内网测试；Release 拒绝不安全证书
        if (BuildConfig.DEBUG) {
            handler.proceed();
        } else {
            handler.cancel();
        }
    }

    /**
     * 页面加载生命周期与错误回调，由 Activity 设置 Toast / 埋点等。
     */
    public interface OnPageLoadListener {

        void onPageStarted(@NonNull String pageUrl);

        void onPageFinished(@NonNull String pageUrl);

        void onMainFrameError(int errorCode, @NonNull String description);

        void onSslError(@NonNull SslError error);
    }
}
