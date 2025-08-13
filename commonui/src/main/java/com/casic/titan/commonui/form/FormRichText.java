package com.casic.titan.commonui.form;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.databinding.Observable;

import com.bumptech.glide.Glide;

import pers.fz.mvvm.util.common.ThreadExecutor;

/**
 * Created by fz on 2024/11/13 14:21
 * describe :富文本
 */
public class FormRichText extends FormTextView {
    /**
     * handler
     */
    protected final Handler mainHandler = new Handler(Looper.getMainLooper());
    /**
     * 图片占位图
     */
    protected Drawable defaultDrawable = null;

    public FormRichText(Context context) {
        super(context);
    }

    public FormRichText(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FormRichText(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        defaultDrawable = ContextCompat.getDrawable(getContext(), pers.fz.mvvm.R.mipmap.ic_default_image);
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
                        setRichText(newValue);
                    } else if (!textView.getText().toString().equals(newValue)) {
                        setRichText(newValue);
                    }
                }
            }
        });
    }

    @Override
    public void createText() {
        super.createText();
        AppCompatTextView tvRichText = (AppCompatTextView) tvSelection;
        tvRichText.setMaxLines(Integer.MAX_VALUE);
        tvRichText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void setText(String text) {
        dataSource.set(text);
        setRichText(text);
    }

    public void setRichText(String text){
        AppCompatTextView tvRichText = (AppCompatTextView) tvSelection;
        if (TextUtils.isEmpty(text)) {
            tvRichText.setText(null);
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
            mainHandler.post(() -> tvRichText.setText(spanned));
        });
    }
}
