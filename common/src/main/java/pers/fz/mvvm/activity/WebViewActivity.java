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
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;

import dagger.hilt.android.AndroidEntryPoint;
import pers.fz.mvvm.R;
import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.databinding.WebViewBinding;
import pers.fz.mvvm.util.common.CordovaDialogsHelper;
import pers.fz.mvvm.util.common.SystemWebChromeClient;
import pers.fz.mvvm.util.common.StringUtil;
import pers.fz.mvvm.util.common.WebViewUtil;
import pers.fz.mvvm.viewmodel.EmptyViewModel;

/**
 * Created by fz on 2020/2/18
 * describe：基础的webView控件封装
 */
@AndroidEntryPoint
public class WebViewActivity extends BaseActivity<EmptyViewModel, WebViewBinding> {

    public final static String TITLE = "titleText";
    public final static String LOAD_URL = "loadUrl";
    public final static String TOOLBAR = "toolbar";
    private String url;
    private boolean hasToolbar;

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
        WebViewUtil.setDefaultSetting(binding.webView);
        binding.webView.setWebViewClient(new MyWebViewClient());
        binding.webView.setWebChromeClient(new SystemWebChromeClient(this, new CordovaDialogsHelper(this), binding.progressBar, toolbarBind == null ? null : toolbarBind.tvTitle));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!hasToolbar) {
            return super.onCreateOptionsMenu(menu);
        }
        getMenuInflater().inflate(R.menu.menu_browser, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (!hasToolbar) {
            return super.onOptionsItemSelected(item);
        }
        if (StringUtil.isEmpty(url)) {
            showToast("目标地址为空！");
            return super.onOptionsItemSelected(item);
        }
        int itemId = item.getItemId();
        if (itemId == R.id.toolbar_browser) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } else if (itemId == R.id.toolbar_copy) {// 获取系统剪贴板
            ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
            // 创建一个剪贴数据集，包含一个普通文本数据条目（需要复制的数据）
            ClipData clipData = ClipData.newPlainText("文件链接", url);
            // 把数据集设置（复制）到剪贴板
            clipboard.setPrimaryClip(clipData);
            showToast("复制成功");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void initData(Bundle bundle) {
        if (hasToolbar) {
            toolbarBind.getToolbarConfig().setTitle(bundle.getString(TITLE));
        }
        url = bundle.getString(LOAD_URL);
        if (TextUtils.isEmpty(url)) {
            showToast("目标地址为空！");
            return;
        }
        binding.webView.loadUrl(url);
    }

    /**
     * show the MainActivity
     * 本地测试地址："file:///android_asset/protocol.html"
     *
     * @param context context
     */
    public static void show(Context context, String loadUrl, String titleText) {
        Intent intent = new Intent(context, WebViewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(LOAD_URL, loadUrl);
        bundle.putString(TITLE, titleText);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static void show(Context context, String loadUrl, String titleText,boolean hasToolbar) {
        Intent intent = new Intent(context, WebViewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(LOAD_URL, loadUrl);
        bundle.putString(TITLE, titleText);
        bundle.putBoolean(TOOLBAR, hasToolbar);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static void show(Context context, Bundle bundle) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    private static class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            view.loadUrl(request.getUrl().toString());
            return true;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
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
