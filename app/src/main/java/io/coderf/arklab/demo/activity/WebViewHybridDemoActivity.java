package io.coderf.arklab.demo.activity;

import android.os.Bundle;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.enums.WebViewUrlTypeEnum;
import io.coderf.arklab.demo.helper.WebViewDemoFileHelper;
import io.coderf.arklab.ui.activity.UiWebViewActivity;

/**
 * WebView <b>增强版</b>演示：commonui {@link UiWebViewActivity}（MediaHelper 相册选图 + 原生定位）。
 * <p>对比基础版见 {@link WebViewBasicDemoActivity}。</p>
 */
@AndroidEntryPoint
public class WebViewHybridDemoActivity extends UiWebViewActivity {

    public static final String DEMO_DOMAIN = "io.coderf.arklab.demo";

    @Override
    public void initData(Bundle bundle) {
        try {
            WebViewDemoFileHelper.ensureDemoFiles(this);
        } catch (Exception e) {
            showToast("演示文件准备失败：" + e.getMessage());
        }

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            extras = new Bundle();
            getIntent().putExtras(extras);
        }
        extras.putString(TITLE, "WebView 增强能力");
        extras.putString(LOAD_URL, "webview/index.html");
        extras.putInt(URL_TYPE, WebViewUrlTypeEnum.ASSETS.type);
        extras.putString(DOMAIN, DEMO_DOMAIN);
        extras.putBoolean(TOOLBAR, true);
        extras.putBoolean(HAS_MENU, true);
        extras.putBoolean(ENABLE_JS_BRIDGE, true);

        super.initData(extras);
    }
}
