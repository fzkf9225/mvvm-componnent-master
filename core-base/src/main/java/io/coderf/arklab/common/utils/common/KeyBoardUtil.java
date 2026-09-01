package io.coderf.arklab.common.utils.common;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.textfield.TextInputEditText;

/**
 * 打开或关闭软键盘。
 * <p>
 * API 30+ 优先使用 {@link WindowInsetsControllerCompat} 显示/隐藏 IME，
 * 避免 {@link InputMethodManager#toggleSoftInput(int, int)} 等已废弃 API。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/8/25 13:20
 */
public class KeyBoardUtil {

    private KeyBoardUtil() {
    }

    /**
     * 打开软键盘。
     *
     * @param mEditText 输入框
     * @param mContext  上下文（Activity 时走 InsetsController，更稳）
     */
    public static void openKeyboard(TextInputEditText mEditText, Context mContext) {
        if (mEditText == null || mContext == null) {
            return;
        }
        mEditText.requestFocus();
        if (!showImeWithInsets(mContext, mEditText)) {
            InputMethodManager imm = getImm(mContext);
            if (imm != null) {
                // 不再使用已废弃的 toggleSoftInput / SHOW_FORCED
                imm.showSoftInput(mEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    /**
     * 关闭软键盘。
     *
     * @param mEditText 输入框
     * @param mContext  上下文
     */
    public static void closeKeyboard(TextInputEditText mEditText, Context mContext) {
        if (mEditText == null || mContext == null) {
            return;
        }
        if (!hideImeWithInsets(mContext, mEditText)) {
            InputMethodManager imm = getImm(mContext);
            if (imm != null) {
                imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
            }
        }
    }

    public static void setListener(Activity activity,
                                   SoftKeyBoardListener.OnSoftKeyBoardChangeListener onSoftKeyBoardChangeListener) {
        SoftKeyBoardListener softKeyBoardListener = new SoftKeyBoardListener(activity);
        softKeyBoardListener.setOnSoftKeyBoardChangeListener(onSoftKeyBoardChangeListener);
    }

    /**
     * 在 Activity 的 dispatchTouchEvent 中调用。
     *
     * @deprecated 请使用 {@link io.coderf.arklab.core.ui.delegate.HideKeyboardOnTouchOutsideDelegate}；
     * {@link io.coderf.arklab.common.base.BaseActivity} 已内置。本方法转发到 core-ui 实现。
     */
    @Deprecated
    public static boolean handleDispatchTouchEvent(Activity activity, MotionEvent ev) {
        return io.coderf.arklab.core.ui.delegate.HideKeyboardOnTouchOutsideDelegate
                .handleTouch(activity, ev);
    }

    /**
     * 隐藏软键盘（基于当前焦点的 View）。
     */
    public static void hideSoftInput(Activity activity) {
        if (activity == null) {
            return;
        }
        View view = activity.getCurrentFocus();
        if (view == null) {
            // 无焦点时仍尝试通过 window 隐藏 IME
            View decor = activity.getWindow() != null ? activity.getWindow().getDecorView() : null;
            if (decor != null) {
                hideImeWithInsets(activity, decor);
            }
            return;
        }
        hideSoftInput(activity, view);
    }

    /**
     * 隐藏软键盘（指定 View）。
     */
    public static void hideSoftInput(Context context, View view) {
        if (context == null || view == null) {
            return;
        }
        if (!hideImeWithInsets(context, view)) {
            InputMethodManager imm = getImm(context);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    /**
     * 显示软键盘。
     */
    public static void showSoftInput(Context context, View view) {
        if (context == null || view == null) {
            return;
        }
        view.requestFocus();
        if (!showImeWithInsets(context, view)) {
            InputMethodManager imm = getImm(context);
            if (imm != null) {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    /**
     * 切换软键盘状态。
     * <p>
     * 不再调用已废弃的 {@link InputMethodManager#toggleSoftInput(int, int)}：
     * 根据当前 IME 可见性选择 show / hide。
     */
    public static void toggleSoftInput(Context context) {
        if (context == null) {
            return;
        }
        View view = resolveFocusView(context);
        if (view == null) {
            return;
        }
        if (isImeVisible(view)) {
            hideSoftInput(context, view);
        } else {
            showSoftInput(context, view);
        }
    }

    /**
     * 判断软键盘是否显示。
     * API 30+ 优先读 WindowInsets；低版本回退 {@link InputMethodManager#isActive(View)}（仅供参考）。
     */
    public static boolean isSoftInputActive(Activity activity) {
        if (activity == null) {
            return false;
        }
        View view = activity.getCurrentFocus();
        if (view == null) {
            View decor = activity.getWindow() != null ? activity.getWindow().getDecorView() : null;
            if (decor != null) {
                return isImeVisible(decor);
            }
            return false;
        }
        if (isImeVisible(view)) {
            return true;
        }
        InputMethodManager imm = getImm(activity);
        return imm != null && imm.isActive(view);
    }

    // -------------------------------------------------------------------------
    // Insets / IMM helpers
    // -------------------------------------------------------------------------

    @Nullable
    private static InputMethodManager getImm(@NonNull Context context) {
        return (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Nullable
    private static View resolveFocusView(@NonNull Context context) {
        if (context instanceof Activity activity) {
            View focus = activity.getCurrentFocus();
            if (focus != null) {
                return focus;
            }
            Window window = activity.getWindow();
            return window != null ? window.getDecorView() : null;
        }
        return null;
    }

    /**
     * @return true 表示已用 InsetsController 处理；false 表示调用方应回退 IMM。
     */
    private static boolean showImeWithInsets(@NonNull Context context, @NonNull View view) {
        WindowInsetsControllerCompat controller = resolveInsetsController(context, view);
        if (controller == null) {
            return false;
        }
        controller.show(WindowInsetsCompat.Type.ime());
        return true;
    }

    /**
     * @return true 表示已用 InsetsController 处理；false 表示调用方应回退 IMM。
     */
    private static boolean hideImeWithInsets(@NonNull Context context, @NonNull View view) {
        WindowInsetsControllerCompat controller = resolveInsetsController(context, view);
        if (controller == null) {
            return false;
        }
        controller.hide(WindowInsetsCompat.Type.ime());
        return true;
    }

    @Nullable
    private static WindowInsetsControllerCompat resolveInsetsController(
            @NonNull Context context,
            @NonNull View view
    ) {
        if (context instanceof Activity) {
            Window window = ((Activity) context).getWindow();
            if (window != null) {
                return WindowCompat.getInsetsController(window, view);
            }
        }
        // 非 Activity Context：尝试从 View 的 window token 关联的 root 获取（可能为 null）
        return null;
    }

    private static boolean isImeVisible(@NonNull View view) {
        WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(view);
        if (insets != null) {
            return insets.isVisible(WindowInsetsCompat.Type.ime());
        }
        // 低版本或 insets 尚未分发
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            Context context = view.getContext();
            InputMethodManager imm = getImm(context);
            return imm != null && imm.isActive(view);
        }
        return false;
    }
}
