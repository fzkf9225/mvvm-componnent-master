package com.casic.otitan.common.adapter;

import android.annotation.SuppressLint;
import android.view.ViewGroup;
import android.widget.TextView;

import com.casic.otitan.common.R;
import com.casic.otitan.common.base.BaseRecyclerViewAdapter;
import com.casic.otitan.common.base.BaseViewHolder;
import com.casic.otitan.common.bean.PopupWindowBean;
import com.casic.otitan.common.databinding.OptionTextViewBinding;

/**
 * Updated by fz on 2026/3/18.
 * describe：底部选择菜单
 * 支持自定义样式属性：
 * - 列表项高度
 * - 字体大小
 * - 字体颜色
 * - 单行/多行显示
 * - 左右margin
 * - 上下padding
 */
public class MenuListAdapter<T extends PopupWindowBean> extends BaseRecyclerViewAdapter<T,OptionTextViewBinding> {


    // 样式属性
    private int itemHeight = -1; // 列表项高度，-1表示使用wrap_content
    private float textSize = -1; // 字体大小，-1表示使用默认值
    private int textColor = -1; // 字体颜色，-1表示使用默认值
    private int maxLines = 1; // 最大行数，默认单行
    private int leftMargin = -1; // 左边距
    private int rightMargin = -1; // 右边距
    private int topPadding = -1; // 上内边距
    private int bottomPadding = -1; // 下内边距
    private int leftPadding = -1; // 左内边距
    private int rightPadding = -1; // 右内边距
    private boolean isSingleLine = true; // 是否单行显示

    public MenuListAdapter() {
        super();
    }

    @Override
    public void onBindHolder(BaseViewHolder<OptionTextViewBinding> holder, int pos) {
        OptionTextViewBinding binding = holder.getBinding();
        binding.setItem(mList.get(pos));

        // 应用自定义样式
        applyCustomStyles(binding);
    }

    /**
     * 应用自定义样式到视图
     */
    private void applyCustomStyles(OptionTextViewBinding binding) {
        TextView textView = binding.tvOption;
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) textView.getLayoutParams();

        // 设置列表项高度
        if (itemHeight > 0) {
            params.height = itemHeight;
        }

        // 设置左右margin
        if (leftMargin >= 0) {
            params.leftMargin = leftMargin;
        }
        if (rightMargin >= 0) {
            params.rightMargin = rightMargin;
        }

        // 设置上下padding
        int currentTopPadding = textView.getPaddingTop();
        int currentBottomPadding = textView.getPaddingBottom();
        int currentLeftPadding = textView.getPaddingLeft();
        int currentRightPadding = textView.getPaddingRight();

        int newTopPadding = topPadding >= 0 ? topPadding : currentTopPadding;
        int newBottomPadding = bottomPadding >= 0 ? bottomPadding : currentBottomPadding;
        int newLeftPadding = leftPadding >= 0 ? leftPadding : currentLeftPadding;
        int newRightPadding = rightPadding >= 0 ? rightPadding : currentRightPadding;

        textView.setPadding(newLeftPadding, newTopPadding, newRightPadding, newBottomPadding);

        // 设置字体大小
        if (textSize > 0) {
            textView.setTextSize(textSize);
        }

        // 设置字体颜色
        if (textColor != -1) {
            textView.setTextColor(textColor);
        }

        // 设置单行/多行显示
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

        textView.setLayoutParams(params);
    }

    @Override
    public int getLayoutId() {
        return R.layout.option_text_view;
    }

    // ==================== 样式设置方法 ====================

    /**
     * 设置列表项高度
     * @param height 高度值（像素）
     */
    @SuppressLint("NotifyDataSetChanged")
    public void setItemHeight(int height) {
        this.itemHeight = height;
        notifyDataSetChanged();
    }

    /**
     * 设置字体大小
     * @param size 字体大小（sp）
     */
    public void setTextSize(float size) {
        this.textSize = size;
        notifyDataSetChanged();
    }

    /**
     * 设置字体颜色
     * @param color 颜色资源ID或颜色值
     */
    public void setTextColor(int color) {
        this.textColor = color;
        notifyDataSetChanged();
    }

    /**
     * 设置最大行数
     * @param maxLines 最大行数
     */
    public void setMaxLines(int maxLines) {
        this.maxLines = maxLines;
        this.isSingleLine = (maxLines == 1);
        notifyDataSetChanged();
    }

    /**
     * 设置是否单行显示
     * @param isSingleLine true为单行，false为多行
     */
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
     * 设置左右边距
     * @param left 左边距（像素）
     * @param right 右边距（像素）
     */
    public void setMargins(int left, int right) {
        this.leftMargin = left;
        this.rightMargin = right;
        notifyDataSetChanged();
    }

    /**
     * 设置上下内边距
     * @param top 上内边距（像素）
     * @param bottom 下内边距（像素）
     */
    public void setVerticalPadding(int top, int bottom) {
        this.topPadding = top;
        this.bottomPadding = bottom;
        notifyDataSetChanged();
    }

    /**
     * 设置左右内边距
     * @param left 左内边距（像素）
     * @param right 右内边距（像素）
     */
    public void setHorizontalPadding(int left, int right) {
        this.leftPadding = left;
        this.rightPadding = right;
        notifyDataSetChanged();
    }

    /**
     * 设置所有内边距
     * @param left 左内边距
     * @param top 上内边距
     * @param right 右内边距
     * @param bottom 下内边距
     */
    public void setPadding(int left, int top, int right, int bottom) {
        this.leftPadding = left;
        this.topPadding = top;
        this.rightPadding = right;
        this.bottomPadding = bottom;
        notifyDataSetChanged();
    }

    /**
     * 批量设置样式
     * @param builder 样式构建器
     */
    public void applyStyles(StyleBuilder builder) {
        this.itemHeight = builder.itemHeight;
        this.textSize = builder.textSize;
        this.textColor = builder.textColor;
        this.maxLines = builder.maxLines;
        this.leftMargin = builder.leftMargin;
        this.rightMargin = builder.rightMargin;
        this.topPadding = builder.topPadding;
        this.bottomPadding = builder.bottomPadding;
        this.leftPadding = builder.leftPadding;
        this.rightPadding = builder.rightPadding;
        this.isSingleLine = builder.isSingleLine;
        notifyDataSetChanged();
    }

    /**
     * 样式构建器（建造者模式）
     */
    public static class StyleBuilder {
        private int itemHeight = -1;
        private float textSize = -1;
        private int textColor = -1;
        private int maxLines = 1;
        private int leftMargin = -1;
        private int rightMargin = -1;
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

        public StyleBuilder setMargins(int left, int right) {
            this.leftMargin = left;
            this.rightMargin = right;
            return this;
        }

        public StyleBuilder setVerticalPadding(int top, int bottom) {
            this.topPadding = top;
            this.bottomPadding = bottom;
            return this;
        }

        public StyleBuilder setHorizontalPadding(int left, int right) {
            this.leftPadding = left;
            this.rightPadding = right;
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
