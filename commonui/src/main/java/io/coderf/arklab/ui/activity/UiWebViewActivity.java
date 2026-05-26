package io.coderf.arklab.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.coderf.arklab.common.activity.WebViewActivity;
import io.coderf.arklab.common.utils.common.NativeWebChromeClient;
import io.coderf.arklab.media.MediaBuilder;
import io.coderf.arklab.media.MediaHelper;
import io.coderf.arklab.media.enums.MediaPickerTypeEnum;
import io.coderf.arklab.ui.webview.MediaWebChromeClient;

/**

 * commonui 增强 WebView：在 {@link WebViewActivity} 基础上仅增强<b>文件选择</b>（commonmedia {@link MediaHelper}）。

 * <p>

 * 定位仍使用 common 模块原生 {@link io.coderf.arklab.common.helper.WebViewNativeLocationHelper}，

 * 不依赖 googlegps 轨迹/上传等复杂能力。

 * </p>

 * <p>对比：仅需 common 依赖时用 {@link WebViewActivity}；需要相册选图/媒体 Dialog 时用本类。</p>

 */
public class UiWebViewActivity extends WebViewActivity {

    @Nullable
    private MediaHelper mediaHelper;

    @Override
    public void initView(Bundle savedInstanceState) {
        mediaHelper = new MediaBuilder(this)
                .bindLifeCycle(this)
                // Photo Picker 优先，减少存储权限与「已授权仍提示拒绝」问题
                .setChooseType(MediaPickerTypeEnum.PICK)
                .setFileMaxSelectedCount(9)
                .setImageMaxSelectedCount(1)
                .setVideoMaxSelectedCount(1)
                .setWriteCaptureExifMetadata(false)
                .builder();
        super.initView(savedInstanceState);
    }

    @NonNull
    @Override
    protected NativeWebChromeClient createWebChromeClient() {
        return new MediaWebChromeClient(
                this,
                dialogsHelper,
                binding.progressBar,
                getWebTitleTextView(),
                mediaHelper
        );
    }

    public static void show(Context context, String loadUrl, String titleText) {
        show(context, loadUrl, titleText, true, true);
    }

    public static void show(Context context, String loadUrl, String titleText, boolean hasToolbar) {
        show(context, loadUrl, titleText, hasToolbar, true);
    }

    public static void show(Context context, String loadUrl, String titleText,
                            boolean hasToolbar, boolean hasMenu) {
        Intent intent = new Intent(context, UiWebViewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(LOAD_URL, loadUrl);
        bundle.putString(TITLE, titleText);
        bundle.putBoolean(TOOLBAR, hasToolbar);
        bundle.putBoolean(HAS_MENU, hasMenu);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static void show(Context context, String loadUrl, String titleText,
                            boolean hasToolbar, boolean hasMenu, String domain, int urlType) {
        Intent intent = new Intent(context, UiWebViewActivity.class);
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
        Intent intent = new Intent(context, UiWebViewActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

}

