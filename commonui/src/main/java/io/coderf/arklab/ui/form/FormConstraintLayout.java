package io.coderf.arklab.ui.form;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.databinding.Observable;
import androidx.databinding.ObservableField;

import com.google.android.material.textfield.TextInputLayout;

import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.common.utils.theme.ThemeAttrs;
import io.coderf.arklab.common.widget.customview.CornerConstraintLayout;
import io.coderf.arklab.ui.R;
import io.coderf.arklab.ui.enums.LabelAlignEnum;
import io.coderf.arklab.ui.enums.LabelTextStyleEnum;
import io.coderf.arklab.ui.enums.LabelVerticalAlignEnum;
import io.coderf.arklab.ui.enums.TextAlignEnum;
import io.coderf.arklab.ui.inter.FormTextWatcher;
import io.coderf.arklab.ui.inter.FormTextWatcherAfter;

/**
 * FormConstraintLayout 类。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @updated 2026/9/1 22:51
 */
public class FormConstraintLayout extends CornerConstraintLayout {
    public static final String TAG = "FormUi";
    /**
     * label文字内容
     */
    protected String labelString;
    /**
     * 输入框、选择框等提示文字
     */
    protected String hintString = "请选择";
    /**
     * 是否必填，也就是是否显示*号
     */
    protected boolean required;
    /**
     * 是否展示底部边框
     */
    protected boolean bottomBorder;
    /**
     * 右侧或者正文也就是输入框、选择框正文文字颜色
     */
    protected int formTextColor;
    /**
     * 右侧或者正文也就是输入框、选择框正文提示文字颜色
     */
    protected int formHintTextColor;
    /**
     * 底部边框颜色
     */
    protected int borderBottomColor;
    /**
     * label文字颜色
     */
    protected int labelTextColor;
    /**
     * 输入框、选择框正文行数
     */
    protected int line;
    /**
     * 输入监听
     */
    public FormTextWatcher formTextWatcher;
    /**
     * 输入监听
     */
    public FormTextWatcherAfter formTextWatcherAfter;
    /**
     * label文字大小
     */
    protected float formLabelTextSize;
    /**
     * 正文文字大小
     */
    protected float formTextSize;
    /**
     * 底部边框与左侧的距离
     */
    protected float borderBottomStartMargin;
    /**
     * 底部边框与右侧的距离
     */
    protected float borderBottomEndMargin;
    /**
     * label左侧margin，默认16dp
     */
    protected float labelStartMargin;
    /**
     * label右侧margin，默认为0
     */
    protected float labelEndMargin;
    /**
     * label文字顶部margin，默认12dp，根据对齐方式生效
     */
    protected float labelTopMargin;
    /**
     * label文字底部部margin，默认12dp，根据对齐方式生效
     */
    protected float labelBottomMargin;
    /**
     * 正文文字与左侧的距离，默认16dp
     */
    protected float textStartMargin;
    /**
     * 正文文字与右侧的距离，默认16dp
     */
    protected float textEndMargin;
    /**
     * 正文文字内容距离上下边框的距离，防止文字多的时候与边框挤在一起
     */
    protected float defaultTextMargin;
    /**
     * 必填*号文字大小
     */
    protected float formRequiredSize;
    /**
     * label对齐方式 是顶部还是左侧，默认为左侧
     */
    protected int labelAlign;
    /**
     * 表单中label文字对齐方式，当对齐方式为左侧时，他的垂直方向对齐方式
     */
    protected int labelVerticalAlign;
    /**
     * 文本对齐方式 是左侧还是右侧，默认为右侧
     */
    protected int textAlign;
    /**
     * label是否加粗，默认不加粗
     */
    protected int labelTextStyle;
    /**
     * label控件
     */
    protected MaterialTextView tvLabel;
    /**
     * 左侧文字的图标控件
     */
    protected ShapeableImageView ivLabelIcon;
    /**
     * 是否展示label左侧图标，默认false
     */
    protected boolean showLabelIcon;
    /**
     * 左侧文字的图标
     */
    protected Drawable labelIcon;
    /**
     * 左侧文字的图标宽高
     */
    protected float labelIconWidth;
    /**
     *左侧文字的图标宽高
     */
    protected float labelIconHeight;
    /**
     * 左侧文字的图标左侧margin
     */
    protected float labelIconStartMargin;
    /**
     * 左侧文字的图标右侧margin
     */
    protected float labelIconEndMargin;
    /**
     * required*号控件的左侧margin
     */
    protected float requiredStartMargin;
    /**
     * 底部边框控件
     */
    protected View vBorderBottom;
    /**
     * 必填*号控件
     */
    protected MaterialTextView tvRequired;
    /**
     * 输入框、选择框正文控件
     */
    protected View tvSelection;
    /**
     * 输入框、选择框正文内容，用于双向绑定
     */
    public final ObservableField<String> dataSource = new ObservableField<>("");
    /** 行内 TextInputLayout，仅输入类表单有值。 */
    @Nullable
    protected TextInputLayout textInputLayout;
    /** 非输入行的校验文案。 */
    @Nullable
    protected MaterialTextView tvError;
    protected String helperText;
    protected boolean counterEnabled;
    @Nullable
    private String errorTextPending;

