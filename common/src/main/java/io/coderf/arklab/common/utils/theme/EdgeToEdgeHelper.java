package io.coderf.arklab.common.utils.theme;

import android.view.View;
import android.view.ViewGroup;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
     * Toolbar 区域：顶部留出状态栏，保留原有 actionBar 高度。
     */
    public static void applyToolbarInsets(@NonNull View toolbar, int actionBarHeightPx) {
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, windowInsets) -> {
            Insets statusBars = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(v.getPaddingLeft(), statusBars.top, v.getPaddingRight(), v.getPaddingBottom());
            ViewGroup.LayoutParams lp = v.getLayoutParams();
            if (lp != null) {
                lp.height = actionBarHeightPx + statusBars.top;
                v.setLayoutParams(lp);
            }
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(toolbar);
    }

    /**
     * 正文容器：底部留出导航栏 / 输入法区域。
     */
    public static void applyNavigationBarInsets(@NonNull View content) {
        ViewCompat.setOnApplyWindowInsetsListener(content, (v, windowInsets) -> {
            Insets navBars = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());
            Insets ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            int bottom = Math.max(navBars.bottom, ime.bottom);
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), bottom);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(content);
    }

    /**
     * 无 Toolbar 页面：同时处理顶部状态栏与底部导航栏。
     */
    public static void applySystemBarInsets(@NonNull View root) {
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets statusBars = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());
            Insets navBars = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());
            Insets ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            int bottom = Math.max(navBars.bottom, ime.bottom);
            v.setPadding(statusBars.left, statusBars.top, statusBars.right, bottom);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(root);
    }
}
