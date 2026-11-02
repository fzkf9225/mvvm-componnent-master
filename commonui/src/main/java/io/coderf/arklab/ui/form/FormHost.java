package io.coderf.arklab.ui.form;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

/**
 * 页面级表单校验辅助：遍历子树中的 {@link FormConstraintLayout}，
 * 失败时聚焦并尽量滚到首错行。
 * <p>
 * 不依赖 annotation 反射；媒体等特殊行请在对应子类 override {@link FormConstraintLayout#validate()}。
 * </p>
 */
public final class FormHost {

    private FormHost() {
    }

    /**
     * @return 全部通过为 true；否则已对首个失败行 {@code setError} 并尝试滚动
     */
    public static boolean validateAll(@NonNull ViewGroup root) {
        FormConstraintLayout firstFailed = validateRecursive(root);
        if (firstFailed == null) {
            return true;
        }
        firstFailed.requestFocus();
        scrollToForm(firstFailed);
        return false;
    }

    @Nullable
    private static FormConstraintLayout validateRecursive(@NonNull ViewGroup group) {
        final int count = group.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = group.getChildAt(i);
            if (child instanceof FormConstraintLayout form) {
                if (!form.validate()) {
                    return form;
                }
            } else if (child instanceof ViewGroup nested) {
                FormConstraintLayout failed = validateRecursive(nested);
                if (failed != null) {
                    return failed;
                }
            }
        }
        return null;
    }

    private static void scrollToForm(@NonNull FormConstraintLayout form) {
        ViewParent parent = form.getParent();
        while (parent instanceof View) {
            if (parent instanceof NestedScrollView nsv) {
                nsv.post(() -> nsv.smoothScrollTo(0, form.getTop()));
                return;
            }
            if (parent instanceof android.widget.ScrollView sv) {
                sv.post(() -> sv.smoothScrollTo(0, form.getTop()));
                return;
            }
            parent = parent.getParent();
        }
    }
}