    public FormConstraintLayout(@NonNull Context context) {
        super(context);
        initAttr(null);
        init();
    }

    public FormConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttr(attrs);
        init();
    }

    public FormConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(attrs);
        init();
    }

    protected void initAttr(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FormUI);
            labelString = typedArray.getString(R.styleable.FormUI_label);
            hintString = typedArray.getString(R.styleable.FormUI_hint);
            formLabelTextSize = typedArray.getDimension(R.styleable.FormUI_formLabelTextSize, DensityUtil.sp2px(getContext(), 14));
            formTextSize = typedArray.getDimension(R.styleable.FormUI_formTextSize, DensityUtil.sp2px(getContext(), 14));

            borderBottomStartMargin = typedArray.getDimension(R.styleable.FormUI_borderBottomStartMargin, DensityUtil.dp2px(getContext(), 16f));
            borderBottomEndMargin = typedArray.getDimension(R.styleable.FormUI_borderBottomEndMargin, 0);

            labelStartMargin = typedArray.getDimension(R.styleable.FormUI_labelStartMargin, DensityUtil.dp2px(getContext(), 16f));
            labelEndMargin = typedArray.getDimension(R.styleable.FormUI_labelEndMargin, 0);

            labelTopMargin = typedArray.getDimension(R.styleable.FormUI_labelTopMargin, DensityUtil.dp2px(getContext(), 12f));
            labelBottomMargin = typedArray.getDimension(R.styleable.FormUI_labelBottomMargin, DensityUtil.dp2px(getContext(), 12f));

            textStartMargin = typedArray.getDimension(R.styleable.FormUI_textStartMargin, DensityUtil.dp2px(getContext(), 12f));
            textEndMargin = typedArray.getDimension(R.styleable.FormUI_textEndMargin, DensityUtil.dp2px(getContext(), 16f));

            defaultTextMargin = typedArray.getDimension(R.styleable.FormUI_defaultTextMargin, DensityUtil.dp2px(getContext(), 12f));

            formRequiredSize = typedArray.getDimension(R.styleable.FormUI_formRequiredSize, DensityUtil.sp2px(getContext(), 14));
            formTextColor = typedArray.getColor(R.styleable.FormUI_formTextColor, ThemeAttrs.onSurface(getContext()));
            formHintTextColor = typedArray.getColor(R.styleable.FormUI_formHintTextColor, ThemeAttrs.onSurfaceVariant(getContext()));
            borderBottomColor = typedArray.getColor(R.styleable.FormUI_borderBottomColor, ThemeAttrs.outlineVariant(getContext()));
            labelTextColor = typedArray.getColor(R.styleable.FormUI_labelTextColor, ThemeAttrs.onSurface(getContext()));
            helperText = typedArray.getString(R.styleable.FormUI_formHelperText);
            counterEnabled = typedArray.getBoolean(R.styleable.FormUI_formCounterEnabled, false);
            if (typedArray.hasValue(R.styleable.FormUI_formErrorText)) {
                errorTextPending = typedArray.getString(R.styleable.FormUI_formErrorText);
            }
            required = typedArray.getBoolean(R.styleable.FormUI_required, false);
            bottomBorder = typedArray.getBoolean(R.styleable.FormUI_bottomBorder, true);
            line = typedArray.getInteger(R.styleable.FormUI_line, 1);
            labelAlign = typedArray.getInt(R.styleable.FormUI_labelAlign, LabelAlignEnum.LEFT.value);
            labelVerticalAlign = typedArray.getInt(R.styleable.FormUI_labelVerticalAlign, LabelVerticalAlignEnum.TOP_TO_VALUE.value);
            textAlign = typedArray.getInt(R.styleable.FormUI_textAlign, TextAlignEnum.RIGHT.value);
            labelTextStyle = typedArray.getInt(R.styleable.FormUI_labelTextStyle, LabelTextStyleEnum.NORMAL.value);

            showLabelIcon = typedArray.getBoolean(R.styleable.FormUI_showLabelIcon, false);
            labelIcon = typedArray.getDrawable(R.styleable.FormUI_labelIcon);
            labelIconWidth = typedArray.getDimension(R.styleable.FormUI_labelIconWidth, 0);
            labelIconHeight = typedArray.getDimension(R.styleable.FormUI_labelIconHeight, 0);
            labelIconStartMargin = typedArray.getDimension(R.styleable.FormUI_labelIconStartMargin, 0);
            labelIconEndMargin = typedArray.getDimension(R.styleable.FormUI_labelIconEndMargin, 0);
            requiredStartMargin = typedArray.getDimension(R.styleable.FormUI_requiredStartMargin, DensityUtil.dp2px(getContext(), 4f));
            typedArray.recycle();
        } else {
            formTextColor = ThemeAttrs.onSurface(getContext());
            formHintTextColor = ThemeAttrs.onSurfaceVariant(getContext());
            labelTextColor = ThemeAttrs.onSurface(getContext());
            borderBottomColor = ThemeAttrs.outlineVariant(getContext());
            formLabelTextSize = DensityUtil.sp2px(getContext(), 14);
            formRequiredSize = DensityUtil.sp2px(getContext(), 14);
            borderBottomStartMargin = DensityUtil.dp2px(getContext(), 16f);
            borderBottomEndMargin = 0;
            labelStartMargin = DensityUtil.dp2px(getContext(), 16f);
            labelEndMargin = 0;
            labelTopMargin= DensityUtil.dp2px(getContext(), 12f);
            labelBottomMargin = DensityUtil.dp2px(getContext(), 12f);
            textStartMargin = DensityUtil.dp2px(getContext(), 12f);
            textEndMargin = DensityUtil.dp2px(getContext(), 16f);
            defaultTextMargin = DensityUtil.dp2px(getContext(), 12f);
            formTextSize = DensityUtil.sp2px(getContext(), 14);
            labelAlign = LabelAlignEnum.LEFT.value;
            textAlign = TextAlignEnum.RIGHT.value;
            labelVerticalAlign = LabelVerticalAlignEnum.TOP_TO_VALUE.value;
            labelTextStyle = LabelTextStyleEnum.NORMAL.value;
            showLabelIcon = false;
            labelIconWidth = DensityUtil.dp2px(getContext(), 0);
            labelIconHeight = DensityUtil.dp2px(getContext(), 0);
            labelIconStartMargin = 0;
            labelIconEndMargin = DensityUtil.dp2px(getContext(), 8);
            requiredStartMargin = DensityUtil.dp2px(getContext(), 4f);
        }
    }

    protected void init() {
        createLabelIcon();
        createLabel();
        createRequired();
        createText();
        createBottomLine();
        layoutLabelIcon();
        layoutLabel();
        layoutRequired();
        layoutText();
        applyInputChrome();
        if (!android.text.TextUtils.isEmpty(errorTextPending)) {
            setError(errorTextPending);
            errorTextPending = null;
        }
        dataSource.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                String newValue = dataSource.get();
                EditText boundEdit = resolveBoundEditText();
                if (boundEdit != null) {
                    String incoming = newValue == null ? "" : newValue;
                    String current = boundEdit.getText() == null ? "" : boundEdit.getText().toString();
                    if (!current.equals(incoming)) {
                        if (boundEdit instanceof AutoCompleteTextView autoComplete) {
                            autoComplete.setText(incoming, false);
                        } else {
                            boundEdit.setText(newValue);
                        }
                    }
                    return;
                }
                if (tvSelection instanceof MaterialTextView textView) {
                    if (textView.getText() == null) {
                        textView.setText(newValue);
                    } else if (!textView.getText().toString().equals(newValue)) {
                        textView.setText(newValue);
                    }
                }
            }
        });
    }

    @Nullable
    protected EditText resolveBoundEditText() {
        if (textInputLayout != null && textInputLayout.getEditText() != null) {
            return textInputLayout.getEditText();
        }
        if (tvSelection instanceof EditText editText) {
            return editText;
        }
        return null;
    }

    protected void applyInputChrome() {
        if (textInputLayout == null) {
            return;
        }
        if (!android.text.TextUtils.isEmpty(helperText)) {
            textInputLayout.setHelperTextEnabled(true);
            textInputLayout.setHelperText(helperText);
        }
        if (counterEnabled) {
            textInputLayout.setCounterEnabled(true);
        }
    }

    /**
     * 校验失败文案。输入行走 TextInputLayout error；其他行走底部分割线变色。
     */
    public void setError(@Nullable CharSequence error) {
        boolean hasError = !android.text.TextUtils.isEmpty(error);
        if (textInputLayout != null) {
            textInputLayout.setErrorEnabled(hasError);
            textInputLayout.setError(hasError ? error : null);
        } else if (tvError != null) {
            tvError.setVisibility(hasError ? View.VISIBLE : View.GONE);
            tvError.setText(error);
        }
        if (vBorderBottom != null) {
            vBorderBottom.setBackgroundColor(hasError
                    ? ThemeAttrs.error(getContext())
                    : borderBottomColor);
        }
    }

    public void setHelperText(@Nullable CharSequence helper) {
        helperText = helper == null ? null : helper.toString();
        if (textInputLayout != null) {
            boolean has = !android.text.TextUtils.isEmpty(helper);
            textInputLayout.setHelperTextEnabled(has);
            textInputLayout.setHelperText(has ? helper : null);
        }
    }

    public void setCounterEnabled(boolean enabled) {
        counterEnabled = enabled;
        if (textInputLayout != null) {
            textInputLayout.setCounterEnabled(enabled);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setAlpha(enabled ? 1f : 0.38f);
        if (tvSelection != null) {
            tvSelection.setEnabled(enabled);
        }
        if (textInputLayout != null) {
            textInputLayout.setEnabled(enabled);
        }
    }

    public MaterialTextView getTvLabel() {
        return tvLabel;
    }

    public View getTvSelection() {
        return tvSelection;
    }

    public MaterialTextView getTvRequired() {
        return tvRequired;
    }

    public View getVBorderBottom() {
        return vBorderBottom;
    }

    public ShapeableImageView getIvLabelIcon() {
        return ivLabelIcon;
    }

    public void createLabelIcon() {
        ivLabelIcon = new ShapeableImageView(getContext());
        ivLabelIcon.setId(View.generateViewId());
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                labelIconWidth <= 0 ? ConstraintLayout.LayoutParams.WRAP_CONTENT : (int) labelIconWidth,
                labelIconHeight <= 0 ? ConstraintLayout.LayoutParams.WRAP_CONTENT : (int) labelIconHeight
        );
        ivLabelIcon.setImageDrawable(labelIcon);
        params.setMarginStart((int) labelIconStartMargin);
        params.setMarginEnd((int) labelIconEndMargin);//这样设置其实没用，因为右侧没有宽度限制
        addView(ivLabelIcon, params);
    }

    public void createLabel() {
        tvLabel = new MaterialTextView(getContext());
        tvLabel.setId(View.generateViewId());
        tvLabel.setLines(1);
        tvLabel.setTextColor(labelTextColor);
        tvLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, formLabelTextSize);
        tvLabel.setText(labelString);
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.setMarginStart((int) labelStartMargin);
        params.setMarginEnd((int) labelEndMargin);
        if (LabelTextStyleEnum.BOLD.value == labelTextStyle) {
            // 设置为加粗
            tvLabel.setTypeface(tvLabel.getTypeface(), Typeface.BOLD);
        } else {
            // 取消加粗（恢复正常）
            tvLabel.setTypeface(tvLabel.getTypeface(), Typeface.NORMAL);
        }
        addView(tvLabel, params);
    }

    public void createRequired() {
        tvRequired = new MaterialTextView(getContext());
        tvRequired.setId(View.generateViewId());
        tvRequired.setLines(1);
        tvRequired.setText("*");
        tvRequired.setVisibility(required ? View.VISIBLE : View.GONE);
        tvRequired.setGravity(android.view.Gravity.CENTER);
        tvRequired.setTextColor(ThemeAttrs.error(getContext()));
        tvRequired.setTextSize(TypedValue.COMPLEX_UNIT_PX, formRequiredSize);

        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.setMarginStart((int) requiredStartMargin);

        addView(tvRequired, params);
    }

    public void createText() {
        MaterialTextView tvText = new MaterialTextView(getContext());
        tvText.setId(View.generateViewId());
        tvText.setHint(hintString);

        tvText.setEllipsize(android.text.TextUtils.TruncateAt.END);
        tvText.setTextColor(formTextColor);
        tvText.setHintTextColor(formHintTextColor);
        tvText.setTextSize(TypedValue.COMPLEX_UNIT_PX, formTextSize);

        // 设置水平权重
        ConstraintLayout.LayoutParams params = null;
        if (LabelAlignEnum.TOP.value == labelAlign) {
            tvText.setGravity(Gravity.START | android.view.Gravity.CENTER_VERTICAL);
            params = new ConstraintLayout.LayoutParams(
                    0, LayoutParams.WRAP_CONTENT);
            params.setMarginStart((int) textEndMargin);
            params.setMarginEnd((int) textEndMargin);
        } else if (LabelAlignEnum.LEFT.value == labelAlign) {
            if (TextAlignEnum.LEFT.value == textAlign) {
                tvText.setGravity(Gravity.START | android.view.Gravity.CENTER_VERTICAL);
            } else {
                tvText.setGravity(Gravity.END | android.view.Gravity.CENTER_VERTICAL);
            }
            params = new ConstraintLayout.LayoutParams(
                    0, LayoutParams.WRAP_CONTENT);
            params.setMarginStart((int) textStartMargin);
            params.setMarginEnd((int) textEndMargin);
            params.horizontalWeight = 1;
        } else {
            params = new ConstraintLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        }
        //如果不是当行显示的话
        params.topMargin = (int) defaultTextMargin;
        params.bottomMargin = (int) defaultTextMargin;
        if (line == 1) {
            tvText.setMaxLines(1);
        } else if (line > 1) {
            tvText.setMaxLines(line);
        } else {
            tvText.setMaxLines(Integer.MAX_VALUE);
        }
        tvSelection = tvText;
        addView(tvSelection, params);
    }

    public void createBottomLine() {
        if (!bottomBorder) {
            //不展示底部边框的情况下
            return;
        }
        vBorderBottom = new View(getContext());
        vBorderBottom.setId(View.generateViewId());
        // 设置布局参数
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                0, DensityUtil.dp2px(getContext(), 1f));
        params.setMarginStart((int) borderBottomStartMargin);
        params.setMarginEnd((int) borderBottomEndMargin);
        vBorderBottom.setBackgroundColor(borderBottomColor);
        addView(vBorderBottom, params);

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this);
        constraintSet.connect(vBorderBottom.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        constraintSet.connect(vBorderBottom.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        constraintSet.connect(vBorderBottom.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        constraintSet.applyTo(this);
    }

    public void layoutLabelIcon() {
        if (!showLabelIcon || labelIcon == null) {
            return;
        }
        if (LabelAlignEnum.TOP.value == labelAlign) {
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(this);
            constraintSet.connect(ivLabelIcon.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            constraintSet.connect(ivLabelIcon.getId(), ConstraintSet.TOP, tvLabel.getId(), ConstraintSet.TOP);
            constraintSet.connect(ivLabelIcon.getId(), ConstraintSet.BOTTOM, tvLabel.getId(), ConstraintSet.BOTTOM);
            constraintSet.applyTo(this);
        } else if (LabelAlignEnum.LEFT.value == labelAlign) {
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(this);
            constraintSet.connect(ivLabelIcon.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            constraintSet.connect(ivLabelIcon.getId(), ConstraintSet.END, tvLabel.getId(), ConstraintSet.START);
            constraintSet.connect(ivLabelIcon.getId(), ConstraintSet.TOP, tvLabel.getId(), ConstraintSet.TOP);
            constraintSet.connect(ivLabelIcon.getId(), ConstraintSet.BOTTOM, tvLabel.getId(), ConstraintSet.BOTTOM);
            constraintSet.applyTo(this);
        }
    }

    public void layoutLabel() {
        if (LabelAlignEnum.TOP.value == labelAlign) {
            if (!showLabelIcon || labelIcon == null) {
                //没有左侧图标，所以直接直连父级
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(this);
                constraintSet.connect(tvLabel.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
                constraintSet.connect(tvLabel.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
                constraintSet.applyTo(this);
                ConstraintLayout.LayoutParams params = (LayoutParams) tvLabel.getLayoutParams();
                params.topMargin = (int) labelTopMargin;
            } else {
                //有左侧图标，所以左侧是图标
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(this);
                constraintSet.connect(tvLabel.getId(), ConstraintSet.START, ivLabelIcon.getId(), ConstraintSet.END);
                constraintSet.connect(tvLabel.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
                constraintSet.applyTo(this);
                ConstraintLayout.LayoutParams params = (LayoutParams) tvLabel.getLayoutParams();
                params.topMargin = (int) labelTopMargin;
            }
        } else if (LabelAlignEnum.LEFT.value == labelAlign) {
            if (!showLabelIcon || labelIcon == null) {
                //没有左侧图标，所以直接直连父级
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(this);
                constraintSet.connect(tvLabel.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
                if (LabelVerticalAlignEnum.TOP.value == labelVerticalAlign) {
                    constraintSet.connect(tvLabel.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, (int) labelTopMargin);
                } else if (LabelVerticalAlignEnum.CENTER.value == labelVerticalAlign) {
                    constraintSet.connect(tvLabel.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, (int) labelBottomMargin);
                    constraintSet.connect(tvLabel.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, (int) labelTopMargin);
                } else if (LabelVerticalAlignEnum.BOTTOM.value == labelVerticalAlign) {
                    constraintSet.connect(tvLabel.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, (int) labelBottomMargin);
                } else {
                    constraintSet.connect(tvLabel.getId(), ConstraintSet.TOP, tvSelection.getId(), ConstraintSet.TOP);
                }
                constraintSet.connect(tvLabel.getId(), ConstraintSet.END, tvRequired.getId(), ConstraintSet.START);
                constraintSet.applyTo(this);
            } else {
                //有左侧图标，所以左侧是图标
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(this);
                constraintSet.connect(tvLabel.getId(), ConstraintSet.START, ivLabelIcon.getId(), ConstraintSet.END);
                if (LabelVerticalAlignEnum.TOP.value == labelVerticalAlign) {
                    constraintSet.connect(tvLabel.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, (int) labelTopMargin);
                } else if (LabelVerticalAlignEnum.CENTER.value == labelVerticalAlign) {
                    constraintSet.connect(tvLabel.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, (int) labelBottomMargin);
                    constraintSet.connect(tvLabel.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, (int) labelTopMargin);
                } else if (LabelVerticalAlignEnum.BOTTOM.value == labelVerticalAlign) {
                    constraintSet.connect(tvLabel.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, (int) labelBottomMargin);
                } else {
                    constraintSet.connect(tvLabel.getId(), ConstraintSet.TOP, tvSelection.getId(), ConstraintSet.TOP);
                }
                constraintSet.connect(tvLabel.getId(), ConstraintSet.END, tvRequired.getId(), ConstraintSet.START);
                constraintSet.applyTo(this);
            }
        } else {

        }
    }

    public void layoutRequired() {
        if (LabelAlignEnum.TOP.value == labelAlign) {
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(this);
            constraintSet.connect(tvRequired.getId(), ConstraintSet.TOP, tvLabel.getId(), ConstraintSet.TOP);
            constraintSet.connect(tvRequired.getId(), ConstraintSet.BOTTOM, tvLabel.getId(), ConstraintSet.BOTTOM);
            constraintSet.connect(tvRequired.getId(), ConstraintSet.START, tvLabel.getId(), ConstraintSet.END);
            constraintSet.applyTo(this);
        } else if (LabelAlignEnum.LEFT.value == labelAlign) {
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(this);
            constraintSet.connect(tvRequired.getId(), ConstraintSet.TOP, tvLabel.getId(), ConstraintSet.TOP);
            constraintSet.connect(tvRequired.getId(), ConstraintSet.BOTTOM, tvLabel.getId(), ConstraintSet.BOTTOM);
            constraintSet.connect(tvRequired.getId(), ConstraintSet.END, tvSelection.getId(), ConstraintSet.START);
            constraintSet.connect(tvRequired.getId(), ConstraintSet.START, tvLabel.getId(), ConstraintSet.END);
            constraintSet.applyTo(this);
        } else {

        }
    }

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

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
        tvRequired.setVisibility(required ? View.VISIBLE : View.GONE);
    }

    public CharSequence getText() {
        return dataSource.get() == null ? "" : dataSource.get();
    }

    public void setText(String text) {
        dataSource.set(text);
    }

    public void setLabel(String text) {
        tvLabel.setText(text);
    }

    public int getLabelAlign() {
        return labelAlign;
    }

    public int getTextAlign() {
        return textAlign;
    }

    /**
     * 运行时切换 label 左侧/顶部对齐，并重新约束子控件。
     */
    public void setLabelAlign(int align) {
        this.labelAlign = align;
        applyAlignLayout();
    }

    /**
     * 运行时切换正文左右对齐（仅 label 在左侧时生效）。
     */
    public void setTextAlign(int align) {
        this.textAlign = align;
        applyAlignLayout();
    }

    /**
     * 一次切换标签对齐与正文对齐，避免连续 apply 两次。
     */
    public void setFormAlign(int labelAlign, int textAlign) {
        this.labelAlign = labelAlign;
        this.textAlign = textAlign;
        applyAlignLayout();
    }

    protected void applyAlignLayout() {
        ConstraintSet clearSet = new ConstraintSet();
        clearSet.clone(this);
        if (ivLabelIcon != null) {
            clearAlignConstraints(clearSet, ivLabelIcon.getId());
        }
        if (tvLabel != null) {
            clearAlignConstraints(clearSet, tvLabel.getId());
        }
        if (tvRequired != null) {
            clearAlignConstraints(clearSet, tvRequired.getId());
        }
        if (tvSelection != null) {
            clearAlignConstraints(clearSet, tvSelection.getId());
            clearSet.setHorizontalBias(tvSelection.getId(), 0.5f);
        }
        clearSet.applyTo(this);
        layoutLabelIcon();
        layoutLabel();
        layoutRequired();
        layoutText();
        restoreLabelChromeParams();
        applySelectionAlignParams();
        requestLayout();
    }

    private static void clearAlignConstraints(ConstraintSet constraintSet, int viewId) {
        constraintSet.clear(viewId, ConstraintSet.START);
        constraintSet.clear(viewId, ConstraintSet.END);
        constraintSet.clear(viewId, ConstraintSet.TOP);
        constraintSet.clear(viewId, ConstraintSet.BOTTOM);
        constraintSet.clear(viewId, ConstraintSet.BASELINE);
    }

    /**
     * ConstraintSet.applyTo 在同时约束 START/END 时会把 wrap 拉成 0。
     * 这里按 createLabel/createRequired 把标签区尺寸和边距还原回去。
     */
    protected void restoreLabelChromeParams() {
        if (tvLabel == null) {
            return;
        }
        ConstraintSet set = new ConstraintSet();
        set.clone(this);
        set.constrainWidth(tvLabel.getId(), ConstraintSet.WRAP_CONTENT);
        set.constrainHeight(tvLabel.getId(), ConstraintSet.WRAP_CONTENT);
        set.setMargin(tvLabel.getId(), ConstraintSet.START, (int) labelStartMargin);
        set.setMargin(tvLabel.getId(), ConstraintSet.END, (int) labelEndMargin);
        if (tvRequired != null) {
            set.constrainWidth(tvRequired.getId(), ConstraintSet.WRAP_CONTENT);
            set.constrainHeight(tvRequired.getId(), ConstraintSet.WRAP_CONTENT);
            set.setMargin(tvRequired.getId(), ConstraintSet.START, (int) requiredStartMargin);
        }
        if (ivLabelIcon != null && showLabelIcon && labelIcon != null) {
            int iconW = labelIconWidth <= 0 ? ConstraintSet.WRAP_CONTENT : (int) labelIconWidth;
            int iconH = labelIconHeight <= 0 ? ConstraintSet.WRAP_CONTENT : (int) labelIconHeight;
            set.constrainWidth(ivLabelIcon.getId(), iconW);
            set.constrainHeight(ivLabelIcon.getId(), iconH);
        }
        set.applyTo(this);
    }

    /**
     * 正文区是否横向铺满（输入/选择类为 true；开关/评分等为 false）。
     */
    protected boolean selectionUsesMatchConstraint() {
        return true;
    }

    /**
     * 按当前 {@link #labelAlign}/{@link #textAlign} 还原 createText 时的宽高、边距与文字方向。
     */
    protected void applySelectionAlignParams() {
        if (tvSelection == null) {
            return;
        }
        LayoutParams params = (LayoutParams) tvSelection.getLayoutParams();
        if (params == null) {
            return;
        }
        boolean top = LabelAlignEnum.TOP.value == labelAlign;
        boolean match = selectionUsesMatchConstraint();
        params.width = match ? 0 : LayoutParams.WRAP_CONTENT;
        params.height = LayoutParams.WRAP_CONTENT;
        params.horizontalWeight = (!top && match) ? 1 : 0;
        params.topMargin = (int) defaultTextMargin;
        params.bottomMargin = (int) defaultTextMargin;
        if (top) {
            params.setMarginStart((int) textEndMargin);
            params.setMarginEnd((int) textEndMargin);
        } else {
            params.setMarginStart((int) textStartMargin);
            params.setMarginEnd((int) textEndMargin);
        }
        tvSelection.setLayoutParams(params);

        int gravity = (top || TextAlignEnum.LEFT.value == textAlign)
                ? Gravity.START | Gravity.CENTER_VERTICAL
                : Gravity.END | Gravity.CENTER_VERTICAL;
        EditText bound = resolveBoundEditText();
        if (bound != null) {
            bound.setGravity(gravity);
        } else if (tvSelection instanceof MaterialTextView textView) {
            textView.setGravity(gravity);
        }
        applySelectionSizeConstraints();
    }

    /**
     * 把 {@link #tvSelection} 当前 LayoutParams 写回 ConstraintSet，避免 applyTo 把 wrap 拉成 0。
     */
    protected void applySelectionSizeConstraints() {
        if (tvSelection == null) {
            return;
        }
        LayoutParams params = (LayoutParams) tvSelection.getLayoutParams();
        if (params == null) {
            return;
        }
        boolean top = LabelAlignEnum.TOP.value == labelAlign;
        ConstraintSet set = new ConstraintSet();
        set.clone(this);
        int width = params.width == 0 ? ConstraintSet.MATCH_CONSTRAINT
                : (params.width == LayoutParams.WRAP_CONTENT ? ConstraintSet.WRAP_CONTENT : params.width);
        int height = params.height == 0 ? ConstraintSet.MATCH_CONSTRAINT
                : (params.height == LayoutParams.WRAP_CONTENT ? ConstraintSet.WRAP_CONTENT : params.height);
        set.constrainWidth(tvSelection.getId(), width);
        set.constrainHeight(tvSelection.getId(), height);
        set.setMargin(tvSelection.getId(), ConstraintSet.START, params.getMarginStart());
        set.setMargin(tvSelection.getId(), ConstraintSet.END, params.getMarginEnd());
        set.setMargin(tvSelection.getId(), ConstraintSet.TOP, params.topMargin);
        set.setMargin(tvSelection.getId(), ConstraintSet.BOTTOM, params.bottomMargin);
        if (!selectionUsesMatchConstraint()) {
            set.setHorizontalBias(tvSelection.getId(), top ? 0f : 1f);
        }
        set.applyTo(this);
    }

    /**
     * 推荐使用这个
     */
    public void addTextChangedListener(FormTextWatcher formTextWatcher) {
        this.formTextWatcher = formTextWatcher;
    }

    /**
     * 推荐使用这个
     */
    public void addTextChangedAfterListener(FormTextWatcherAfter formTextWatcherAfter) {
        this.formTextWatcherAfter = formTextWatcherAfter;
    }

}
