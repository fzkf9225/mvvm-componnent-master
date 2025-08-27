package com.casic.titan.commonui.form;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.databinding.Observable;
import androidx.databinding.ObservableField;

import com.casic.titan.commonui.R;
import com.casic.titan.commonui.enums.LabelAlignEnum;
import com.casic.titan.commonui.enums.LabelTextStyleEnum;
import com.casic.titan.commonui.enums.TextAlignEnum;
import com.casic.titan.commonui.inter.FormTextWatcher;
import com.casic.titan.commonui.inter.FormTextWatcherAfter;

import pers.fz.mvvm.util.common.DensityUtil;

public class FormConstraintLayout extends ConstraintLayout {
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
    protected boolean required = false;
    /**
     * 是否展示底部边框
     */
    protected boolean bottomBorder = true;
    /**
     * 右侧或者正文也就是输入框、选择框正文文字颜色
     */
    protected int formTextColor;
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
    protected int line = 1;
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
    protected int labelAlign = LabelAlignEnum.LEFT.value;
    /**
     * 文本对齐方式 是左侧还是右侧，默认为右侧
     */
    protected int textAlign = TextAlignEnum.RIGHT.value;
    /**
     * label是否加粗，默认不加粗
     */
    protected int labelTextStyle = LabelTextStyleEnum.NORMAL.value;
    /**
     * label控件
     */
    protected AppCompatTextView tvLabel;
    /**
     * 底部边框控件
     */
    protected View vBorderBottom;
    /**
     * 必填*号控件
     */
    protected AppCompatTextView tvRequired;
    /**
     * 输入框、选择框正文控件
     */
    protected View tvSelection;
    /**
     * 输入框、选择框正文内容，用于双向绑定
     */
    public final ObservableField<String> dataSource = new ObservableField<>("");

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

            textStartMargin = typedArray.getDimension(R.styleable.FormUI_textStartMargin, DensityUtil.dp2px(getContext(), 12f));
            textEndMargin = typedArray.getDimension(R.styleable.FormUI_textEndMargin, DensityUtil.dp2px(getContext(), 16f));

            defaultTextMargin = typedArray.getDimension(R.styleable.FormUI_defaultTextMargin, DensityUtil.dp2px(getContext(), 12f));

