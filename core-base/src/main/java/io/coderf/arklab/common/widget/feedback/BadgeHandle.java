package io.coderf.arklab.common.widget.feedback;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;

import java.lang.ref.WeakReference;

/**
 * 角标句柄：一次 {@link BadgeHelper#attach} 的产物，负责数字/文字更新与移除。
 * 页面销毁前应调用 {@link #detach()}（重复调用安全，幂等）。
 *
 * <p>内部持有持续的 layout 监听：锚点尺寸/位置变化时自动重算角标坐标，
 * 避免旋转、软键盘弹出、RecyclerView 复用时角标错位或被裁剪。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/10/20 14:30
 */
@ExperimentalBadgeUtils
public final class BadgeHandle {

    @NonNull
    private final BadgeDrawable badge;
    @NonNull
    private final WeakReference<View> anchorRef;
    @NonNull
    private final View.OnLayoutChangeListener layoutListener;

    /** 角标是否已挂载到锚点 overlay（布局未完成时先挂监听，布局后补挂）。 */
    private boolean attached;
    private boolean detached;

    /**
     * 包内构造，仅供 {@link BadgeHelper} 创建。
     *
     * @param anchor 锚点 View
     * @param badge  已按配置建好的角标
     */
    BadgeHandle(@NonNull View anchor, @NonNull BadgeDrawable badge) {
        this.badge = badge;
        this.anchorRef = new WeakReference<>(anchor);
        this.layoutListener = new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (detached) {
                    v.removeOnLayoutChangeListener(this);
                    return;
                }
                if (!attached) {
                    // 能收到 layout 回调说明锚点已完成布局，直接挂载。
                    // 注意不能用 isLaidOut() 判断：View.layout() 的 PFLAG3_IS_LAID_OUT
                    // 标志位在 onLayoutChange 回调之后才置位，首次回调时 isLaidOut() 仍为 false
                    BadgeUtils.attachBadgeDrawable(badge, v);
                    attached = true;
                } else {
                    // 锚点布局变化后重算角标坐标（官方在 overlay 机制下不会自动跟随）
                    badge.updateBadgeCoordinates(v);
                }
            }
        };
        anchor.addOnLayoutChangeListener(layoutListener);
        if (anchor.isLaidOut()) {
            BadgeUtils.attachBadgeDrawable(badge, anchor);
            attached = true;
        }
    }

    /**
     * 更新数字角标：&gt;0 显示数字，&lt;=0 清空并隐藏。需在主线程调用。
     *
     * @param number 角标数字
     */
    public void update(int number) {
        if (number > 0) {
            badge.setNumber(number);
            badge.setVisible(true);
        } else {
            badge.clearNumber();
            badge.setVisible(false);
        }
    }

    /**
     * 更新文字角标（如 "NEW"）：非空显示文字，null/空串清除文字。
     * 清除后若仍有数字则回落到数字显示。需在主线程调用。
     *
     * @param text 角标文字，传 null 或空串表示清除
     */
    public void updateText(@Nullable String text) {
        if (text == null || text.isEmpty()) {
            badge.clearText();
        } else {
            badge.setText(text);
        }
        // 文字优先于数字：有内容才显示，避免空角标残留
        badge.setVisible(badge.hasText() || badge.hasNumber());
    }

    /**
     * 移除角标并释放监听，之后本句柄不再可用（幂等，可重复调用）。
     */
    public void detach() {
        if (detached) {
            return;
        }
        detached = true;
        View anchor = anchorRef.get();
        if (anchor != null) {
            anchor.removeOnLayoutChangeListener(layoutListener);
            BadgeUtils.detachBadgeDrawable(badge, anchor);
        }
        anchorRef.clear();
        BadgeHelper.unregister(this);
    }

    /** 返回底层 {@link BadgeDrawable}，供 helper 未覆盖的微调（透明度、Locale、contentDescription 等）。 */
    @NonNull
    public BadgeDrawable badge() {
        return badge;
    }
}
