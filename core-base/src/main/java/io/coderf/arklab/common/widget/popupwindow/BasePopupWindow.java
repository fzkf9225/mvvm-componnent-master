package io.coderf.arklab.common.widget.popupwindow;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.widget.dialog.DialogHostAdaptHelper;
import io.coderf.arklab.core.utils.DensityUtil;

/**
 * PopupWindow 公共基类：统一触摸/焦点配置、遮罩、宿主蒙层、安全展示与横屏居中策略，
 * 并提供 inflate 密度对齐与 RecyclerView 高度测算，降低各业务 Popup 重复代码。
 * <p>
 * 对齐 {@link io.coderf.arklab.common.widget.dialog.BaseDialog} 的职责边界：
 * 基类只管窗口行为，子类只管内容区与选择逻辑。
 * <p>
 * Material3 无通用 {@code MaterialPopupWindow} 可替换任意自定义内容；
 * 本基类继续基于 {@link PopupWindow}，样式由子类通过 ThemeAttrs / 内容布局落地。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/11/5
 */
public abstract class BasePopupWindow extends PopupWindow {

    /** 级联类面板：竖屏宽全屏、高约 2/3；横屏宽约 1/2、高约 4/5 */
    protected static final float DEFAULT_HOST_DIM_ALPHA = 0.5f;

    protected final Context context;
    protected final LayoutInflater layoutInflater;

    /** 横屏时改为屏幕居中（Cascade 开启；轻量锚点下拉保持 false） */
    private boolean landscapeCenterEnabled = false;

    /**
     * 宿主 Activity Window 蒙层透明度；{@code <= 0} 表示不改 Window.alpha。
     * 展示时应用，dismiss 时还原。
     */
    private float hostDimAlpha = -1f;

    private boolean hostDimApplied = false;
    private float savedHostWindowAlpha = 1f;

    @Nullable
    private OnDismissListener clientDismissListener;

    public BasePopupWindow(@NonNull Context context) {
        super(context);
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        // 内部统一处理蒙层还原，再回调业务 dismiss
        super.setOnDismissListener(() -> {
            clearHostDim();
            if (clientDismissListener != null) {
                clientDismissListener.onDismiss();
            }
        });
    }

    /**
     * 当前 Context 对应的 Activity 是否仍可用于展示 PopupWindow。
     */
    public static boolean canShow(@Nullable Context context) {
        if (context == null) {
            return false;
        }
        if (context instanceof Activity activity) {
            return !activity.isFinishing() && !activity.isDestroyed();
        }
        Activity host = DialogHostAdaptHelper.activityFrom(context);
        if (host != null) {
            return !host.isFinishing() && !host.isDestroyed();
        }
        return true;
    }

    /** 当前是否横屏。 */
    protected final boolean isLandscape() {
        return context.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
    }

    /**
     * 横屏 inflate 前同步宿主 CustomAdapt 密度；竖屏不改密度。
     * 子类请用本方法替代直接 {@code XxxBinding.inflate(...)}。
     */
    @NonNull
    protected final <T> T inflateWithHostAdapt(
            @NonNull DialogHostAdaptHelper.InflateAction<T> inflate) {
        return DialogHostAdaptHelper.inflateWithHostAdapt(context, inflate);
    }

    @Nullable
    protected final Activity getHostActivity() {
        if (context instanceof Activity) {
            return (Activity) context;
        }
        return DialogHostAdaptHelper.activityFrom(context);
    }

