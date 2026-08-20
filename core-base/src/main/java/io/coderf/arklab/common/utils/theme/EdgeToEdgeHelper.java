package io.coderf.arklab.common.utils.theme;

import android.view.View;
import android.view.ViewGroup;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import io.coderf.arklab.common.widget.customview.ActionToolbar;

/**
 * Android 15 Edge-to-Edge 统一适配：在基类 Activity 中启用并分发系统栏 Insets。
 */
public final class EdgeToEdgeHelper {

    private EdgeToEdgeHelper() {
    }

    public static void enable(@NonNull ComponentActivity activity) {
        EdgeToEdge.enable(activity);
    }

    /**
     * Toolbar 区域：顶部留出状态栏，内容区高度为 {@code actionBarHeightPx}。
     */
    public static void applyToolbarInsets(@NonNull Toolbar toolbar, int actionBarHeightPx, boolean customHeight) {
        int contentHeightPx = customHeight ? actionBarHeightPx : 0;
        applyContentHeight(toolbar, contentHeightPx);
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, windowInsets) -> {
            Insets statusBars = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(v.getPaddingLeft(), statusBars.top, v.getPaddingRight(), v.getPaddingBottom());
            ViewGroup.LayoutParams lp = v.getLayoutParams();
            if (lp != null) {
                lp.height = actionBarHeightPx + statusBars.top;
                v.setLayoutParams(lp);
            }
            applyContentHeight(toolbar, contentHeightPx);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(toolbar);
    }

    /**
     * 非 Edge-to-Edge 场景下应用自定义 Toolbar 高度。
     */
    public static void applyToolbarHeight(@NonNull Toolbar toolbar, int toolbarHeightPx, boolean customHeight) {
        ViewGroup.LayoutParams lp = toolbar.getLayoutParams();
        if (lp != null) {
            lp.height = toolbarHeightPx;
            toolbar.setLayoutParams(lp);
        }
        applyContentHeight(toolbar, customHeight ? toolbarHeightPx : 0);
    }

    private static void applyContentHeight(@NonNull Toolbar toolbar, int contentHeightPx) {
        if (toolbar instanceof ActionToolbar) {
            ((ActionToolbar) toolbar).setContentHeightPx(contentHeightPx);
        } else if (contentHeightPx > 0) {
            toolbar.setMinimumHeight(contentHeightPx);
        }
    }

    /**
     * 正文容器：底部留出导航栏；默认不随软键盘上顶。
     *
     * @see #applyNavigationBarInsets(View, boolean)
     */
    public static void applyNavigationBarInsets(@NonNull View content) {
        applyNavigationBarInsets(content, false);
    }

    /**
     * 正文容器：底部留出导航栏，可选是否随软键盘上顶。
     *
     * @param adjustForIme {@code true} 时底部内容随输入法上移；{@code false} 时贴底控件保持原位。
     */
    public static void applyNavigationBarInsets(@NonNull View content, boolean adjustForIme) {
        ViewCompat.setOnApplyWindowInsetsListener(content, (v, windowInsets) -> {
            Insets navBars = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());
            Insets ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            int bottom = adjustForIme ? Math.max(navBars.bottom, ime.bottom) : navBars.bottom;
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), bottom);
            return stripImeInsets(windowInsets, adjustForIme);
        });
        ViewCompat.requestApplyInsets(content);
    }

    /**
     * 无 Toolbar 页面：同时处理顶部状态栏与底部导航栏；默认不随软键盘上顶。
     *
     * @see #applySystemBarInsets(View, boolean)
     */
    public static void applySystemBarInsets(@NonNull View root) {
        applySystemBarInsets(root, false);
    }

    /**
     * 无 Toolbar 页面：同时处理顶部状态栏与底部导航栏，可选是否随软键盘上顶。
     *
     * @param adjustForIme {@code true} 时底部内容随输入法上移；{@code false} 时贴底控件保持原位。
     */
    public static void applySystemBarInsets(@NonNull View root, boolean adjustForIme) {
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets statusBars = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());
            Insets navBars = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());
            Insets ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            int bottom = adjustForIme ? Math.max(navBars.bottom, ime.bottom) : navBars.bottom;
            v.setPadding(statusBars.left, statusBars.top, statusBars.right, bottom);
            return stripImeInsets(windowInsets, adjustForIme);
        });
        ViewCompat.requestApplyInsets(root);
    }

    private static WindowInsetsCompat stripImeInsets(WindowInsetsCompat windowInsets, boolean adjustForIme) {
        if (adjustForIme) {
            return windowInsets;
        }
        Insets ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
        if (ime.bottom <= 0 && ime.top <= 0 && ime.left <= 0 && ime.right <= 0) {
            return windowInsets;
        }
        return new WindowInsetsCompat.Builder(windowInsets)
                .setInsets(WindowInsetsCompat.Type.ime(), Insets.NONE)
                .build();
    }
}
