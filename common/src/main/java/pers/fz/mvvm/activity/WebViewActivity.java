package pers.fz.mvvm.activity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import dagger.hilt.android.AndroidEntryPoint;
import pers.fz.mvvm.R;
import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.databinding.WebViewBinding;
import pers.fz.mvvm.enums.WebViewUrlTypeEnum;
import pers.fz.mvvm.helper.CordovaDialogsHelper;
import pers.fz.mvvm.utils.common.SystemWebChromeClient;
import pers.fz.mvvm.viewmodel.EmptyViewModel;

/**
 * 基于ConfigurableWebView的WebViewActivity
 * 功能：
 * 1. 支持加载网络/本地/自动判断URL类型
 * 2. 支持自定义Toolbar显示
 * 3. 提供复制链接和浏览器打开功能
 * 4. 支持返回键控制网页后退
 */
@AndroidEntryPoint
public class WebViewActivity extends BaseActivity<EmptyViewModel, WebViewBinding> {
    /**
     * 页面标题,Intent参数Key
     */
    public final static String TITLE = "titleText";
    /**
     * URL类型(0:网络,1:本地,2:自动)
      */
    public final static String URL_TYPE = "urlType";
    /**
     * 加载的URL
     */
    public final static String LOAD_URL = "loadUrl";
    /**
     * 是否显示Toolbar
     */
    public final static String TOOLBAR = "toolbar";
    /**
     * 是否显示右上角菜单
     */
    public final static String HAS_MENU = "hasMenu";
    /**
     * 自定义域名(用于加载本地资源)
     */
    public final static String DOMAIN = "domain";

    /**
     * 当前加载的URL
     */
    protected String url;
    /**
     * URL类型
     */
    protected int urlType;
    /**
     * 是否显示Toolbar
     */
    protected boolean hasToolbar;
    /**
     * 是否显示菜单
     */
    protected boolean hasMenu;
    /**
     * 自定义域名
     */
    protected String domain;

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
        // 设置WebChromeClient显示进度条和标题
        binding.webView.setWebChromeClient(new SystemWebChromeClient(
                this,
                new CordovaDialogsHelper(this),
                binding.progressBar,
                toolbarBind == null ? null : toolbarBind.tvTitle
        ));
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

        if (TextUtils.isEmpty(url)) {
            showToast("目标地址为空！");
            return super.onOptionsItemSelected(item);
        }

        int itemId = item.getItemId();
        if (itemId == R.id.toolbar_browser) {
            // 在浏览器中打开
            openInBrowser();
        } else if (itemId == R.id.toolbar_copy) {
            // 复制链接
            copyUrlToClipboard();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void initData(Bundle bundle) {
        // 初始化参数
        urlType = bundle.getInt(URL_TYPE, WebViewUrlTypeEnum.AUTO.type);
        url = bundle.getString(LOAD_URL);
        hasMenu = bundle.getBoolean(HAS_MENU, true);
        domain = bundle.getString(DOMAIN);
        if (TextUtils.isEmpty(url)) {
            showToast("目标地址不存在！");
            return;
        }
        // 设置标题
        if (hasToolbar) {
            toolbarBind.getToolbarConfig().setTitle(bundle.getString(TITLE));
        }

        // 检查URL有效性
        if (TextUtils.isEmpty(url)) {
            showToast("目标地址为空！");
            return;
        }

        // 获取ConfigurableWebView实例并配置
        binding.webView.setUrlType(urlType);
        if (!TextUtils.isEmpty(domain)) {
            binding.webView.setDomain(domain);
        }
        binding.webView.loadConfiguredUrl(url);
    }

    /**
     * 在浏览器中打开当前URL
     */
    private void openInBrowser() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    /**
     * 复制当前URL到剪贴板
     */
    private void copyUrlToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("网页链接", url);
        clipboard.setPrimaryClip(clipData);
        showToast("复制成功");
    }

    /**
     * 显示WebView页面
     *
     * @param context   上下文
     * @param loadUrl   要加载的URL
     * @param titleText 页面标题
     */
    public static void show(Context context, String loadUrl, String titleText) {
        show(context, loadUrl, titleText, true, true);
    }

    /**
     * 显示WebView页面（带Toolbar配置）
     *
     * @param context    上下文
     * @param loadUrl    要加载的URL
     * @param titleText  页面标题
     * @param hasToolbar 是否显示Toolbar
     */
    public static void show(Context context, String loadUrl, String titleText, boolean hasToolbar) {
        show(context, loadUrl, titleText, hasToolbar, true);
    }

    /**
     * 显示WebView页面（完整配置）
     *
     * @param context    上下文
     * @param loadUrl    要加载的URL
     * @param titleText  页面标题
     * @param hasToolbar 是否显示Toolbar
     * @param hasMenu    是否显示菜单
     */
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

    /**
     * 显示WebView页面（完整配置+自定义域名）
     */
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

    /**
     * 显示WebView页面（使用Bundle传递参数）
     */
    public static void show(Context context, Bundle bundle) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 处理返回键，优先网页后退
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (binding.webView.canGoBack()) {
                binding.webView.goBack();
                return true;
            } else {
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
