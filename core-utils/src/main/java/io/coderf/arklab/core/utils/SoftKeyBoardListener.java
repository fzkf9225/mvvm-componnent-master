package io.coderf.arklab.core.utils;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

/**
 * 监听软键盘显示/隐藏；成对注册与移除 GlobalLayoutListener，避免泄漏。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2017/9/6
 */
public class SoftKeyBoardListener {
    private final View rootView;
    private int rootViewVisibleHeight;
    @Nullable
    private OnSoftKeyBoardChangeListener onSoftKeyBoardChangeListener;
    @Nullable
    private ViewTreeObserver.OnGlobalLayoutListener layoutListener;

    public SoftKeyBoardListener(Activity activity) {
        rootView = activity.getWindow().getDecorView();
        layoutListener = () -> {
            Rect r = new Rect();
            rootView.getWindowVisibleDisplayFrame(r);

            int visibleHeight = r.height();
            if (rootViewVisibleHeight == 0) {
                rootViewVisibleHeight = visibleHeight;
                return;
            }

            if (rootViewVisibleHeight == visibleHeight) {
                return;
            }

            if (rootViewVisibleHeight - visibleHeight > 200) {
                if (onSoftKeyBoardChangeListener != null) {
                    onSoftKeyBoardChangeListener.keyBoardShow(rootViewVisibleHeight - visibleHeight);
                }
                rootViewVisibleHeight = visibleHeight;
                return;
            }

            if (visibleHeight - rootViewVisibleHeight > 200) {
                if (onSoftKeyBoardChangeListener != null) {
                    onSoftKeyBoardChangeListener.keyBoardHide(visibleHeight - rootViewVisibleHeight);
                }
                rootViewVisibleHeight = visibleHeight;
            }
        };
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);

        if (activity instanceof LifecycleOwner owner) {
            owner.getLifecycle().addObserver(new DefaultLifecycleObserver() {
                @Override
                public void onDestroy(@NonNull LifecycleOwner lifecycleOwner) {
                    destroy();
                    owner.getLifecycle().removeObserver(this);
                }
            });
        }
    }

    public void setOnSoftKeyBoardChangeListener(OnSoftKeyBoardChangeListener onSoftKeyBoardChangeListener) {
        this.onSoftKeyBoardChangeListener = onSoftKeyBoardChangeListener;
    }

    /** 手动移除监听（Activity 非 LifecycleOwner 时务必在 onDestroy 调用）。 */
    public void destroy() {
        if (layoutListener != null) {
            ViewTreeObserver observer = rootView.getViewTreeObserver();
            if (observer.isAlive()) {
                observer.removeOnGlobalLayoutListener(layoutListener);
            }
            layoutListener = null;
        }
        onSoftKeyBoardChangeListener = null;
    }

    public interface OnSoftKeyBoardChangeListener {
        void keyBoardShow(int height);

        void keyBoardHide(int height);
    }
}
