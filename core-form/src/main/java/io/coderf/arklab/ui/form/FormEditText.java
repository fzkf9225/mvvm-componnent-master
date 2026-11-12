package io.coderf.arklab.ui.form;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import io.coderf.arklab.common.utils.theme.ThemeAttrs;

import java.util.ArrayList;
import java.util.List;

import io.coderf.arklab.core.utils.DensityUtil;
import io.coderf.arklab.core.utils.StringUtil;
import io.coderf.arklab.ui.R;
import io.coderf.arklab.ui.enums.FormTextFormatEnum;
import io.coderf.arklab.ui.enums.LabelAlignEnum;
import io.coderf.arklab.ui.enums.TextAlignEnum;
import io.coderf.arklab.ui.impl.DecimalDigitsInputFilter;
import io.coderf.arklab.ui.impl.FormTextFormatter;

/**
 * FormEditText 类。
 * <p>
 * 支持可配置的清空图标（compound drawable，不改动现有 padding / gravity / TextInputLayout 排版）：
 * <ul>
 *   <li>{@code app:showClearIcon} — 是否启用，默认 true</li>
 *   <li>{@code app:clearIcon} — 图标资源，默认 {@code icon_clear}</li>
 *   <li>{@code app:clearIconSize} — 图标尺寸，0 表示 intrinsic</li>
 *   <li>{@code app:clearIconPadding} — 图标与文字间距</li>
 *   <li>{@code app:clearIconAlwaysShow} — 是否常驻；默认 false（有内容且获焦时显示）</li>
 * </ul>
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2023/12/26 16:27
 */
public class FormEditText extends FormConstraintLayout {
    /**
     * 输入框类型
     */
    protected int inputType;
    /**
     * 回车按钮样式
     */
    protected int imeOptions;
    /**
     * 输入框格式；{@link FormTextFormatEnum#NORMAL} 时不做格式化。
     */
    protected int formatText;
    /**
     * 可输入最大长度，默认为-1，不限制
     */
    protected int maxLength;
    /**
     * 小数位，只有在小数的时候才有效，所以请配合输入类型使用
     */
    protected int digits;
    /**
     * 输入框背景
     */
    protected Drawable inputDrawable;
    /**
     * 输入框高度
     */
    protected float inputHeight;
    /**
     * 输入框控件引用
     */
    protected TextInputEditText editText;

    /** 是否启用清空图标 */
    protected boolean showClearIcon = true;
    /** 清空图标 */
    @Nullable
    protected Drawable clearIconDrawable;
    /** 清空图标尺寸（px），0 表示 intrinsic */
    protected int clearIconSizePx;
    /** 清空图标与文字间距（px），&lt;0 表示使用默认 8dp */
    protected int clearIconPaddingPx = -1;
    /**
     * 清空图标是否常驻。
     * false（默认）：有内容且获焦时显示；true：有内容即显示。
     */
    protected boolean clearIconAlwaysShow = false;

    public FormEditText(@NonNull Context context) {
        super(context);
    }

    public FormEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FormEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initAttr(AttributeSet attrs) {
        super.initAttr(attrs);
        TypedArray typedArray = obtainFormUi(attrs);
        try {
            inputType = resolveAndroidInputType(attrs, InputType.TYPE_CLASS_TEXT);
            imeOptions = resolveAndroidImeOptions(attrs, EditorInfo.IME_ACTION_NEXT);
            formatText = typedArray.getInt(R.styleable.FormUI_formatText, 0);
            maxLength = typedArray.getInt(R.styleable.FormUI_maxLength, -1);
            digits = typedArray.getInt(R.styleable.FormUI_digits, -1);
            inputDrawable = typedArray.getDrawable(R.styleable.FormUI_inputDrawable);
            inputHeight = typedArray.getDimension(R.styleable.FormUI_inputHeight, DensityUtil.dp2px(getContext(), 35f));
            showClearIcon = typedArray.getBoolean(R.styleable.FormUI_showClearIcon, true);
            clearIconDrawable = typedArray.getDrawable(R.styleable.FormUI_clearIcon);
            clearIconSizePx = (int) typedArray.getDimension(R.styleable.FormUI_clearIconSize, 0);
            clearIconAlwaysShow = typedArray.getBoolean(R.styleable.FormUI_clearIconAlwaysShow, false);
            clearIconPaddingPx = (int) typedArray.getDimension(R.styleable.FormUI_clearIconPadding, -1);
        } finally {
            typedArray.recycle();
        }
    }

    private int resolveAndroidInputType(@Nullable AttributeSet attrs, int fallback) {
        if (attrs == null) {
            return fallback;
        }
        TypedArray a = getContext().obtainStyledAttributes(attrs, new int[]{android.R.attr.inputType});
        try {
            return a.getInt(0, fallback);
        } finally {
            a.recycle();
        }
    }

