package pers.fz.mvvm.widget.customview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.net.http.SslError;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.webkit.WebViewAssetLoader;

import pers.fz.mvvm.R;
import pers.fz.mvvm.enums.WebViewUrlTypeEnum;
import pers.fz.mvvm.util.common.WebViewUtil;

/**
 * created by fz on 2025/6/27 9:33
 * describe:
 */
public class ConfigurableWebView extends WebView {

    private WebViewAssetLoader assetLoader;
    /**
     * url类型，0：网络地址，1：本地assets地址，2：自动
     */
    private int urlType = WebViewUrlTypeEnum.AUTO.type;
    /**
     * url地址
     */
    private String loadUrl;
    private final String DEFAULT_DOMAIN = "casic.titan.com";
    /**
     * 自定义域名
     */
    private String domain = DEFAULT_DOMAIN;

    public ConfigurableWebView(Context context) {
        super(context);
        init(null);
    }

    public ConfigurableWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ConfigurableWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.ConfigurableWebView);
            domain = ta.getString(R.styleable.ConfigurableWebView_domain);
            if (TextUtils.isEmpty(domain)) {
                domain = DEFAULT_DOMAIN;
            }
        } else {
            domain = DEFAULT_DOMAIN;
        }
        WebViewUtil.setDefaultSetting(this);
        // 初始化AssetLoader用于加载本地资源
        assetLoader = new WebViewAssetLoader.Builder()
                .setHttpAllowed(true)
                .setDomain(domain)
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(getContext()))
                .build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            setRendererPriorityPolicy(WebView.RENDERER_PRIORITY_IMPORTANT, true);
        }

        // 根据Android版本设置不同的WebViewClient
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setWebViewClient(new Api29WebViewClient());
        } else {
            getSettings().setAllowUniversalAccessFromFileURLs(true);
            getSettings().setAllowFileAccessFromFileURLs(true);
            getSettings().setEnableSmoothTransition(true);
            setWebViewClient(new OldWebViewClient());
        }

        setInitialScale(1);
        requestFocusFromTouch();
        WebView.setWebContentsDebuggingEnabled(true);
    }

    /**
     * 设置URL类型
     *
     * @param urlType 0：网络地址，1：本地assets地址，2：自动判断
     */
    public void setUrlType(int urlType) {
        this.urlType = urlType;
    }

    /**
     * 本地的话需要设置域名
     *
     * @param domain 自定义域名
     */
    public void setDomain(String domain) {
        // 初始化AssetLoader用于加载本地资源
        assetLoader = new WebViewAssetLoader.Builder()
                .setHttpAllowed(true)
                .setDomain(domain)
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(getContext()))
                .build();
    }

    /**
     * 加载URL
     *
     * @param url 要加载的URL
     */
    public void loadConfiguredUrl(String url) {
        this.loadUrl = url;

        if (url == null || url.isEmpty()) {
            return;
        }

        if (urlType == WebViewUrlTypeEnum.INTERNET.type) {// 直接加载网络URL
            loadUrl(url);
        } else if (urlType == WebViewUrlTypeEnum.ASSETS.type) {// 加载本地assets文件
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                loadUrl("http://" + domain + "/assets/" + url);
            } else {
                loadUrl("file:///android_asset/" + url);
            }
        } else if (urlType == WebViewUrlTypeEnum.AUTO.type) {// 自动判断URL类型
            if (url.startsWith("http://") || url.startsWith("https://")) {
                loadUrl(url);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    loadUrl("http://" + domain + "/assets/" + url);
                } else {
                    loadUrl("file:///android_asset/" + url);
                }
            }
        }
    }

    /**
     * 获取当前设置的URL
     *
     * @return 当前URL
     */
    public String getLoadUrl() {
        return loadUrl;
    }

    /**
     * 获取当前URL类型
     *
     * @return URL类型
     */
    public int getUrlType() {
        return urlType;
    }

    /**
     * 用于API 29+的WebViewClient
     */
    private class Api29WebViewClient extends WebViewClient {
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            return assetLoader.shouldInterceptRequest(request.getUrl());
        }

        @SuppressLint("WebViewClientOnReceivedSslError")
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed(); // 忽略SSL错误
        }
    }

    /**
     * 用于API 29以下的WebViewClient
     */
    private static class OldWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            view.loadUrl(request.getUrl().toString());
            return true;
        }

        @SuppressLint("WebViewClientOnReceivedSslError")
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed(); // 忽略SSL错误
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        destroy();
    }
}

