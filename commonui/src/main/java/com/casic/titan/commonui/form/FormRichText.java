package com.casic.titan.commonui.form;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.lifecycle.LifecycleOwner;

import com.bumptech.glide.Glide;
import com.casic.titan.commonui.R;
import com.casic.titan.commonui.databinding.FormRichTextBinding;
import com.casic.titan.commonui.inter.FormTextWatcher;

import pers.fz.mvvm.util.common.DensityUtil;
import pers.fz.mvvm.util.common.ThreadExecutor;

/**
 * Created by fz on 2024/11/13 14:21
 * describe :富文本
 */
public class FormRichText extends ConstraintLayout {
    protected String labelString;
    protected String hintString = "暂无数据";
    protected boolean required = false;
    protected boolean bottomBorder = true;
    protected int rightTextColor;
    protected int labelTextColor;
    public FormTextWatcher formTextWatcher;
    public FormRichTextBinding binding;
    private float formLabelTextSize;
    private float formTextSize;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Drawable defaultDrawable = null;

    public FormRichText(Context context) {
        super(context);
        initAttr(null);
        init();
    }

    public FormRichText(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttr(attrs);
        init();
    }

    public FormRichText(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(attrs);
        init();
    }

    protected void initAttr(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FormEditText);
            labelString = typedArray.getString(R.styleable.FormEditText_label);
            hintString = typedArray.getString(R.styleable.FormEditText_hint);
            formLabelTextSize = typedArray.getDimension(R.styleable.FormEditText_formLabelTextSize, DensityUtil.sp2px(getContext(), 14));
            formTextSize = typedArray.getDimension(R.styleable.FormEditText_formTextSize, DensityUtil.sp2px(getContext(), 14));
            rightTextColor = typedArray.getColor(R.styleable.FormEditText_rightTextColor, ContextCompat.getColor(getContext(), R.color.auto_color));
            labelTextColor = typedArray.getColor(R.styleable.FormEditText_labelTextColor, ContextCompat.getColor(getContext(), R.color.dark_color));
            required = typedArray.getBoolean(R.styleable.FormEditText_required, false);
            bottomBorder = typedArray.getBoolean(R.styleable.FormEditText_bottomBorder, true);
            typedArray.recycle();
        } else {
            rightTextColor = ContextCompat.getColor(getContext(), R.color.auto_color);
            labelTextColor = ContextCompat.getColor(getContext(), R.color.dark_color);
            formLabelTextSize = DensityUtil.sp2px(getContext(), 14);
            formTextSize = DensityUtil.sp2px(getContext(), 14);
        }
    }

    protected void init() {
        defaultDrawable = ContextCompat.getDrawable(getContext(), pers.fz.mvvm.R.mipmap.ic_default_image);
        binding = FormRichTextBinding.inflate(LayoutInflater.from(getContext()), this, true);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        binding.setLifecycleOwner((LifecycleOwner) getContext());
        binding.tvRichText.setSelected(true);
        binding.tvRichText.setHint(hintString);
        binding.tvRichText.setTextColor(rightTextColor);
        binding.tvLabel.setTextColor(labelTextColor);
        binding.tvLabel.setText(labelString);
        binding.tvLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, formLabelTextSize);
        binding.tvRichText.setTextSize(TypedValue.COMPLEX_UNIT_PX, formTextSize);
        if (bottomBorder) {
            setBackground(ContextCompat.getDrawable(getContext(), R.drawable.line_bottom));
        }
        binding.tvRichText.setMaxLines(Integer.MAX_VALUE);
        // 设置自定义布局的paddingTop和paddingBottom
        int padding = DensityUtil.dp2px(getContext(), 12); // 你可以根据需要调整padding值
        setPadding(getPaddingStart(), padding, getPaddingEnd(), padding);
        // 使链接可点击
        binding.tvRichText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public FormRichTextBinding getBinding() {
        return binding;
    }

    /**
     * 不要使用这个因为会导致databinding双向绑定无效
     */
    public void addTextChangedListener(TextWatcher watcher) {
        binding.tvRichText.addTextChangedListener(watcher);
    }

    /**
     * 推荐使用这个
     */
    public void addTextChangedListener(FormTextWatcher formTextWatcher) {
        this.formTextWatcher = formTextWatcher;
    }

    public CharSequence getText() {
        return binding.tvRichText.getText();
    }

    public void setText(String text) {
        if (TextUtils.isEmpty(text)) {
            binding.tvRichText.setText(null);
            return;
        }
        // 将 HTML 转换为 Spanned 对象并显示在 TextView 中
        ThreadExecutor.getInstance().execute(() -> {
            Spanned spanned = HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_COMPACT, source -> {
                // 使用Glide加载图片
                Drawable drawable;
                try {
                    drawable = Glide.with(getContext())
                            .asDrawable()
                            .load(source)
                            .submit()
                            .get();
                    // 设置 Drawable 的边界
                    int width = drawable.getIntrinsicWidth();
                    int height = drawable.getIntrinsicHeight();
                    drawable.setBounds(0, 0, width, height);
                } catch (Exception e) {
                    e.printStackTrace();
                    drawable = defaultDrawable;
                }
                return drawable;
            }, (b, s, editable, xmlReader) -> {

            });
            mainHandler.post(() -> binding.tvRichText.setText(spanned));
        });
    }

    public void setLabel(String text) {
        binding.tvLabel.setText(text);
    }
}
