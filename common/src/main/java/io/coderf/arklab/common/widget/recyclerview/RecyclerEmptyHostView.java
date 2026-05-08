package io.coderf.arklab.common.widget.recyclerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.widget.empty.EmptyLayout;

/**
 * 与 {@code base_smart_paging.xml} 中列表区域等价的容器：{@link FrameLayout} 内自下而上叠放
 * {@link RecyclerView} 与 {@link EmptyLayout}，子 View 均通过代码创建（不依赖 merge 布局）。
 * <p>
 * 供新业务按需引用；基类与现有 XML 布局保持不变。
 */
public class RecyclerEmptyHostView extends FrameLayout {

    private final RecyclerView recyclerView;
    private final EmptyLayout emptyLayout;

    public RecyclerEmptyHostView(@NonNull Context context) {
        this(context, null);
    }

    public RecyclerEmptyHostView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecyclerEmptyHostView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        recyclerView = new RecyclerView(context);
        recyclerView.setId(R.id.mRecyclerview);
        recyclerView.setOverScrollMode(OVER_SCROLL_NEVER);
        FrameLayout.LayoutParams rvLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        addView(recyclerView, rvLp);

        emptyLayout = new EmptyLayout(context);
        emptyLayout.setId(R.id.mEmptyLayout);
        FrameLayout.LayoutParams emptyLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        emptyLp.gravity = android.view.Gravity.CENTER;
        addView(emptyLayout, emptyLp);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RecyclerEmptyHostView, defStyleAttr, 0);
        int bgRes = a.getResourceId(R.styleable.RecyclerEmptyHostView_recyclerEmptyBackground, 0);
        a.recycle();
        if (bgRes != 0) {
            setContentBackgroundResource(bgRes);
        } else {
            setContentBackgroundResource(R.color.default_background);
        }
    }

    @NonNull
    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    @NonNull
    public EmptyLayout getEmptyLayout() {
        return emptyLayout;
    }

    public void setContentBackgroundResource(@DrawableRes int resId) {
        setBackgroundResource(resId);
        emptyLayout.setBackgroundResource(resId);
    }

    public void setContentBackgroundColor(@ColorInt int color) {
        setBackgroundColor(color);
        emptyLayout.setBackgroundColor(color);
    }

    public void setContentBackground(@Nullable Drawable background) {
        setBackground(background);
        if (background == null) {
            emptyLayout.setBackground(null);
            return;
        }
        Drawable.ConstantState state = background.getConstantState();
        emptyLayout.setBackground(state != null
                ? state.newDrawable(getResources()).mutate()
                : background.mutate());
    }

    public void setContentBackgroundColorRes(int colorRes) {
        setContentBackgroundColor(ContextCompat.getColor(getContext(), colorRes));
    }
}
