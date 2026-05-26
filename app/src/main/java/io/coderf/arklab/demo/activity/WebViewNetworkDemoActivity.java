package io.coderf.arklab.demo.activity;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.activity.WebViewActivity;
import io.coderf.arklab.common.enums.WebViewUrlTypeEnum;

/**
 * WebView <b>网络页</b>演示：在 app 模块提供 Hilt 入口。
 * <p>默认加载公网 https 页面；无网络时会在加载前提示。</p>
 */
@AndroidEntryPoint
public class WebViewNetworkDemoActivity extends WebViewActivity {

    private static final String DEFAULT_NETWORK_URL = "https://blog.csdn.net/fzkf9225";

    @Override
    public void initData(Bundle bundle) {
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            extras = new Bundle();
            getIntent().putExtras(extras);
        }
        if (!extras.containsKey(LOAD_URL)) {
            extras.putString(LOAD_URL, DEFAULT_NETWORK_URL);
        }
        if (!extras.containsKey(URL_TYPE)) {
            extras.putInt(URL_TYPE, WebViewUrlTypeEnum.INTERNET.type);
        }
        if (!extras.containsKey(TITLE)) {
            extras.putString(TITLE, "WebView 网络示例");
        }
        extras.putBoolean(TOOLBAR, extras.getBoolean(TOOLBAR, true));
        extras.putBoolean(HAS_MENU, extras.getBoolean(HAS_MENU, true));

        if (!isNetworkAvailable()) {
            showToast("当前无可用网络，请检查 Wi-Fi/移动数据或模拟器网络设置");
        }

        super.initData(extras);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        Network network = cm.getActiveNetwork();
        if (network == null) {
            return false;
        }
        NetworkCapabilities caps = cm.getNetworkCapabilities(network);
        return caps != null
                && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }
}
