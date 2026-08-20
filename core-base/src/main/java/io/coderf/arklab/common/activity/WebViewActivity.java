package io.coderf.arklab.common.activity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.R;
import io.coderf.arklab.common.base.BaseActivity;
import io.coderf.arklab.common.databinding.WebViewBinding;
import io.coderf.arklab.common.enums.WebViewUrlTypeEnum;
import io.coderf.arklab.common.helper.CordovaDialogsHelper;
import io.coderf.arklab.common.helper.WebViewLocalUrlResolver;
import io.coderf.arklab.common.helper.WebViewNativeLocationHelper;
import io.coderf.arklab.common.utils.common.ConfigurableWebViewClient;
import io.coderf.arklab.common.utils.common.NativeWebChromeClient;
import io.coderf.arklab.common.viewmodel.EmptyViewModel;
import io.coderf.arklab.common.webview.WebViewBridgeCallback;
import io.coderf.arklab.common.webview.WebViewJsBridge;
import io.coderf.arklab.common.webview.WebViewMenuAction;
import io.coderf.arklab.common.widget.dialog.WebViewActionSheetDialog;

/**
 * 通用 WebView 容器（<b>仅依赖 common 模块</b>，无 commonmedia / googlegps）。
 * <p>能力：</p>
 * <ul>
 *     <li>网络 / assets / 沙盒 / 下载目录（{@link WebViewUrlTypeEnum}）</li>
 *     <li>JS alert/confirm/prompt（{@link CordovaDialogsHelper}）</li>
 *     <li>原生文件选择与拍照（{@link NativeWebChromeClient}）</li>
 *     <li>原生单次定位（{@link WebViewNativeLocationHelper}）与扫码 JSBridge</li>
 * </ul>
 * <p>需要 MediaHelper / GpsStarter 增强能力请使用 commonui 的 {@code UiWebViewActivity}。</p>
 */