    protected final int resolveScreenWidthPx() {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    protected final int resolveScreenHeightPx() {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * 统一 Popup 基础配置：外部可点、可触摸、可获焦、透明窗口背景。
     *
     * @param contentRoot 内容根布局，可为 null
     */
    protected void applyDefaultWindowConfig(@Nullable View contentRoot) {
        setOutsideTouchable(true);
        setTouchable(true);
        setFocusable(true);
        if (contentRoot != null) {
            contentRoot.setFocusableInTouchMode(true);
        }
        setBackgroundDrawable(ContextCompat.getDrawable(context, R.color.transparent));
    }

    /**
     * 锚点下拉类：宽 MATCH_PARENT、高 WRAP_CONTENT。
     */
    protected void applyMatchParentDropDownSize() {
        setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
    }

    /**
     * 级联面板尺寸：竖屏全宽 + 2/3 高；横屏约 1/2 宽 + 4/5 高。
     */
    protected void applyCascadePanelSize() {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        if (isLandscape()) {
            setWidth(metrics.widthPixels / 2);
            setHeight(metrics.heightPixels * 4 / 5);
        } else {
            setWidth(metrics.widthPixels);
            setHeight(metrics.heightPixels * 2 / 3);
        }
    }

    /**
     * 开启横屏居中展示（竖屏仍相对锚点下拉）。
     */
    protected void setLandscapeCenterEnabled(boolean enabled) {
        this.landscapeCenterEnabled = enabled;
    }

    /**
     * 展示时压暗宿主 Activity Window；dismiss 时自动还原。
     *
     * @param alpha 宿主 Window.alpha，通常 0.5f；传 {@code <= 0} 关闭
     */
    protected void enableHostDim(float alpha) {
        this.hostDimAlpha = alpha;
    }

    /**
     * 遮罩：点击关闭，并按屏高拉伸 mask 高度。
     */
    protected void setupMask(@Nullable View maskView) {
        if (maskView == null) {
            return;
        }
        maskView.setOnClickListener(v -> dismiss());
        View content = getContentView();
        Runnable applyHeight = () -> {
            int maskHeight = resolveScreenHeightPx();
            if (maskHeight <= 0) {
                return;
            }
            ViewGroup.LayoutParams params = maskView.getLayoutParams();
            if (params == null) {
                return;
            }
            params.height = maskHeight;
            maskView.setLayoutParams(params);
        };
        Objects.requireNonNullElse(content, maskView).post(applyHeight);
    }

    /**
     * 测算 LinearLayoutManager 列表总高度（含分割线近似）。
     *
     * @param fallbackItemLayout 首项未渲染时用于测量的 item 布局
     */
    protected final int calculateLinearRecyclerViewHeight(
            @NonNull RecyclerView recyclerView,
            @LayoutRes int fallbackItemLayout) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager == null || recyclerView.getAdapter() == null) {
            return 0;
        }
        int itemCount = recyclerView.getAdapter().getItemCount();
        if (itemCount == 0) {
            return 0;
        }

        View childView = layoutManager.findViewByPosition(0);
        if (childView == null) {
            childView = layoutInflater.inflate(fallbackItemLayout, recyclerView, false);
            childView.measure(
                    View.MeasureSpec.makeMeasureSpec(
                            Math.max(recyclerView.getWidth(), resolveScreenWidthPx()),
                            View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            return childView.getMeasuredHeight() * itemCount;
        }

        int itemHeight = childView.getHeight();
        int dividerHeight = 0;
        if (recyclerView.getItemDecorationCount() > 0) {
            dividerHeight = DensityUtil.dp2px(context, 1);
        }
        return (itemHeight * itemCount) + (dividerHeight * Math.max(0, itemCount - 1));
    }

    /**
     * 测算 GridLayoutManager 列表总高度（按行数）。
     */
    protected final int calculateGridRecyclerViewHeight(
            @NonNull RecyclerView recyclerView,
            @LayoutRes int fallbackItemLayout) {
        GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
        if (layoutManager == null || recyclerView.getAdapter() == null) {
            return 0;
        }
        int itemCount = recyclerView.getAdapter().getItemCount();
        if (itemCount == 0) {
            return 0;
        }
        int spanCount = Math.max(1, layoutManager.getSpanCount());
        int rowCount = (int) Math.ceil((double) itemCount / spanCount);

        View childView = layoutManager.findViewByPosition(0);
        if (childView == null) {
            int itemWidth = Math.max(1, Math.max(recyclerView.getWidth(), resolveScreenWidthPx()) / spanCount);
            childView = layoutInflater.inflate(fallbackItemLayout, recyclerView, false);
            childView.measure(
                    View.MeasureSpec.makeMeasureSpec(itemWidth, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            return childView.getMeasuredHeight() * rowCount;
        }

        int itemHeight = childView.getHeight();
        int dividerHeight = 0;
        if (recyclerView.getItemDecorationCount() > 0) {
            dividerHeight = DensityUtil.dp2px(context, 1);
        }
        return (itemHeight * rowCount) + (dividerHeight * Math.max(0, rowCount - 1));
    }

    /**
     * 展示：竖屏相对锚点下拉；若开启 {@link #setLandscapeCenterEnabled(boolean)} 则横屏居中。
     */
    public void show(@NonNull View anchor) {
        showAsDropDown(anchor, 0, 0);
    }

    @Override
    public void showAsDropDown(View anchor) {
        showAsDropDown(anchor, 0, 0);
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff) {
        if (!prepareShow(anchor)) {
            return;
        }
        if (shouldShowCentered()) {
            showCentered(anchor);
            return;
        }
        super.showAsDropDown(anchor, xoff, yoff);
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff, int gravity) {
        if (!prepareShow(anchor)) {
            return;
        }
        if (shouldShowCentered()) {
            showCentered(anchor);
            return;
        }
        super.showAsDropDown(anchor, xoff, yoff, gravity);
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        if (!canShow(context) || parent == null) {
            return;
        }
        applyHostDimIfNeeded();
        super.showAtLocation(parent, gravity, x, y);
    }

    @Override
    public void setOnDismissListener(@Nullable OnDismissListener onDismissListener) {
        this.clientDismissListener = onDismissListener;
    }

    private boolean prepareShow(@Nullable View anchor) {
        if (!canShow(context) || anchor == null) {
            return false;
        }
        applyHostDimIfNeeded();
        return true;
    }

    private boolean shouldShowCentered() {
        return landscapeCenterEnabled && isLandscape();
    }

    protected final void showCentered(@NonNull View anchor) {
        Activity host = getHostActivity();
        View token = host != null && host.getWindow() != null
                ? host.getWindow().getDecorView()
                : anchor.getRootView();
        super.showAtLocation(token, Gravity.CENTER, 0, 0);
    }

    private void applyHostDimIfNeeded() {
        if (hostDimAlpha <= 0f || hostDimApplied) {
            return;
        }
        Activity host = getHostActivity();
        if (host == null || host.getWindow() == null) {
            return;
        }
        Window window = host.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        savedHostWindowAlpha = lp.alpha;
        lp.alpha = hostDimAlpha;
        window.setAttributes(lp);
        hostDimApplied = true;
    }

    private void clearHostDim() {
        if (!hostDimApplied) {
            return;
        }
        Activity host = getHostActivity();
        if (host != null && host.getWindow() != null) {
            WindowManager.LayoutParams lp = host.getWindow().getAttributes();
            lp.alpha = savedHostWindowAlpha > 0f ? savedHostWindowAlpha : 1f;
            host.getWindow().setAttributes(lp);
        }
        hostDimApplied = false;
    }
}
