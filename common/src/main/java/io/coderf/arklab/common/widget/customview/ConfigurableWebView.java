package io.coderf.arklab.common.widget.customview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.webkit.WebViewAssetLoader;

import java.io.File;

import io.coderf.arklab.common.BuildConfig;
import io.coderf.arklab.common.R;
import io.coderf.arklab.common.enums.WebViewUrlTypeEnum;
import io.coderf.arklab.common.helper.JavaScriptAssetsPathHandler;
import io.coderf.arklab.common.helper.StoragePathHandler;
import io.coderf.arklab.common.helper.WebViewLocalUrlResolver;
import io.coderf.arklab.common.utils.common.ConfigurableWebViewClient;
import io.coderf.arklab.common.utils.common.WebViewUtil;

/**
 * 可配置 URL 类型的 WebView：支持网络、APK assets、应用沙盒/下载目录本地文件。
 * <p>本地与 assets 均通过 {@link WebViewAssetLoader} 映射为 <b>https</b> 虚拟域名（安全源，支持 Geolocation 等 API）。</p>
 */
public class ConfigurableWebView extends WebView {

    private WebViewAssetLoader assetLoader;
    private ConfigurableWebViewClient webViewClient;

    /** url 类型，见 {@link WebViewUrlTypeEnum} */
    private int urlType = WebViewUrlTypeEnum.AUTO.type;

    /** 业务传入的原始地址 */
    private String loadUrl;

    private static final String DEFAULT_DOMAIN = "arklab.coderf.com";

    /** 与 WebViewAssetLoader 一致的虚拟域名 */
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
    private void init(@Nullable AttributeSet attrs) {
        if (attrs != null) {
            TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.ConfigurableWebView);
            domain = ta.getString(R.styleable.ConfigurableWebView_domain);
            if (TextUtils.isEmpty(domain)) {
                domain = DEFAULT_DOMAIN;
            }
            ta.recycle();
        } else {
            domain = DEFAULT_DOMAIN;
        }
        WebViewUtil.setDefaultSetting(this);
        rebuildAssetLoader(domain);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            setRendererPriorityPolicy(WebView.RENDERER_PRIORITY_IMPORTANT, true);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            getSettings().setAllowUniversalAccessFromFileURLs(true);
            getSettings().setAllowFileAccessFromFileURLs(true);
        }

        webViewClient = new ConfigurableWebViewClient(assetLoader);
        webViewClient.setLocalNavigationContext(getContext(), domain);
        setWebViewClient(webViewClient);

        setInitialScale(1);
        requestFocusFromTouch();
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);
    }

    /**
     * 按域名重建 AssetLoader（assets + 沙盒 files + external + 下载目录）。
     */
    private void rebuildAssetLoader(@NonNull String domainName) {
        // 使用 https 虚拟源（默认），满足 H5 Geolocation 等「仅安全源」限制
        WebViewAssetLoader.Builder builder = new WebViewAssetLoader.Builder()
                .setDomain(domainName)
                .addPathHandler("/assets/", new JavaScriptAssetsPathHandler(getContext()));

        File filesDir = getContext().getFilesDir();
        if (filesDir != null) {
            builder.addPathHandler("/files/", new StoragePathHandler(filesDir));
        }
        File externalDir = getContext().getExternalFilesDir(null);
        if (externalDir != null) {
            builder.addPathHandler("/external/", new StoragePathHandler(externalDir));
        }
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (downloadDir != null) {
            builder.addPathHandler("/download/", new StoragePathHandler(downloadDir));
        }
        assetLoader = builder.build();
        if (webViewClient != null) {
            webViewClient = new ConfigurableWebViewClient(assetLoader);
            webViewClient.setLocalNavigationContext(getContext(), domain);
            setWebViewClient(webViewClient);
        }
    }

    /**
     * 设置 URL 类型，见 {@link WebViewUrlTypeEnum}。
     */
    public void setUrlType(int urlType) {
        this.urlType = urlType;
    }

    /**
     * 设置虚拟域名（加载 assets / 本地文件时必须与 {@link #loadConfiguredUrl} 生成的域名一致）。
     */
    public void setDomain(@NonNull String domain) {
        this.domain = domain;
        rebuildAssetLoader(domain);
    }

    public void setAssetLoader(@NonNull WebViewAssetLoader assetLoader) {
        this.assetLoader = assetLoader;
        webViewClient = new ConfigurableWebViewClient(assetLoader);
        setWebViewClient(webViewClient);
    }

    public void setOnPageLoadListener(@Nullable ConfigurableWebViewClient.OnPageLoadListener listener) {
        if (webViewClient != null) {
            webViewClient.setOnPageLoadListener(listener);
        }
    }

    /**
     * 加载 URL。assets 类型只需传 assets 下相对路径；本地类型支持 {@link WebViewLocalUrlResolver} 约定协议。
     */
    public void loadConfiguredUrl(@Nullable String url) {
        this.loadUrl = url;
        if (url == null || url.isEmpty()) {
            return;
        }
        String target = WebViewLocalUrlResolver.resolveLoadUrl(getContext(), domain, urlType, url);
        loadUrl(target);
    }

    @Nullable
    public String getLoadUrl() {
        return loadUrl;
    }

    public int getUrlType() {
        return urlType;
    }

    @NonNull
    public String getDomain() {
        return domain;
    }

    /**
     * 在 Activity {@code onDestroy} 中调用，释放 WebView 资源（勿在 {@code onDetachedFromWindow} 自动 destroy，避免配置变更误销毁）。
     */
    public void release() {
        stopLoading();
        loadUrl("about:blank");
        onPause();
        removeAllViews();
        destroy();
    }
}
