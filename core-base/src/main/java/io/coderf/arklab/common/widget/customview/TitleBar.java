package io.coderf.arklab.common.widget.customview;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.utils.common.DensityUtil;

/**
 * 通用标题栏：返回按钮 + 居中标题 + 可选右侧文字操作区。
 * 可在 XML 直接使用，也可通过 {@link #bind(String, OnBackClickListener)} 快速绑定。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2026/7/13 10:15
 */
public class TitleBar extends ConstraintLayout {

    private final AppCompatImageButton backButton;
    private final AppCompatTextView titleView;
    private final AppCompatTextView rightView;

    private OnBackClickListener onBackClickListener;
    private OnRightClickListener onRightClickListener;

    public TitleBar(@NonNull Context context) {
        this(context, null);
    }

    public TitleBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TitleBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.view_title_bar, this, true);
        backButton = findViewById(R.id.title_bar_back);
        titleView = findViewById(R.id.title_bar_title);
        rightView = findViewById(R.id.title_bar_right);

        int defaultHeight = DensityUtil.dp2px(context, 48f);
        setMinimumHeight(defaultHeight);

        backButton.setOnClickListener(v -> {
            if (onBackClickListener != null) {
                onBackClickListener.onBackClick();
                return;
            }
            if (getContext() instanceof Activity activity) {
                activity.onBackPressed();
            }
        });
        rightView.setOnClickListener(v -> {
            if (onRightClickListener != null) {
                onRightClickListener.onRightClick();
            }
        });

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TitleBar);
            CharSequence title = ta.getText(R.styleable.TitleBar_titleBarTitle);
            CharSequence rightText = ta.getText(R.styleable.TitleBar_titleBarRightText);
            int titleColor = ta.getColor(R.styleable.TitleBar_titleBarTitleColor,
                    ContextCompat.getColor(context, R.color.black));
            int rightColor = ta.getColor(R.styleable.TitleBar_titleBarRightTextColor,
                    ContextCompat.getColor(context, R.color.themeColor));
            float titleSize = ta.getDimension(R.styleable.TitleBar_titleBarTitleSize,
                    DensityUtil.sp2px(context, 18f));
            int backIcon = ta.getResourceId(R.styleable.TitleBar_titleBarBackIcon, R.mipmap.icon_fh_black);
            boolean showBack = ta.getBoolean(R.styleable.TitleBar_titleBarShowBack, true);
            ta.recycle();

            titleView.setText(title);
            titleView.setTextColor(titleColor);
            titleView.setTextSize(DensityUtil.px2sp(context,titleSize));
            rightView.setTextColor(rightColor);
            if (rightText != null && rightText.length() > 0) {
                rightView.setText(rightText);
                rightView.setVisibility(VISIBLE);
            }
            backButton.setImageDrawable(ContextCompat.getDrawable(context, backIcon));
            backButton.setVisibility(showBack ? VISIBLE : INVISIBLE);
        } else {
            backButton.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.icon_fh_black));
        }
    }


    public TitleBar setTitle(@Nullable CharSequence title) {
        titleView.setText(title);
        return this;
    }

    public TitleBar setTitle(@StringRes int resId) {
        titleView.setText(resId);
        return this;
    }

    public TitleBar setTitleColor(@ColorInt int color) {
        titleView.setTextColor(color);
        return this;
    }

    public TitleBar setBackIcon(@DrawableRes int resId) {
        backButton.setImageDrawable(ContextCompat.getDrawable(getContext(), resId));
        return this;
    }

    public TitleBar setShowBackButton(boolean show) {
        backButton.setVisibility(show ? VISIBLE : INVISIBLE);
        return this;
    }

    public TitleBar setRightText(@Nullable CharSequence text) {
        rightView.setText(text);
        rightView.setVisibility(text == null || text.length() == 0 ? GONE : VISIBLE);
        return this;
    }

    public TitleBar setRightTextColor(@ColorInt int color) {
        rightView.setTextColor(color);
        return this;
    }

    public TitleBar setOnBackClickListener(@Nullable OnBackClickListener listener) {
        this.onBackClickListener = listener;
        return this;
    }

    public TitleBar setOnRightClickListener(@Nullable OnRightClickListener listener) {
        this.onRightClickListener = listener;
        return this;
    }

    public TitleBar bind(@Nullable CharSequence title, @Nullable OnBackClickListener backListener) {
        setTitle(title);
        setOnBackClickListener(backListener);
        return this;
    }

    public AppCompatTextView getTitleView() {
        return titleView;
    }

    public AppCompatTextView getRightView() {
        return rightView;
    }

    public AppCompatImageButton getBackButton() {
        return backButton;
    }

    public interface OnBackClickListener {
        void onBackClick();
    }

    public interface OnRightClickListener {
        void onRightClick();
    }
}