            formRequiredSize = typedArray.getDimension(R.styleable.FormUI_formRequiredSize, DensityUtil.sp2px(getContext(), 14));
            formTextColor = typedArray.getColor(R.styleable.FormUI_formTextColor, ContextCompat.getColor(getContext(), R.color.auto_color));
            borderBottomColor = typedArray.getColor(R.styleable.FormUI_borderBottomColor, ContextCompat.getColor(getContext(), R.color.line));
            labelTextColor = typedArray.getColor(R.styleable.FormUI_labelTextColor, ContextCompat.getColor(getContext(), R.color.auto_color));
            required = typedArray.getBoolean(R.styleable.FormUI_required, false);
            bottomBorder = typedArray.getBoolean(R.styleable.FormUI_bottomBorder, true);
            line = typedArray.getInteger(R.styleable.FormUI_line, 1);
            labelAlign = typedArray.getInt(R.styleable.FormUI_labelAlign, LabelAlignEnum.LEFT.value);
            textAlign = typedArray.getInt(R.styleable.FormUI_textAlign, TextAlignEnum.RIGHT.value);
            labelTextStyle = typedArray.getInt(R.styleable.FormUI_labelTextStyle, LabelTextStyleEnum.NORMAL.value);
            typedArray.recycle();
        } else {
            formTextColor = ContextCompat.getColor(getContext(), R.color.auto_color);
            labelTextColor = ContextCompat.getColor(getContext(), R.color.auto_color);
            borderBottomColor = ContextCompat.getColor(getContext(), R.color.line);
            formLabelTextSize = DensityUtil.sp2px(getContext(), 14);
            formRequiredSize = DensityUtil.sp2px(getContext(), 14);
            borderBottomStartMargin = DensityUtil.dp2px(getContext(), 16f);
            borderBottomEndMargin = 0;
            labelStartMargin = DensityUtil.dp2px(getContext(), 16f);
            labelEndMargin = 0;
            textStartMargin = DensityUtil.dp2px(getContext(), 12f);
            textEndMargin = DensityUtil.dp2px(getContext(), 16f);
            defaultTextMargin = DensityUtil.dp2px(getContext(), 12f);
            formTextSize = DensityUtil.sp2px(getContext(), 14);
            labelAlign = LabelAlignEnum.LEFT.value;
            textAlign = TextAlignEnum.RIGHT.value;
            labelTextStyle = LabelTextStyleEnum.NORMAL.value;
        }
    }

    protected void init() {
        createLabel();
        createRequired();
        createText();
        createBottomLine();
        layoutLabel();
        layoutRequired();
        layoutText();
        dataSource.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                String newValue = dataSource.get();
                if (tvSelection instanceof AppCompatTextView textView) {
                    if (textView.getText() == null) {
                        textView.setText(newValue);
                    } else if (!textView.getText().toString().equals(newValue)) {
                        textView.setText(newValue);
                    }
                } else if (tvSelection instanceof AppCompatEditText editText) {
                    if (editText.getText() == null) {
                        editText.setText(newValue);
                    } else if (!editText.getText().toString().equals(newValue)) {
                        editText.setText(newValue);
                    }
                }
            }
        });
    }

    public AppCompatTextView getTvLabel() {
        return tvLabel;
    }

    public View getTvSelection() {
        return tvSelection;
    }

    public AppCompatTextView getTvRequired() {
        return tvRequired;
    }

    public View getVBorderBottom() {
        return vBorderBottom;
    }

    public void createLabel() {
        tvLabel = new AppCompatTextView(getContext());
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
        tvRequired = new AppCompatTextView(getContext());
        tvRequired.setId(View.generateViewId());
        tvRequired.setLines(1);
        tvRequired.setText("*");
        tvRequired.setVisibility(required ? View.VISIBLE : View.GONE);
        tvRequired.setGravity(android.view.Gravity.CENTER);
        tvRequired.setTextColor(ContextCompat.getColor(getContext(), R.color.theme_red));
        tvRequired.setTextSize(TypedValue.COMPLEX_UNIT_PX, formRequiredSize);

        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.setMarginStart(DensityUtil.dp2px(getContext(), 4f));

        addView(tvRequired, params);
    }

    public void createText() {
        AppCompatTextView tvText = new AppCompatTextView(getContext());
        tvText.setId(View.generateViewId());
        tvText.setHint(hintString);
        tvText.setTextColor(formTextColor);

        tvText.setEllipsize(android.text.TextUtils.TruncateAt.END);
        tvText.setTextColor(formTextColor);
        tvText.setHintTextColor(ContextCompat.getColor(getContext(), pers.fz.mvvm.R.color.hint_text_color));
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

    public void layoutLabel() {
        if (LabelAlignEnum.TOP.value == labelAlign) {
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(this);
            constraintSet.connect(tvLabel.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            constraintSet.connect(tvLabel.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            constraintSet.applyTo(this);
            ConstraintLayout.LayoutParams params = (LayoutParams) tvLabel.getLayoutParams();
            params.topMargin = (int) defaultTextMargin;
        } else if (LabelAlignEnum.LEFT.value == labelAlign) {
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(this);
            constraintSet.connect(tvLabel.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            constraintSet.connect(tvLabel.getId(), ConstraintSet.TOP, tvSelection.getId(), ConstraintSet.TOP);
            constraintSet.connect(tvLabel.getId(), ConstraintSet.END, tvRequired.getId(), ConstraintSet.START);
            constraintSet.applyTo(this);
            ConstraintLayout.LayoutParams params = (LayoutParams) tvLabel.getLayoutParams();
            params.topMargin = 0;
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