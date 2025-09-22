package com.casic.otitan.commonui.form;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.casic.otitan.commonui.R;

import com.casic.otitan.common.utils.common.DensityUtil;


/**
 * Created by fz on 2023/12/26 16:27
 * describe :
 */
public class FormSelection extends FormConstraintLayout {
    /**
     * 下拉框icon图标
     */
    protected Drawable selectionIcon;

    public FormSelection(@NonNull Context context) {
        super(context);
    }

    public FormSelection(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FormSelection(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initAttr(AttributeSet attrs) {
        super.initAttr(attrs);
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.FormUI);
            selectionIcon = typedArray.getDrawable(R.styleable.FormUI_selectionIcon);
            if (selectionIcon == null) {
                selectionIcon = ContextCompat.getDrawable(getContext(), R.mipmap.icon_down);
            }
            typedArray.recycle();
        } else {
            selectionIcon = ContextCompat.getDrawable(getContext(), R.mipmap.icon_down);
        }
    }

    @Override
    public void createText() {
        super.createText();
        setSelectionIcon(selectionIcon, DensityUtil.dp2px(getContext(), 8f));
    }

    public void setSelectionIcon(Drawable drawable, int padding) {
        AppCompatTextView textView = (AppCompatTextView) tvSelection;
        textView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
        textView.setCompoundDrawablePadding(padding);
    }
}
