package io.coderf.arklab.common.widget.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.utils.theme.ThemeAttrs;

/**
 * 通用标题栏：MaterialToolbar + 居中标题 + 可选右侧文字操作。
 * 对外 API 与历史 TitleBar 一致，内部走官方 Top App Bar。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
public class TitleBar extends MaterialToolbar {

    private final MaterialButton rightButton;

    private OnBackClickListener onBackClickListener;
    private OnRightClickListener onRightClickListener;

    public TitleBar(@NonNull Context context) {
        this(context, null);
    }

    public TitleBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, androidx.appcompat.R.attr.toolbarStyle);
    }

    public TitleBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTitleCentered(true);
        setMinimumHeight(DensityUtil.dp2px(context, 48f));
        setContentInsetsRelative(0, 0);
        setContentInsetStartWithNavigation(0);

        // 主题把 borderlessButtonStyle 映射到 Widget.Material3.Button.TextButton
        rightButton = new MaterialButton(context, null, androidx.appcompat.R.attr.borderlessButtonStyle);
        rightButton.setId(R.id.title_bar_right);
        rightButton.setInsetTop(0);
        rightButton.setInsetBottom(0);
        rightButton.setMinWidth(DensityUtil.dp2px(context, 48f));
        rightButton.setMinimumWidth(DensityUtil.dp2px(context, 48f));
        rightButton.setMinHeight(DensityUtil.dp2px(context, 48f));
        rightButton.setTextSize(15f);
        rightButton.setAllCaps(false);
        rightButton.setPadding(
                DensityUtil.dp2px(context, 12f),
                0,
                DensityUtil.dp2px(context, 12f),
                0);
        rightButton.setVisibility(GONE);
        LayoutParams rightLp = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT,
                Gravity.END | Gravity.CENTER_VERTICAL);
        addView(rightButton, rightLp);

        setNavigationOnClickListener(v -> {
            if (onBackClickListener != null) {
                onBackClickListener.onBackClick();
                return;
            }
            if (getContext() instanceof AppCompatActivity activity) {
                activity.getOnBackPressedDispatcher().onBackPressed();
            }
        });
        rightButton.setOnClickListener(v -> {
            if (onRightClickListener != null) {
                onRightClickListener.onRightClick();
            }
        });

        int defaultOnSurface = ThemeAttrs.onSurface(context);
        int defaultPrimary = ThemeAttrs.primary(context);
        setTitleTextColor(defaultOnSurface);
        setNavigationIconTint(defaultOnSurface);
        rightButton.setTextColor(defaultPrimary);

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TitleBar);
            CharSequence title = ta.getText(R.styleable.TitleBar_titleBarTitle);
            CharSequence rightText = ta.getText(R.styleable.TitleBar_titleBarRightText);
            int titleColor = ta.getColor(R.styleable.TitleBar_titleBarTitleColor, defaultOnSurface);
            int rightColor = ta.getColor(R.styleable.TitleBar_titleBarRightTextColor, defaultPrimary);
            float titleSize = ta.getDimension(R.styleable.TitleBar_titleBarTitleSize,
                    DensityUtil.sp2px(context, 18f));
            int backIcon = ta.getResourceId(R.styleable.TitleBar_titleBarBackIcon, R.drawable.icon_fh);
            boolean showBack = ta.getBoolean(R.styleable.TitleBar_titleBarShowBack, true);
            ta.recycle();

            setTitle(title);
            setTitleTextColor(titleColor);
            applyTitleTextSizePx(titleSize);
            rightButton.setTextColor(rightColor);
            if (rightText != null && rightText.length() > 0) {
                rightButton.setText(rightText);
                rightButton.setVisibility(VISIBLE);
            }
            if (showBack) {
                setNavigationIcon(backIcon);
                setNavigationIconTint(titleColor);
            } else {
                setNavigationIcon(null);
            }
        } else {
            setNavigationIcon(R.drawable.icon_fh);
        }
    }

    private void applyTitleTextSizePx(float sizePx) {
        post(() -> {
            TextView titleView = findTitleTextView();
            if (titleView != null) {
                titleView.setTextSize(DensityUtil.px2sp(getContext(), sizePx));
            }
        });
    }

    @Nullable
    private TextView findTitleTextView() {
        CharSequence title = getTitle();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof TextView tv && child != rightButton) {
                if (title == null || title.equals(tv.getText())) {
                    return tv;
                }
            }
        }
        return null;
    }

    public TitleBar setTitleColor(@ColorInt int color) {
        setTitleTextColor(color);
        return this;
    }

    public TitleBar setBackIcon(@DrawableRes int resId) {
        setNavigationIcon(resId);
        return this;
    }

    public TitleBar setBackIconTint(@ColorInt int color) {
        setNavigationIconTint(color);
        return this;
    }

    public TitleBar setShowBackButton(boolean show) {
        if (show) {
            if (getNavigationIcon() == null) {
                setNavigationIcon(R.drawable.icon_fh);
            }
        } else {
            setNavigationIcon(null);
        }
        return this;
    }

    public TitleBar setRightText(@Nullable CharSequence text) {
        rightButton.setText(text);
        rightButton.setVisibility(text == null || text.length() == 0 ? GONE : VISIBLE);
        return this;
    }

    public TitleBar setRightTextColor(@ColorInt int color) {
        rightButton.setTextColor(color);
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

    @Nullable
    public TextView getTitleView() {
        return findTitleTextView();
    }

    public TextView getRightView() {
        return rightButton;
    }

    @Nullable
    public ImageButton getBackButton() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof ImageButton) {
                return (ImageButton) child;
            }
        }
        return null;
    }

    public interface OnBackClickListener {
        void onBackClick();
    }

    public interface OnRightClickListener {
        void onRightClick();
    }
}
