package io.coderf.arklab.ui.form;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import io.coderf.arklab.common.utils.common.DensityUtil;
import io.coderf.arklab.ui.R;


/**
 * Created by fz on 2023/12/26 16:27
 * describe :
 */
public class FormSelection extends FormConstraintLayout {
    /**
     * 下拉框icon图标
     */
    protected Drawable selectionIcon;
    /**
     * 下拉框icon宽度，<=0 时自适应图标原始尺寸
     */
    protected float selectionIconWidth;
    /**
     * 下拉框icon高度，<=0 时自适应图标原始尺寸
     */
    protected float selectionIconHeight;
    /**
     * 下拉框icon与文字间距
     */
    protected int selectionIconPadding;

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
            selectionIconWidth = typedArray.getDimension(R.styleable.FormUI_selectionIconWidth, 0);
            selectionIconHeight = typedArray.getDimension(R.styleable.FormUI_selectionIconHeight, 0);
            selectionIconPadding = (int) typedArray.getDimension(
                    R.styleable.FormUI_selectionIconPadding, DensityUtil.dp2px(getContext(), 8f));
            typedArray.recycle();
        } else {
            selectionIcon = ContextCompat.getDrawable(getContext(), R.mipmap.icon_down);
            selectionIconPadding = DensityUtil.dp2px(getContext(), 8f);
        }
    }

    @Override
    public void createText() {
        super.createText();
        applySelectionIcon();
    }

    public void setSelectionIcon(Drawable drawable, int padding) {
        this.selectionIcon = drawable;
        this.selectionIconPadding = padding;
        applySelectionIcon();
    }

    protected void applySelectionIcon() {
        if (tvSelection == null) {
            return;
        }
        AppCompatTextView textView = (AppCompatTextView) tvSelection;
        Drawable drawable = selectionIcon;
        if (drawable == null) {
            textView.setCompoundDrawables(null, null, null, null);
            textView.setCompoundDrawablePadding(selectionIconPadding);
            return;
        }
        drawable = drawable.mutate();
        if (selectionIconWidth > 0 || selectionIconHeight > 0) {
            int width = selectionIconWidth > 0
                    ? (int) selectionIconWidth
                    : drawable.getIntrinsicWidth();
            int height = selectionIconHeight > 0
                    ? (int) selectionIconHeight
                    : drawable.getIntrinsicHeight();
            if (width > 0 && height > 0) {
                drawable.setBounds(0, 0, width, height);
                textView.setCompoundDrawables(null, null, drawable, null);
            } else {
                textView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
            }
        } else {
            textView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
        }
        textView.setCompoundDrawablePadding(selectionIconPadding);
    }
}
