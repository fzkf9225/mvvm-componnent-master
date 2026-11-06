package io.coderf.arklab.ui.form;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import com.google.android.material.textview.MaterialTextView;
import androidx.core.text.HtmlCompat;

import com.bumptech.glide.Glide;

import io.coderf.arklab.common.api.Config;
import io.coderf.arklab.core.utils.ThreadExecutor;

/**
 * 富文本
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2024/11/13 14:21
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
        defaultDrawable = Config.getInstance().getDefaultPlaceholderDrawable(getContext());
        super.init();
    }

    @Override
    public void createText() {
        super.createText();
        MaterialTextView tvRichText = (MaterialTextView) tvSelection;
        tvRichText.setMaxLines(Integer.MAX_VALUE);
        tvRichText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    protected void applyDataSourceToView(@Nullable String newValue) {
        if (tvSelection instanceof MaterialTextView textView) {
            CharSequence current = textView.getText();
            String currentPlain = current == null ? null : current.toString();
            if (currentPlain == null && newValue == null) {
                return;
            }
            if (currentPlain != null && currentPlain.equals(newValue)) {
                return;
            }
            setRichText(newValue);
        }
    }

    @Override
    public void setText(String text) {
        // 经 dataSource 回调走 applyDataSourceToView → setRichText
        dataSource.set(text);
    }

    public void setRichText(String text) {
        MaterialTextView tvRichText = (MaterialTextView) tvSelection;
        if (tvRichText == null) {
            return;
        }
        if (TextUtils.isEmpty(text)) {
            tvRichText.setText(null);
            return;
        }
        // 将 HTML 转换为 Spanned 对象并显示在 MaterialTextView 中
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
            mainHandler.post(() -> {
                if (!isAttachedToWindow() || tvSelection == null) {
                    return;
                }
                ((MaterialTextView) tvSelection).setText(spanned);
            });
        });
    }
}
