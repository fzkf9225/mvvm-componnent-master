package io.coderf.arklab.common.widget.dialog;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * 标准确认 / 提示走 {@link MaterialAlertDialogBuilder}
 * （主题见 {@code ThemeOverlay.App.MaterialAlertDialog}）。
 * <p>
 * 富文本、自定义布局、细粒度按钮样式继续用 {@link ConfirmDialog} / {@link MessageDialog}。
 *
 * @author fz
 * @version 1.1
 * @updated 2026/9/21
 */
public final class MaterialAlertHelper {

    private MaterialAlertHelper() {
    }

    // ---------- 确认（确定 + 取消） ----------

    public static void confirm(@NonNull Context context,
                               @Nullable CharSequence title,
                               @Nullable CharSequence message,
                               @Nullable CharSequence positive,
                               @Nullable Runnable onPositive) {
        confirm(context, title, message, positive, null, onPositive, null, true);
    }

    public static void confirm(@NonNull Context context,
                               @Nullable CharSequence title,
                               @Nullable CharSequence message,
                               @Nullable CharSequence positive,
                               @Nullable CharSequence negative,
                               @Nullable Runnable onPositive,
                               @Nullable Runnable onNegative) {
        confirm(context, title, message, positive, negative, onPositive, onNegative, true);
    }

    /**
     * @param cancelable 是否允许点外部 / 返回键关闭
     */
    public static void confirm(@NonNull Context context,
                               @Nullable CharSequence title,
                               @Nullable CharSequence message,
                               @Nullable CharSequence positive,
                               @Nullable CharSequence negative,
                               @Nullable Runnable onPositive,
                               @Nullable Runnable onNegative,
                               boolean cancelable) {
        CharSequence pos = positive != null ? positive : context.getString(android.R.string.ok);
        CharSequence neg = negative != null ? negative : context.getString(android.R.string.cancel);
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(pos, (dialog, which) -> {
                    if (onPositive != null) {
                        onPositive.run();
                    }
                })
                .setNegativeButton(neg, (dialog, which) -> {
                    if (onNegative != null) {
                        onNegative.run();
                    }
                })
                .setCancelable(cancelable)
                .show();
    }

    public static void confirm(@NonNull Context context,
                               @StringRes int titleRes,
                               @StringRes int messageRes,
                               @Nullable Runnable onPositive) {
        confirm(context,
                context.getString(titleRes),
                context.getString(messageRes),
                null,
                onPositive);
    }

    // ---------- 单按钮提示 ----------

    public static void message(@NonNull Context context,
                               @Nullable CharSequence title,
                               @Nullable CharSequence message) {
        message(context, title, message, null, null, true);
    }

    public static void message(@NonNull Context context,
                               @Nullable CharSequence title,
                               @Nullable CharSequence message,
                               @Nullable CharSequence positive,
                               @Nullable Runnable onPositive) {
        message(context, title, message, positive, onPositive, true);
    }

    public static void message(@NonNull Context context,
                               @Nullable CharSequence title,
                               @Nullable CharSequence message,
                               @Nullable CharSequence positive,
                               @Nullable Runnable onPositive,
                               boolean cancelable) {
        CharSequence pos = positive != null ? positive : context.getString(android.R.string.ok);
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(pos, (dialog, which) -> {
                    if (onPositive != null) {
                        onPositive.run();
                    }
                })
                .setCancelable(cancelable)
                .show();
    }

    // ---------- 三按钮 ----------

    /**
     * 确定 / 中立 / 取消。中立按钮文案与回调均可选。
     */
    public static void confirmWithNeutral(@NonNull Context context,
                                          @Nullable CharSequence title,
                                          @Nullable CharSequence message,
                                          @Nullable CharSequence positive,
                                          @Nullable CharSequence neutral,
                                          @Nullable CharSequence negative,
                                          @Nullable Runnable onPositive,
                                          @Nullable Runnable onNeutral,
                                          @Nullable Runnable onNegative) {
        CharSequence pos = positive != null ? positive : context.getString(android.R.string.ok);
        CharSequence neg = negative != null ? negative : context.getString(android.R.string.cancel);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(pos, (dialog, which) -> {
                    if (onPositive != null) {
                        onPositive.run();
                    }
                })
                .setNegativeButton(neg, (dialog, which) -> {
                    if (onNegative != null) {
                        onNegative.run();
                    }
                });
        if (neutral != null && neutral.length() > 0) {
            builder.setNeutralButton(neutral, (dialog, which) -> {
                if (onNeutral != null) {
                    onNeutral.run();
                }
            });
        }
        builder.show();
    }
}
