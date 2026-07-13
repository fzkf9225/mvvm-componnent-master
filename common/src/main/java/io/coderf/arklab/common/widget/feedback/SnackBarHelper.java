package io.coderf.arklab.common.widget.feedback;

import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.google.android.material.snackbar.Snackbar;

/**
 * Snackbar 封装：支持短提示、带操作按钮与长时间展示。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/7/13 10:25
 */
public final class SnackBarHelper {

    private SnackBarHelper() {
    }

    public static void showShort(@NonNull View anchor, @Nullable CharSequence message) {
        show(anchor, message, Snackbar.LENGTH_SHORT, null, null);
    }

    public static void showLong(@NonNull View anchor, @Nullable CharSequence message) {
        show(anchor, message, Snackbar.LENGTH_LONG, null, null);
    }

    public static void showShort(@NonNull View anchor, @StringRes int messageRes) {
        showShort(anchor, anchor.getContext().getString(messageRes));
    }

    public static void showAction(
            @NonNull View anchor,
            @Nullable CharSequence message,
            @Nullable CharSequence actionText,
            @Nullable View.OnClickListener actionListener
    ) {
        show(anchor, message, Snackbar.LENGTH_LONG, actionText, actionListener);
    }

    public static void show(
            @NonNull View anchor,
            @Nullable CharSequence message,
            int duration,
            @Nullable CharSequence actionText,
            @Nullable View.OnClickListener actionListener
    ) {
        if (TextUtils.isEmpty(message)) {
            return;
        }
        Snackbar snackbar = Snackbar.make(anchor, message, duration);
        if (!TextUtils.isEmpty(actionText) && actionListener != null) {
            snackbar.setAction(actionText, actionListener);
        }
        snackbar.show();
    }
}
