package io.coderf.arklab.common.widget.recyclerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
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
 * 承载 {@link RecyclerView} 与 {@link EmptyLayout} 的容器，叠放并统一内容区背景，
 * 适用于任意带空态的列表页（分页、Smart 刷新、SwipeRefresh 等），与是否使用 Paging 无关。
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
        LayoutInflater.from(context).inflate(R.layout.merge_recycler_empty_host, this, true);
        recyclerView = findViewById(R.id.mRecyclerview);
        emptyLayout = findViewById(R.id.mEmptyLayout);

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

    /**
     * 使用颜色或 drawable 资源设置列表区域与占位层背景（与原先双控件分别设背景一致）。
     */
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

    /**
     * 使用主题中的颜色资源解析为 {@link ColorInt} 后设置背景。
     */
    public void setContentBackgroundColorRes(int colorRes) {
        setContentBackgroundColor(ContextCompat.getColor(getContext(), colorRes));
    }
}
