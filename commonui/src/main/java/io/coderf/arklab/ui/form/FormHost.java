package io.coderf.arklab.ui.form;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

/**
 * 页面级表单校验辅助：遍历子树中的 {@link FormValidatable}
 *（{@link FormConstraintLayout} 与 {@link FormMedia}），失败时聚焦并尽量滚到首错行。
 * <p>
 * 不依赖 annotation 反射；媒体行通过 {@link FormMedia#validate()} 参与整页校验。
 * </p>
 */
public final class FormHost {

    private FormHost() {
    }

    /**
     * @return 全部通过为 true；否则已对首个失败行 {@code setError} 并尝试滚动
     */
    public static boolean validateAll(@NonNull ViewGroup root) {
        View firstFailed = validateRecursive(root);
        if (firstFailed == null) {
            return true;
        }
        firstFailed.requestFocus();
        scrollToView(firstFailed);
        return false;
    }

    @Nullable
    private static View validateRecursive(@NonNull ViewGroup group) {
        final int count = group.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = group.getChildAt(i);
            if (child instanceof FormValidatable form) {
                if (!form.validate()) {
                    return child;
                }
                // Form* 自身即叶子校验单元，不再下钻其子 View，避免重复
                continue;
            }
            if (child instanceof ViewGroup nested) {
                View failed = validateRecursive(nested);
                if (failed != null) {
                    return failed;
                }
            }
        }
        return null;
    }

    private static void scrollToView(@NonNull View form) {
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
