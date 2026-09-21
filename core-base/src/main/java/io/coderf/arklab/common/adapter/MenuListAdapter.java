package io.coderf.arklab.common.adapter;

import android.annotation.SuppressLint;
import android.view.ViewGroup;

import com.google.android.material.textview.MaterialTextView;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.base.BaseRecyclerViewAdapter;
import io.coderf.arklab.common.base.BaseViewHolder;
import io.coderf.arklab.common.bean.PopupWindowBean;
import io.coderf.arklab.common.databinding.OptionTextViewBinding;

/**
 * 底部选择菜单列表 Adapter。
 * <p>
 * 可配置：项高度、字号、字色、行数、四边 padding。
 * 水波纹来自 item 布局 {@code SelectableItemStyle}（background = selectableItemBackground）。
 * 若对 item 再 {@code setBackground(...)} 实心色，会盖掉水波纹；改字色不影响。
 *
 * @author fz
 * @version 1.1
 * @since 1.0
 * @updated 2026/9/21
 */
public class MenuListAdapter<T extends PopupWindowBean> extends BaseRecyclerViewAdapter<T, OptionTextViewBinding> {

    private int itemHeight = -1;
    private float textSize = -1;
    private int textColor = -1;
    private int maxLines = 1;
    private int topPadding = -1;
    private int bottomPadding = -1;
    private int leftPadding = -1;
    private int rightPadding = -1;
    private boolean isSingleLine = true;

    public MenuListAdapter() {
        super();
    }

    @Override
    public void onBindHolder(BaseViewHolder<OptionTextViewBinding> holder, int pos) {
        OptionTextViewBinding binding = holder.getBinding();
        binding.setItem(mList.get(pos));
        applyCustomStyles(binding);
    }

    private void applyCustomStyles(OptionTextViewBinding binding) {
        MaterialTextView textView = binding.tvOption;
        ViewGroup.LayoutParams params = textView.getLayoutParams();

        if (itemHeight > 0) {
            params.height = itemHeight;
            textView.setLayoutParams(params);
        }

        int newTop = topPadding >= 0 ? topPadding : textView.getPaddingTop();
        int newBottom = bottomPadding >= 0 ? bottomPadding : textView.getPaddingBottom();
        int newLeft = leftPadding >= 0 ? leftPadding : textView.getPaddingLeft();
        int newRight = rightPadding >= 0 ? rightPadding : textView.getPaddingRight();
        textView.setPadding(newLeft, newTop, newRight, newBottom);

        if (textSize > 0) {
            textView.setTextSize(textSize);
        }
        if (textColor != -1) {
            textView.setTextColor(textColor);
        }

        if (isSingleLine) {
            textView.setSingleLine(true);
            textView.setMaxLines(1);
            textView.setEllipsize(android.text.TextUtils.TruncateAt.END);
        } else {
            textView.setSingleLine(false);
            if (maxLines > 0) {
                textView.setMaxLines(maxLines);
            }
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.option_text_view;
    }

    // ==================== 样式设置 ====================

    @SuppressLint("NotifyDataSetChanged")
    public void setItemHeight(int height) {
        this.itemHeight = height;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setTextSize(float size) {
        this.textSize = size;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setTextColor(int color) {
        this.textColor = color;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setMaxLines(int maxLines) {
        this.maxLines = maxLines;
        this.isSingleLine = (maxLines == 1);
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setSingleLine(boolean isSingleLine) {
        this.isSingleLine = isSingleLine;
        if (!isSingleLine) {
            this.maxLines = Integer.MAX_VALUE;
        } else {
            this.maxLines = 1;
        }
        notifyDataSetChanged();
    }

    /**
     * 设置左右内边距（原 setMargins，已改为 padding，避免与列表/父布局 margin 混淆）。
     *
     * @param left  左 padding（px）
     * @param right 右 padding（px）
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setHorizontalPadding(int left, int right) {
        this.leftPadding = left;
        this.rightPadding = right;
        notifyDataSetChanged();
    }

    /**
     * @deprecated 请使用 {@link #setHorizontalPadding(int, int)}，语义为内边距而非外边距。
     */
    @Deprecated
    @SuppressLint("NotifyDataSetChanged")
    public void setMargins(int left, int right) {
        setHorizontalPadding(left, right);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setVerticalPadding(int top, int bottom) {
        this.topPadding = top;
        this.bottomPadding = bottom;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setPadding(int left, int top, int right, int bottom) {
        this.leftPadding = left;
        this.topPadding = top;
        this.rightPadding = right;
        this.bottomPadding = bottom;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void applyStyles(StyleBuilder builder) {
        this.itemHeight = builder.itemHeight;
        this.textSize = builder.textSize;
        this.textColor = builder.textColor;
        this.maxLines = builder.maxLines;
        this.topPadding = builder.topPadding;
        this.bottomPadding = builder.bottomPadding;
        this.leftPadding = builder.leftPadding;
        this.rightPadding = builder.rightPadding;
        this.isSingleLine = builder.isSingleLine;
        notifyDataSetChanged();
    }

    /**
     * 样式构建器。
     */
    public static class StyleBuilder {
        private int itemHeight = -1;
        private float textSize = -1;
        private int textColor = -1;
        private int maxLines = 1;
        private int topPadding = -1;
        private int bottomPadding = -1;
        private int leftPadding = -1;
        private int rightPadding = -1;
        private boolean isSingleLine = true;

        public StyleBuilder setItemHeight(int height) {
            this.itemHeight = height;
            return this;
        }

        public StyleBuilder setTextSize(float size) {
            this.textSize = size;
            return this;
        }

        public StyleBuilder setTextColor(int color) {
            this.textColor = color;
            return this;
        }

        public StyleBuilder setMaxLines(int maxLines) {
            this.maxLines = maxLines;
            this.isSingleLine = (maxLines == 1);
            return this;
        }

        public StyleBuilder setSingleLine(boolean singleLine) {
            isSingleLine = singleLine;
            this.maxLines = singleLine ? 1 : Integer.MAX_VALUE;
            return this;
        }

        /** 左右内边距（px）。 */
        public StyleBuilder setHorizontalPadding(int left, int right) {
            this.leftPadding = left;
            this.rightPadding = right;
            return this;
        }

        /**
         * @deprecated 请使用 {@link #setHorizontalPadding(int, int)}
         */
        @Deprecated
        public StyleBuilder setMargins(int left, int right) {
            return setHorizontalPadding(left, right);
        }

        public StyleBuilder setVerticalPadding(int top, int bottom) {
            this.topPadding = top;
            this.bottomPadding = bottom;
            return this;
        }

        public StyleBuilder setPadding(int left, int top, int right, int bottom) {
            this.leftPadding = left;
            this.topPadding = top;
            this.rightPadding = right;
            this.bottomPadding = bottom;
            return this;
        }

        public StyleBuilder build() {
            return this;
        }
    }
}