@AndroidEntryPoint
public class WebViewActivity extends BaseActivity<EmptyViewModel, WebViewBinding>
        implements WebViewBridgeCallback {

    /** Intent：页面标题 */
    public static final String TITLE = "titleText";
    /** Intent：URL 类型，见 {@link WebViewUrlTypeEnum#type} */
    public static final String URL_TYPE = "urlType";
    /** Intent：加载地址 */
    public static final String LOAD_URL = "loadUrl";
    /** Intent：是否显示 Toolbar */
    public static final String TOOLBAR = "toolbar";
    /** Intent：是否显示右上角菜单 */
    public static final String HAS_MENU = "hasMenu";
    /** Intent：本地/assets 虚拟域名 */
    public static final String DOMAIN = "domain";
    /** Intent：是否注入 {@link WebViewJsBridge}，默认 true */
    public static final String ENABLE_JS_BRIDGE = "enableJsBridge";

    /** 底部菜单：在系统浏览器中打开 */
    public static final int WEB_MENU_OPEN_BROWSER = 1;
    /** 底部菜单：复制链接 */
    public static final int WEB_MENU_COPY_LINK = 2;

    protected String url;
    protected int urlType;
    protected boolean hasToolbar;
    protected boolean hasMenu;
    protected String domain;
    protected boolean enableJsBridge;

    protected CordovaDialogsHelper dialogsHelper;
    @Nullable
    protected NativeWebChromeClient nativeWebChromeClient;
    @Nullable
    protected WebViewNativeLocationHelper nativeLocationHelper;
    @Nullable
    protected WebViewJsBridge jsBridge;
    @Nullable
    private ActivityResultLauncher<ScanOptions> scanLauncher;

    @Override
    protected int getLayoutId() {
        return R.layout.web_view;
    }

    @Override
    protected boolean hasToolBar() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            return true;
        }
        return hasToolbar = bundle.getBoolean(TOOLBAR, true);
    }

    @Override
    public String setTitleBar() {
        return null;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void initView(Bundle savedInstanceState) {
        dialogsHelper = new CordovaDialogsHelper(this);
        nativeLocationHelper = createLocationHelper();
        nativeWebChromeClient = createWebChromeClient();
        binding.webView.setWebChromeClient(nativeWebChromeClient);

        scanLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (jsBridge == null) {
                return;
            }
            jsBridge.dispatchScanResult(
                    result.getContents() != null ? result.getContents() : ""
            );
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.webView.canGoBack()) {
                    binding.webView.goBack();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    /**
     * 创建 WebChromeClient，默认 {@link NativeWebChromeClient}。子类可覆盖以接入 commonui 增强实现。
     */
    @NonNull
    protected NativeWebChromeClient createWebChromeClient() {
        return new NativeWebChromeClient(
                this,
                dialogsHelper,
                binding.progressBar,
                getWebTitleTextView()
        );
    }

    /**
     * 创建定位助手，默认 {@link WebViewNativeLocationHelper}。返回 null 表示由子类自行实现定位（如 GpsStarter）。
     */
    @Nullable
    protected WebViewNativeLocationHelper createLocationHelper() {
        return new WebViewNativeLocationHelper(this);
    }

    @Nullable
    protected TextView getWebTitleTextView() {
        return toolbarBind == null ? null : toolbarBind.tvTitle;
    }

    @Override
    public void initData(Bundle bundle) {
        urlType = bundle.getInt(URL_TYPE, WebViewUrlTypeEnum.AUTO.type);
        url = bundle.getString(LOAD_URL);
        hasMenu = bundle.getBoolean(HAS_MENU, true);
        domain = bundle.getString(DOMAIN);
        enableJsBridge = bundle.getBoolean(ENABLE_JS_BRIDGE, true);

        if (TextUtils.isEmpty(url)) {
            showToast("目标地址不存在！");
            return;
        }

        if (hasToolbar && toolbarBind != null) {
            toolbarBind.getToolbarConfig().setTitle(bundle.getString(TITLE));
        }

        binding.webView.setUrlType(urlType);
        if (!TextUtils.isEmpty(domain)) {
            binding.webView.setDomain(domain);
        }

        binding.webView.setOnPageLoadListener(new ConfigurableWebViewClient.OnPageLoadListener() {
            @Override
            public void onPageStarted(@NonNull String pageUrl) {
            }

            @Override
            public void onPageFinished(@NonNull String pageUrl) {
                if (jsBridge != null) {
                    jsBridge.ensureCallbacksInitialized();
                }
            }

            @Override
            public void onMainFrameError(int errorCode, @NonNull String description) {
                showToast(describeLoadError(errorCode, description));
            }

            @Override
            public void onSslError(@NonNull android.net.http.SslError error) {
                showToast("SSL 证书校验失败");
            }
        });

        if (enableJsBridge) {
            jsBridge = new WebViewJsBridge(binding.webView, this);
            binding.webView.addJavascriptInterface(jsBridge, WebViewJsBridge.BRIDGE_NAME);
        }

        binding.webView.loadConfiguredUrl(url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!hasToolbar || !hasMenu) {
            return super.onCreateOptionsMenu(menu);
        }
        getMenuInflater().inflate(R.menu.menu_browser, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (!hasToolbar || !hasMenu) {
            return super.onOptionsItemSelected(item);
        }
        if (item.getItemId() == R.id.toolbar_web_menu) {
            showWebViewActionSheet();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 展示微信风格底部操作面板。子类可覆盖 {@link #buildWebViewMenuActions()} 增删菜单项。
     */
    protected void showWebViewActionSheet() {
        List<WebViewMenuAction> actions = buildWebViewMenuActions();
        if (actions.isEmpty()) {
            return;
        }
        String hint = getString(R.string.webview_sheet_provider, resolveWebViewProviderLabel());
        new WebViewActionSheetDialog(this, hint, actions, this::onWebViewMenuActionSelected).show();
    }

    /**
     * 构建底部菜单项，子类可 super 后追加或完全重写。
     */
    @NonNull
    protected List<WebViewMenuAction> buildWebViewMenuActions() {
        List<WebViewMenuAction> list = new ArrayList<>(2);
        list.add(new WebViewMenuAction(
                WEB_MENU_OPEN_BROWSER,
                R.drawable.ic_webview_action_browser,
                getString(R.string.webview_action_open_browser)
        ));
        list.add(new WebViewMenuAction(
                WEB_MENU_COPY_LINK,
                R.drawable.ic_webview_action_copy,
                getString(R.string.webview_action_copy_link)
        ));
        return list;
    }

    /**
     * 底部菜单点击，默认处理内置项；子类可先处理自定义 id 再 super。
     */
    protected void onWebViewMenuActionSelected(@NonNull WebViewMenuAction action) {
        if (TextUtils.isEmpty(url)) {
            showToast("目标地址为空！");
            return;
        }
        int id = action.getId();
        if (id == WEB_MENU_OPEN_BROWSER) {
            openInBrowser();
        } else if (id == WEB_MENU_COPY_LINK) {
            copyUrlToClipboard();
        }
    }

    /**
     * 面板顶部「此网页由 xxx 提供」中的 xxx。
     */
    @NonNull
    protected String resolveWebViewProviderLabel() {
        if (WebViewLocalUrlResolver.isNetworkUrl(url)) {
            try {
                Uri uri = Uri.parse(url);
                String host = uri.getHost();
                if (!TextUtils.isEmpty(host)) {
                    return host;
                }
            } catch (Exception ignored) {
                // fall through
            }
            return url;
        }
        if (!TextUtils.isEmpty(domain)) {
            return domain;
        }
        return getString(R.string.webview_sheet_local_provider);
    }

    private void openInBrowser() {
        String openUrl = url;
        if (!WebViewLocalUrlResolver.isNetworkUrl(url)) {
            openUrl = WebViewLocalUrlResolver.resolveLoadUrl(this, binding.webView.getDomain(), urlType, url);
        }
        if (!WebViewLocalUrlResolver.isNetworkUrl(openUrl)) {
            showToast("本地页面无法在浏览器中打开");
            return;
        }
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(openUrl)));
    }

    private void copyUrlToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("网页链接", url));
        showToast("复制成功");
    }

    @NonNull
    private static String describeLoadError(int errorCode, @NonNull String description) {
        if (errorCode == WebViewClient.ERROR_HOST_LOOKUP) {
            return "无法解析域名，请检查网络连接或更换访问地址";
        }
        if (errorCode == WebViewClient.ERROR_CONNECT) {
            return "无法连接服务器，请检查网络";
        }
        if (errorCode == WebViewClient.ERROR_TIMEOUT) {
            return "连接超时，请稍后重试";
        }
        return "页面加载失败：" + description;
    }

    @Override
    protected void onPause() {
        binding.webView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.webView.onResume();
    }

    @Override
    protected void onDestroy() {
        if (dialogsHelper != null) {
            dialogsHelper.destroyLastDialog();
        }
        if (nativeWebChromeClient != null) {
            nativeWebChromeClient.cancelPendingFileChooser();
        }
        releaseLocationHelper();
        if (jsBridge != null) {
            binding.webView.removeJavascriptInterface(WebViewJsBridge.BRIDGE_NAME);
        }
        binding.webView.release();
        super.onDestroy();
    }

    /**
     * 释放定位资源，子类若使用 GpsStarter 需覆盖此方法。
     */
    protected void releaseLocationHelper() {
        if (nativeLocationHelper != null) {
            nativeLocationHelper.cancel();
            nativeLocationHelper = null;
        }
    }

    // region WebViewBridgeCallback

    @Override
    public void startQrScan() {
        if (scanLauncher == null) {
            return;
        }
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setCaptureActivity(CaptureActivity.class);
        options.setOrientationLocked(false);
        options.setPrompt("");
        scanLauncher.launch(options);
    }

    @Override
    public void requestSingleLocation(@NonNull String requestId) {
        if (nativeLocationHelper == null) {
            dispatchLocationToJs(requestId, null);
            return;
        }
        nativeLocationHelper.requestSingleLocation(
                location -> dispatchLocationToJs(requestId, location)
        );
    }

    @Override
    public void evaluateJavascript(@NonNull String script) {
        String js = script.startsWith("javascript:") ? script.substring("javascript:".length()) : script;
        binding.webView.post(() -> binding.webView.evaluateJavascript(js, null));
    }

    protected void dispatchLocationToJs(@NonNull String requestId, @Nullable Location location) {
        if (jsBridge == null) {
            return;
        }
        String json = location == null ? null : WebViewJsBridge.buildLocationJson(
                location.getLatitude(), location.getLongitude(), location.getAccuracy());
        jsBridge.dispatchLocationResult(requestId, json);
    }

    // endregion

    // region 静态启动（保持原有签名）

    public static void show(Context context, String loadUrl, String titleText) {
        show(context, loadUrl, titleText, true, true);
    }

    public static void show(Context context, String loadUrl, String titleText, boolean hasToolbar) {
        show(context, loadUrl, titleText, hasToolbar, true);
    }

    public static void show(Context context, String loadUrl, String titleText, boolean hasToolbar, boolean hasMenu) {
        Intent intent = new Intent(context, WebViewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(LOAD_URL, loadUrl);
        bundle.putString(TITLE, titleText);
        bundle.putBoolean(TOOLBAR, hasToolbar);
        bundle.putBoolean(HAS_MENU, hasMenu);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static void show(Context context, String loadUrl, String titleText, boolean hasToolbar,
                            boolean hasMenu, String domain, int urlType) {
        Intent intent = new Intent(context, WebViewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(LOAD_URL, loadUrl);
        bundle.putString(TITLE, titleText);
        bundle.putBoolean(TOOLBAR, hasToolbar);
        bundle.putBoolean(HAS_MENU, hasMenu);
        bundle.putString(DOMAIN, domain);
        bundle.putInt(URL_TYPE, urlType);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static void show(Context context, Bundle bundle) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    // endregion

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (binding.webView.canGoBack()) {
                binding.webView.goBack();
                return true;
            }
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
