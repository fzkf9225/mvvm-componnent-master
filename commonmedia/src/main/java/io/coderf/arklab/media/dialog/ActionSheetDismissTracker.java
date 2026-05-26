package io.coderf.arklab.media.dialog;

import android.app.Dialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.WeakHashMap;

/**
 * 底部 ActionSheet 在未选择「相册/拍照/文件」等项即关闭时回调（点取消或点外部）。
 */
public final class ActionSheetDismissTracker {

    private static final WeakHashMap<Dialog, ActionSheetDismissTracker> TRACKERS = new WeakHashMap<>();

    private boolean userChoseAction;

    private ActionSheetDismissTracker() {
    }

    @NonNull
    public static ActionSheetDismissTracker attach(
            @NonNull Dialog dialog,
            @Nullable Runnable onDismissWithoutAction
    ) {
        ActionSheetDismissTracker tracker = new ActionSheetDismissTracker();
        TRACKERS.put(dialog, tracker);
        dialog.setOnDismissListener(d -> {
            TRACKERS.remove(dialog);
            if (!tracker.userChoseAction && onDismissWithoutAction != null) {
                onDismissWithoutAction.run();
            }
            tracker.userChoseAction = false;
        });
        return tracker;
    }

    /** 用户点了会拉起相册/相机/文件管理器等项时，须在 {@link Dialog#dismiss()} 之前调用。 */
    public static void markUserChoseAction(@NonNull Dialog dialog) {
        ActionSheetDismissTracker tracker = TRACKERS.get(dialog);
        if (tracker != null) {
            tracker.userChoseAction = true;
        }
    }
}
