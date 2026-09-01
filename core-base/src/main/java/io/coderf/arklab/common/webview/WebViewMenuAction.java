package io.coderf.arklab.common.webview;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

/**
 * WebView 底部操作面板中的单项。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
public class WebViewMenuAction {

    private final int id;
    @DrawableRes
    private final int iconRes;
    @NonNull
    private final String title;

    public WebViewMenuAction(int id, @DrawableRes int iconRes, @NonNull String title) {
        this.id = id;
        this.iconRes = iconRes;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    @DrawableRes
    public int getIconRes() {
        return iconRes;
    }

    @NonNull
    public String getTitle() {
        return title;
    }
}