    private int resolveAndroidImeOptions(@Nullable AttributeSet attrs, int fallback) {
        if (attrs == null) {
            return fallback;
        }
        TypedArray a = getContext().obtainStyledAttributes(attrs, new int[]{android.R.attr.imeOptions});
        try {
            return a.getInt(0, fallback);
        } finally {
            a.recycle();
        }
    }

    @Override
    public void createLabel() {
        super.createLabel();
        if (LabelAlignEnum.LEFT.value == labelAlign) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) tvLabel.getLayoutParams();
            params.topMargin = (int) defaultTextMargin;
        }
    }

    @Override
    public void layoutLabel() {
        if (LabelAlignEnum.TOP.value == labelAlign) {
            if (!showLabelIcon || labelIcon == null) {
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(this);
                constraintSet.connect(tvLabel.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
                constraintSet.connect(tvLabel.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
                constraintSet.applyTo(this);
                ConstraintLayout.LayoutParams params = (LayoutParams) tvLabel.getLayoutParams();
                params.topMargin = (int) defaultTextMargin;
            } else {
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(this);
                constraintSet.connect(tvLabel.getId(), ConstraintSet.START, ivLabelIcon.getId(), ConstraintSet.END);
                constraintSet.connect(tvLabel.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
                constraintSet.applyTo(this);
                ConstraintLayout.LayoutParams params = (LayoutParams) tvLabel.getLayoutParams();
                params.topMargin = (int) defaultTextMargin;
            }
        } else if (LabelAlignEnum.LEFT.value == labelAlign) {
            if (!showLabelIcon || labelIcon == null) {
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(this);
                constraintSet.connect(tvLabel.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
                constraintSet.connect(tvLabel.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
                constraintSet.connect(tvLabel.getId(), ConstraintSet.END, tvRequired.getId(), ConstraintSet.START);
                constraintSet.applyTo(this);
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) tvLabel.getLayoutParams();
                params.topMargin = (int) defaultTextMargin;
            } else {
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(this);
                constraintSet.connect(tvLabel.getId(), ConstraintSet.START,  ivLabelIcon.getId(), ConstraintSet.END);
                constraintSet.connect(tvLabel.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
                constraintSet.connect(tvLabel.getId(), ConstraintSet.END, tvRequired.getId(), ConstraintSet.START);
                constraintSet.applyTo(this);
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) tvLabel.getLayoutParams();
                params.topMargin = (int) defaultTextMargin;
            }
        } else {

        }
    }

    @Override
    public void createText() {
        editText = new TextInputEditText(getContext()) {
            @Override
            protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
                super.onFocusChanged(focused, direction, previouslyFocusedRect);
                updateClearCompoundDrawables(getText());
            }

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouchEvent(MotionEvent event) {
                if (handleClearIconTouch(event)) {
                    return true;
                }
                return super.onTouchEvent(event);
            }
        };
        editText.setId(View.generateViewId());
        editText.setHint(hintString);
        editText.setHintTextColor(formHintTextColor != 0 ? formHintTextColor : ThemeAttrs.onSurfaceVariant(getContext()));
        editText.setBackground(null);
        if (inputDrawable != null) {
            editText.setBackground(inputDrawable);
        } else {
            editText.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        }
        editText.setEllipsize(android.text.TextUtils.TruncateAt.END);
        editText.setTextColor(formTextColor);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, formTextSize);
        editText.setImeOptions(imeOptions);
        editText.setInputType(inputType);
        // 创建过滤器列表
        List<InputFilter> filters = new ArrayList<>();

        // 添加小数位数过滤器（如果有设置）
        if (digits > 0) {
            filters.add(new DecimalDigitsInputFilter(digits));
        }
        // 添加长度限制过滤器（如果有设置）
        if (maxLength > 0) {
            filters.add(new InputFilter.LengthFilter(maxLength));
        }
        // 将多个过滤器合并设置（不会覆盖已有过滤器）
        if (!filters.isEmpty()) {
            editText.setFilters(filters.toArray(new InputFilter[0]));
        }
        FormTextFormatter.apply(editText, FormTextFormatEnum.fromValue(formatText), digits, null);
        tvSelection = editText;
        ConstraintLayout.LayoutParams params;
        if (LabelAlignEnum.TOP.value == labelAlign) {
            editText.setPadding(0, 0, 0, 0);
            editText.setGravity(Gravity.START | android.view.Gravity.CENTER_VERTICAL);
            params = new ConstraintLayout.LayoutParams(
                    0, (int) inputHeight);
            params.setMarginStart((int) textEndMargin);
            params.setMarginEnd((int) textEndMargin);
        } else if (LabelAlignEnum.LEFT.value == labelAlign) {
            editText.setPadding(0, (int) defaultTextMargin, 0, (int) defaultTextMargin);
            editText.setGravity(android.view.Gravity.END | android.view.Gravity.CENTER_VERTICAL);
            params = new ConstraintLayout.LayoutParams(
                    0, LayoutParams.WRAP_CONTENT);
            params.setMarginStart((int) textStartMargin);
            params.setMarginEnd((int) textEndMargin);
            params.horizontalWeight = 1;
        } else {
            params = new ConstraintLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        }
        editText.setLines(1);
        setupClearIcon();
        TextInputLayout til = FormTextInputLayouts.wrap(getContext(), editText);
        til.setId(View.generateViewId());
        if (maxLength > 0) {
            til.setCounterMaxLength(maxLength);
        }
        textInputLayout = til;
        tvSelection = til;
        addView(til, params);
        applyEditPaddingAndGravity();
        // wrap/compact 会重置 EditText，补一次清空图标状态（未显示时与原先一致）
        updateClearCompoundDrawables(editText.getText());
    }

    /**
     * 初始化清空图标资源与监听；不改动原有 padding / gravity。
     * 监听始终注册，显隐由 {@link #showClearIcon} 控制，便于运行时开关。
     */
    private void setupClearIcon() {
        if (editText == null) {
            return;
        }
        ensureClearIconResources();
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateClearCompoundDrawables(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void ensureClearIconResources() {
        if (!showClearIcon || editText == null) {
            return;
        }
        if (clearIconDrawable == null) {
            clearIconDrawable = ContextCompat.getDrawable(getContext(),
                    io.coderf.arklab.common.R.mipmap.icon_clear);
        }
        if (clearIconDrawable != null) {
            clearIconDrawable = clearIconDrawable.mutate();
        }
        if (clearIconPaddingPx < 0) {
            clearIconPaddingPx = DensityUtil.dp2px(getContext(), 8);
        }
        editText.setCompoundDrawablePadding(clearIconPaddingPx);
    }

    private boolean shouldShowClearIcon(@Nullable CharSequence text) {
        if (!showClearIcon || clearIconDrawable == null) {
            return false;
        }
        boolean hasText = text != null && !StringUtil.isEmpty(text.toString());
        if (!hasText) {
            return false;
        }
        return clearIconAlwaysShow || (editText != null && editText.hasFocus());
    }

    @Nullable
    private Drawable sizeClearDrawable() {
        if (clearIconDrawable == null) {
            return null;
        }
        Drawable d = clearIconDrawable.mutate();
        if (clearIconSizePx > 0) {
            d.setBounds(0, 0, clearIconSizePx, clearIconSizePx);
        } else {
            int w = d.getIntrinsicWidth() > 0 ? d.getIntrinsicWidth()
                    : DensityUtil.dp2px(getContext(), 24);
            int h = d.getIntrinsicHeight() > 0 ? d.getIntrinsicHeight()
                    : DensityUtil.dp2px(getContext(), 24);
            d.setBounds(0, 0, w, h);
        }
        return d;
    }

    private void updateClearCompoundDrawables(@Nullable CharSequence text) {
        if (editText == null) {
            return;
        }
        if (!showClearIcon) {
            return;
        }
        Drawable end = shouldShowClearIcon(text) ? sizeClearDrawable() : null;
        // 仅管理 end，不碰 start/top/bottom，避免影响现有排版能力
        Drawable[] existing = editText.getCompoundDrawablesRelative();
        editText.setCompoundDrawablesRelative(existing[0], existing[1], end, existing[3]);
    }

    private boolean isTouchOnClearIcon(@NonNull MotionEvent event) {
        if (editText == null || !shouldShowClearIcon(editText.getText())) {
            return false;
        }
        Drawable[] drawables = editText.getCompoundDrawablesRelative();
        Drawable clear = drawables[2];
        if (clear == null) {
            return false;
        }
        int x = (int) event.getX();
        int clearWidth = clear.getBounds().width();
        int touchLeft = editText.getWidth() - editText.getPaddingEnd()
                - clearWidth - Math.max(0, clearIconPaddingPx);
        return x >= touchLeft && x <= editText.getWidth();
    }

    private boolean handleClearIconTouch(@NonNull MotionEvent event) {
        if (!isTouchOnClearIcon(event)) {
            return false;
        }
        int action = event.getAction();
        // DOWN 消费掉，避免光标跳到末尾；UP 时清空
        if (action == MotionEvent.ACTION_DOWN) {
            return true;
        }
        if (action == MotionEvent.ACTION_UP) {
            editText.setText("");
            return true;
        }
        return action == MotionEvent.ACTION_MOVE;
    }

    @NonNull
    public TextInputEditText getEditText() {
        return editText;
    }

    private void applyEditPaddingAndGravity() {
        if (editText == null) {
            return;
        }
        if (inputDrawable != null) {
            editText.setBackground(inputDrawable);
        }
        if (LabelAlignEnum.TOP.value == labelAlign) {
            editText.setPadding(0, 0, 0, 0);
            editText.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            return;
        }
        int vertical = (int) defaultTextMargin;
        editText.setPadding(0, vertical, 0, vertical);
        if (TextAlignEnum.LEFT.value == textAlign) {
            editText.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        } else {
            editText.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        }
    }

    @Override
    public void layoutText() {
        if (LabelAlignEnum.TOP.value == labelAlign) {
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(this);
            constraintSet.connect(tvSelection.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            constraintSet.connect(tvSelection.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
            constraintSet.connect(tvSelection.getId(), ConstraintSet.TOP, tvLabel.getId(), ConstraintSet.BOTTOM);
            constraintSet.connect(tvSelection.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
            constraintSet.applyTo(this);
        } else if (LabelAlignEnum.LEFT.value == labelAlign) {
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(this);
            constraintSet.connect(tvSelection.getId(), ConstraintSet.START, tvRequired.getId(), ConstraintSet.END);
            constraintSet.connect(tvSelection.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
            constraintSet.connect(tvSelection.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            constraintSet.connect(tvSelection.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
            constraintSet.applyTo(this);
        } else {

        }
    }

    @Override
    protected void applySelectionAlignParams() {
        super.applySelectionAlignParams();
        if (editText == null || tvSelection == null) {
            return;
        }
        LayoutParams params = (LayoutParams) tvSelection.getLayoutParams();
        if (params == null) {
            return;
        }
        boolean top = LabelAlignEnum.TOP.value == labelAlign;
        params.width = 0;
        if (top) {
            params.height = (int) inputHeight;
            params.topMargin = 0;
            params.bottomMargin = 0;
            params.horizontalWeight = 0;
            params.setMarginStart((int) textEndMargin);
            params.setMarginEnd((int) textEndMargin);
        } else {
            params.height = LayoutParams.WRAP_CONTENT;
            params.topMargin = 0;
            params.bottomMargin = 0;
            params.horizontalWeight = 1;
            params.setMarginStart((int) textStartMargin);
            params.setMarginEnd((int) textEndMargin);
        }
        applyEditPaddingAndGravity();
        tvSelection.setLayoutParams(params);
        applySelectionSizeConstraints();
        // padding 重设后补一次清空图标（隐藏时仍为 null，视觉与原先一致）
        if (showClearIcon) {
            if (clearIconPaddingPx >= 0) {
                editText.setCompoundDrawablePadding(clearIconPaddingPx);
            }
            updateClearCompoundDrawables(editText.getText());
        }
    }

    /**
     * 不要使用这个因为会导致databinding双向绑定无效
     */
    public void addTextChangedListener(TextWatcher watcher) {
        editText.addTextChangedListener(watcher);
    }

    public void setShowClearIcon(boolean show) {
        this.showClearIcon = show;
        if (!show && editText != null) {
            Drawable[] existing = editText.getCompoundDrawablesRelative();
            editText.setCompoundDrawablesRelative(existing[0], existing[1], null, existing[3]);
            return;
        }
        ensureClearIconResources();
        if (editText != null) {
            updateClearCompoundDrawables(editText.getText());
        }
    }

    public boolean isShowClearIcon() {
        return showClearIcon;
    }

    public void setClearIcon(@Nullable Drawable drawable) {
        this.clearIconDrawable = drawable != null ? drawable.mutate() : null;
        if (editText != null) {
            updateClearCompoundDrawables(editText.getText());
        }
    }

    public void setClearIconSize(int sizePx) {
        this.clearIconSizePx = Math.max(0, sizePx);
        if (editText != null) {
            updateClearCompoundDrawables(editText.getText());
        }
    }

    public void setClearIconAlwaysShow(boolean alwaysShow) {
        this.clearIconAlwaysShow = alwaysShow;
        if (editText != null) {
            updateClearCompoundDrawables(editText.getText());
        }
    }

    public boolean isClearIconAlwaysShow() {
        return clearIconAlwaysShow;
    }

    /**
     * 设置清空图标与文字间距（px）。
     */
    public void setClearIconPadding(int paddingPx) {
        this.clearIconPaddingPx = Math.max(0, paddingPx);
        if (editText != null) {
            editText.setCompoundDrawablePadding(this.clearIconPaddingPx);
            updateClearCompoundDrawables(editText.getText());
        }
    }
}
