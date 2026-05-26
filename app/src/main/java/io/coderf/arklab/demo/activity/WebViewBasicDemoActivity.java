package io.coderf.arklab.demo.activity;

import android.os.Bundle;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.activity.WebViewActivity;
import io.coderf.arklab.common.enums.WebViewUrlTypeEnum;
import io.coderf.arklab.demo.helper.WebViewDemoFileHelper;

/**
 * WebView <b>基础版</b>演示：仅依赖 common 模块的 {@link WebViewActivity}。
 * <p>
 * 与 {@link WebViewHybridDemoActivity}（commonui 增强版）对比：
 * </p>
 * <ul>
 *     <li>文件选择：系统选择器 + 拍照（{@link io.coderf.arklab.common.utils.common.NativeWebChromeClient}）</li>
 *     <li>定位：原生 {@link io.coderf.arklab.common.helper.WebViewNativeLocationHelper}</li>
 *     <li>不依赖 commonmedia / googlegps</li>
 * </ul>
 */
@AndroidEntryPoint
public class WebViewBasicDemoActivity extends WebViewActivity {

    /** 与 Demo 包名一致，供 WebViewAssetLoader 虚拟域名使用 */
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
        extras.putString(TITLE, "WebView 基础能力");
        extras.putString(LOAD_URL, "webview/index.html");
        extras.putInt(URL_TYPE, WebViewUrlTypeEnum.ASSETS.type);
        extras.putString(DOMAIN, DEMO_DOMAIN);
        extras.putBoolean(TOOLBAR, true);
        extras.putBoolean(HAS_MENU, true);
        extras.putBoolean(ENABLE_JS_BRIDGE, true);

        super.initData(extras);
    }
}
