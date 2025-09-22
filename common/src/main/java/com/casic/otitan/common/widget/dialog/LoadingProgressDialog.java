package com.casic.otitan.common.widget.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.casic.otitan.common.R;
import com.casic.otitan.common.databinding.LoadingDialogBinding;
import com.casic.otitan.common.utils.common.ScreenUtil;

/**
 * updated by fz on 2025/9/8.
 * describe:自定义加载dialog
 */
public class LoadingProgressDialog extends Dialog {
    private volatile static LoadingProgressDialog instance;
    private LoadingDialogBinding loadingDialogBinding;
    private boolean isCanCancel = false;
    private OnCancelListener onCancelListener;
    /**
     * 加载提示文字
     */
    private String message = "正在加载,请稍后";
    private String baseMessage = "正在加载,请稍后"; // 基础文本，不包含省略号
    private boolean enableDynamicEllipsis = false; // 是否启用动态省略号效果
    private int maxDotCount = 3; // 默认最大点数为3
    private int fixedWidth = -1; // 固定宽度，-1表示未设置

    private final Handler handler = new Handler(Looper.getMainLooper());

    // 正确的循环动画：. → .. → ... → . → .. → ...
    private final Runnable updateTextRunnable = new Runnable() {
        private int dotCount = 0;

        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            if (!enableDynamicEllipsis) {
                return;
            }

            // 递增点数
            dotCount++;

            // 如果超过最大点数，重置为1（从1个点重新开始）
            if (dotCount > maxDotCount) {
                dotCount = 1;
            }

            StringBuilder dots = new StringBuilder();
            for (int i = 0; i < dotCount; i++) {
                dots.append(".");
            }
            loadingDialogBinding.tipTextView.setText(baseMessage + dots);

            // 设置统一的延迟时间，确保匀速动画
            handler.postDelayed(this, 500); // 每500毫秒更新一次
        }
    };

    // 或者使用带暂停效果的版本：. → .. → ... → (暂停) → . → .. → ...
    private final Runnable updateTextRunnableWithPause = new Runnable() {
        private int dotCount = 0;

        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            if (!enableDynamicEllipsis) {
                return;
            }

            dotCount++;

            if (dotCount > maxDotCount) {
                // 达到最大点数后，重置为0并稍作暂停
                dotCount = 0;
                StringBuilder dots = new StringBuilder();
                for (int i = 0; i < maxDotCount; i++) {
                    dots.append(".");
                }
                loadingDialogBinding.tipTextView.setText(baseMessage + dots);
                // 在最大点数时暂停800ms
                handler.postDelayed(this, 800);
                return;
            }

            StringBuilder dots = new StringBuilder();
            for (int i = 0; i < dotCount; i++) {
                dots.append(".");
            }
            loadingDialogBinding.tipTextView.setText(baseMessage + dots);

            // 正常递增时的速度
            handler.postDelayed(this, 300);
        }
    };

    public LoadingProgressDialog(Context context) {
        super(context, R.style.loading_dialog);
    }

    public static LoadingProgressDialog getInstance(Context context) {
        if (instance == null || !instance.isShowing()) {
            synchronized (LoadingProgressDialog.class) {
                if (instance == null || !instance.isShowing()) {
                    instance = new LoadingProgressDialog(context);
                }
            }
        }
        return instance;
    }

    public void refreshMessage(String message) {
        processMessage(message);
        // 计算最大宽度并设置固定宽度
        calculateAndSetFixedWidth();
        if (enableDynamicEllipsis) {
            // 如果启用动态效果，则重新启动Runnable
            handler.removeCallbacks(updateTextRunnable);
            handler.post(updateTextRunnable);
        } else {
            loadingDialogBinding.setMessage(message);
        }
        refreshLayoutWidth();
    }

    public LoadingProgressDialog(Context context, int theme) {
        super(context, theme);
    }

    public LoadingProgressDialog setCanCancel(boolean isCanCancel) {
        this.isCanCancel = isCanCancel;
        return this;
    }

    public void refreshCanCancel(boolean isCanCancel) {
        this.isCanCancel = isCanCancel;
        setCancelable(isCanCancel);
    }

    public LoadingProgressDialog setCanCelListener(OnCancelListener onCancelListener) {
        this.onCancelListener = onCancelListener;
        return this;
    }

    // 新增方法：设置是否启用动态省略号效果
    public LoadingProgressDialog setEnableDynamicEllipsis(boolean enable) {
        this.enableDynamicEllipsis = enable;
        return this;
    }

    public LoadingProgressDialog builder() {
        createLoadingDialog();
        return this;
    }

    private void createLoadingDialog() {
        loadingDialogBinding = LoadingDialogBinding.inflate(getLayoutInflater(), null, false);

        // 计算最大宽度并设置固定宽度
        calculateAndSetFixedWidth();

        if (enableDynamicEllipsis) {
            // 如果启用动态效果，则启动Runnable
            handler.post(updateTextRunnable);
        } else {
            loadingDialogBinding.setMessage(message);
        }
        setCanceledOnTouchOutside(isCanCancel);
        setCancelable(isCanCancel);
        setOnCancelListener(onCancelListener);

        refreshLayoutWidth();

        setContentView(loadingDialogBinding.getRoot());
    }

    /**
     * 计算最大文本宽度并设置固定宽度
     */
    private void calculateAndSetFixedWidth() {
        if (!enableDynamicEllipsis) {
            return;
        }

        // 创建最大文本（基础文本 + 最大点数个点）
        StringBuilder maxText = new StringBuilder(baseMessage);
        for (int i = 0; i < maxDotCount; i++) {
            maxText.append(".");
        }

        // 使用Paint计算文本宽度
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(loadingDialogBinding.tipTextView.getTextSize());
        textPaint.setTypeface(loadingDialogBinding.tipTextView.getTypeface());
        float textWidth = textPaint.measureText(maxText.toString());

        // 获取TextView的padding（如果View还未初始化，使用默认值）
        int paddingLeft = 0;
        int paddingRight = 0;

        try {
            paddingLeft = loadingDialogBinding.tipTextView.getPaddingLeft();
            paddingRight = loadingDialogBinding.tipTextView.getPaddingRight();
        } catch (Exception e) {
            // 如果View还未完全初始化，使用默认padding
            int defaultPadding = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 8,
                    getContext().getResources().getDisplayMetrics()
            );
            paddingLeft = defaultPadding;
            paddingRight = defaultPadding;
        }

        // 计算总宽度（文本宽度 + 左右padding）
        fixedWidth = (int) Math.ceil(textWidth) + paddingLeft + paddingRight;

        // 设置最小宽度，避免太窄
        int minWidth = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 120,
                getContext().getResources().getDisplayMetrics()
        );
        fixedWidth = Math.max(fixedWidth, minWidth);
        int screenWidth = ScreenUtil.getScreenWidth(getContext());
        fixedWidth = Math.min(fixedWidth, (int)(screenWidth * 4.0 / 5));
        // 设置TextView的固定宽度
        ViewGroup.LayoutParams params = loadingDialogBinding.tipTextView.getLayoutParams();
        if (params == null) {
            params = new LinearLayout.LayoutParams(fixedWidth,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
        } else {
            params.width = fixedWidth;
        }
        loadingDialogBinding.tipTextView.setLayoutParams(params);
    }

    public LoadingProgressDialog setMessage(String message) {
        processMessage(message);
        return this;
    }

    /**
     * 处理消息文本，提取基础文本和点数
     *
     * @param message 原始消息
     */
    private void processMessage(String message) {
        this.message = message;

        if (enableDynamicEllipsis && message != null && message.contains(".")) {
            // 启用动态效果且消息包含点，提取基础文本和点数
            int lastNonDotIndex = message.length() - 1;

            // 找到最后一个非点的字符位置
            while (lastNonDotIndex >= 0 && message.charAt(lastNonDotIndex) == '.') {
                lastNonDotIndex--;
            }

            if (lastNonDotIndex < message.length() - 1) {
                // 末尾有点
                baseMessage = message.substring(0, lastNonDotIndex + 1);
                maxDotCount = message.length() - lastNonDotIndex - 1;
            } else {
                // 末尾没有点或者点不在末尾
                baseMessage = message;
                maxDotCount = 3; // 默认3个点
            }
        } else {
            // 不启用动态效果或者消息不包含点
            baseMessage = message;
            maxDotCount = 3; // 默认3个点
        }
    }

    private void refreshLayoutWidth() {
        // 设置固定宽度
        LinearLayout rootLayout = loadingDialogBinding.llProgress;
        if (fixedWidth > 0) {
            ViewGroup.LayoutParams params = rootLayout.getLayoutParams();
            if (params == null) {
                params = new LinearLayout.LayoutParams(
                        fixedWidth,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
            } else {
                params.width = fixedWidth + loadingDialogBinding.llProgress.getPaddingStart() + loadingDialogBinding.llProgress.getPaddingEnd();
            }
            rootLayout.setLayoutParams(params);
        }
    }

    @Override
    public void show() {
        // 防止重复显示
        if (!isShowing()) {
            super.show();
            if (enableDynamicEllipsis) {
                handler.removeCallbacks(updateTextRunnable);
                handler.post(updateTextRunnable);
            }
        }
    }

    @Override
    public void dismiss() {
        try {
            handler.removeCallbacks(updateTextRunnable); // 停止动态效果
            if (isShowing()) {
                super.dismiss();
            }
        } finally {
            instance = null;
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacks(updateTextRunnable); // 确保Handler停止
        instance = null;
    }
}