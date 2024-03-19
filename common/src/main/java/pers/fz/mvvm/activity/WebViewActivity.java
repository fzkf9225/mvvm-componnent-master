package pers.fz.mvvm.activity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.ComponentActivity;

import dagger.hilt.android.AndroidEntryPoint;
import pers.fz.mvvm.R;

import pers.fz.mvvm.base.BaseActivity;
import pers.fz.mvvm.base.BaseViewModel;

import pers.fz.mvvm.databinding.WebViewBinding;
import pers.fz.mvvm.util.CordovaDialogsHelper;
import pers.fz.mvvm.util.SystemWebChromeClient;
import pers.fz.mvvm.util.common.StringUtil;

/**
 * Created by fz on 2020/2/18
 * describe：基础的webView控件封装
 */
@AndroidEntryPoint
public class WebViewActivity extends BaseActivity<BaseViewModel, WebViewBinding> {

    public final static String TITLE = "title_text";
    public final static String LOAD_URL = "load_url";
    private String url;

    @Override
    protected int getLayoutId() {
        return R.layout.web_view;
    }

    @Override
    public String setTitleBar() {
        return null;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void initView(Bundle savedInstanceState) {
        WebSettings settings = binding.webView.getSettings();
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setJavaScriptEnabled(true);
        settings.setTextZoom(100);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        settings.setAllowFileAccess(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        binding.webView.setWebViewClient(new MyWebViewClient());
        binding.webView.setWebChromeClient(
                new SystemWebChromeClient(this,new CordovaDialogsHelper(this),binding.progressBar, toolbarBind.tvTitle)
        );
        addMenu(R.menu.menu_browser, item -> {
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
            return false;
        });
    }

    @Override
    public void initData(Bundle bundle) {
        toolbarBind.getToolbarConfig().setTitle(bundle.getString(TITLE));
        url = bundle.getString(LOAD_URL);
        if(StringUtil.isEmpty(url)){
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
