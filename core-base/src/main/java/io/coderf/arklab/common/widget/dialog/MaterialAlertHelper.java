package io.coderf.arklab.common.widget.dialog;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * 标准确认/提示走 MaterialAlertDialog。
 * 富文本、三按钮细粒度样式继续用 {@link ConfirmDialog} / {@link MessageDialog}。
 */
public final class MaterialAlertHelper {

    private MaterialAlertHelper() {
    }

    public static void confirm(@NonNull Context context,
                               @Nullable CharSequence title,
                               @Nullable CharSequence message,
                               @Nullable CharSequence positive,
                               @Nullable Runnable onPositive) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positive, (dialog, which) -> {
                    if (onPositive != null) {
                        onPositive.run();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    public static void message(@NonNull Context context,
                               @Nullable CharSequence title,
                               @Nullable CharSequence message) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
