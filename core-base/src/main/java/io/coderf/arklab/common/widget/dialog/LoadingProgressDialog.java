package io.coderf.arklab.common.widget.dialog;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import io.coderf.arklab.common.R;
import io.coderf.arklab.common.databinding.LoadingDialogBinding;
import io.coderf.arklab.core.utils.ScreenUtil;

/**
 * 全局加载进度弹窗（单例）。
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2025/9/8
 */
public class LoadingProgressDialog extends BaseDialog {
    /**
     * 绑定布局
     */
    private LoadingDialogBinding loadingDialogBinding;
    /**
     * 是否可取消
     */
    private boolean isCanCancel = false;
    /**
     * 取消监听
     */
    private OnCancelListener onCancelListener;
    /**
     * 加载提示文字
     */
    private String message = "正在加载,请稍后";
    /**
     * 基础文本，不包含省略号
     */
    private String baseMessage = "正在加载,请稍后";
    /**
     * 是否启用动态省略号效果
     */
    private boolean enableDynamicEllipsis = false;
    /**
     * 默认的最大点数
     */
    private final static int DEFAULT_MAX_DOT_COUNT = 3;
    /**
     * 默认最大点数为3
     */
    private int maxDotCount = DEFAULT_MAX_DOT_COUNT;
    /**
     * 固定宽度，-1表示未设置
     */
    private int fixedWidth = -1;

    private final Handler handler = new Handler(Looper.getMainLooper());

    /**
     * 正确的循环动画：. → .. → ... → . → .. → ...（未显示的点用透明占位，避免文字抖动/偏移）
     */
    private final Runnable updateTextRunnable = new Runnable() {
        private int dotCount = 0;

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

            applyEllipsisText(dotCount);

            // 设置统一的延迟时间，确保匀速动画
            handler.postDelayed(this, 500); // 每500毫秒更新一次
        }
    };

    public LoadingProgressDialog(Context context) {
        super(context, R.style.loading_dialog);
    }

    /**
     * 创建加载框。每次传入当前页面 Context，避免单例缓存已销毁 Activity。
     * <p>
     * 保留方法名以兼容既有调用；实现改为按需新建（不再跨页面复用 Dialog）。
     */
    public static LoadingProgressDialog getInstance(Context context) {
        return new LoadingProgressDialog(context);
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
            handler.removeCallbacks(updateTextRunnable);
            loadingDialogBinding.setMessage(message);
        }
        refreshLayoutWidth();
    }

    /**
     * 已在展示时原地刷新文案与行为，避免 dismiss 后再 show 造成闪烁。
     */
    /**
     * 已在展示时原地刷新文案、可取消状态与取消监听，避免 dismiss 后再 show 造成闪烁。
     *
     * @param message               加载文案
     * @param enableDynamicEllipsis 是否动态省略号
     * @param isCanCancel           是否允许系统返回 / 手势返回取消（点外侧永不取消）
     */
    public void refreshShowing(String message, boolean enableDynamicEllipsis, boolean isCanCancel) {
        this.enableDynamicEllipsis = enableDynamicEllipsis;
        refreshCanCancel(isCanCancel);
        // 刷新时同步 OnCancelListener（新建 Dialog 在 builder 里设置，复用实例时必须补上）
        setOnCancelListener(isCanCancel ? onCancelListener : null);
        refreshMessage(message);
    }

    public LoadingProgressDialog(Context context, int theme) {
        super(context, theme);
    }

    public LoadingProgressDialog setCanCancel(boolean isCanCancel) {
        this.isCanCancel = isCanCancel;
        return this;
    }

    /**
     * 更新是否可取消。可取消时仅系统返回 / 手势返回生效，
     * {@link #setCanceledOnTouchOutside(boolean)} 始终为 false，避免误点外侧取消请求。
     */
    public void refreshCanCancel(boolean isCanCancel) {
        this.isCanCancel = isCanCancel;
        setCancelable(isCanCancel);
        setCanceledOnTouchOutside(false);
    }

    /**
     * 设置取消监听；若 Dialog 已创建，立即应用到 {@link #setOnCancelListener}。
     *
     * @param onCancelListener 用户取消回调，可为 null
     */
    public LoadingProgressDialog setCanCelListener(OnCancelListener onCancelListener) {
        this.onCancelListener = onCancelListener;
        // builder 之后再设 listener 时需立刻生效
        if (loadingDialogBinding != null) {
            setOnCancelListener(onCancelListener);
        }
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
        loadingDialogBinding = inflateWithHostAdapt(
                () -> LoadingDialogBinding.inflate(getLayoutInflater(), null, false));

        // 计算最大宽度并设置固定宽度
        calculateAndSetFixedWidth();

        if (enableDynamicEllipsis) {
            // 如果启用动态效果，则启动Runnable
            handler.post(updateTextRunnable);
        } else {
            loadingDialogBinding.setMessage(message);
        }
        // 仅返回键 / 手势返回可取消；点外侧不取消，降低误触
        setCanceledOnTouchOutside(false);
        setCancelable(isCanCancel);
        setOnCancelListener(onCancelListener);

        refreshLayoutWidth();

        setContentView(loadingDialogBinding.getRoot());
    }

    /**
     * 构建带透明占位省略号的文案：始终保留 maxDotCount 个点的宽度，
     * 仅前 visibleDotCount 个点可见，其余透明，避免居中抖动与偏左。
     */
    private CharSequence buildEllipsisText(int visibleDotCount) {
        String safeBase = baseMessage != null ? baseMessage : "";
        int dots = Math.max(maxDotCount, 0);
        int visible = Math.max(0, Math.min(visibleDotCount, dots));

        StringBuilder fullText = new StringBuilder(safeBase);
        for (int i = 0; i < dots; i++) {
            fullText.append('.');
        }

        SpannableString spannable = new SpannableString(fullText);
        if (visible < dots) {
            spannable.setSpan(
                    new ForegroundColorSpan(Color.TRANSPARENT),
                    safeBase.length() + visible,
                    safeBase.length() + dots,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
        return spannable;
    }

    private void applyEllipsisText(int visibleDotCount) {
        if (loadingDialogBinding == null) {
            return;
        }
        loadingDialogBinding.tipTextView.setText(buildEllipsisText(visibleDotCount));
    }

    /**
     * 按最大文本（基础文案 + 满点数）计算并固定 TextView 宽度，配合透明占位双重防抖。
     */
    private void calculateAndSetFixedWidth() {
        if (!enableDynamicEllipsis) {
            return;
        }

        // 创建最大文本（基础文本 + 最大点数个点）
        StringBuilder maxText = new StringBuilder(baseMessage != null ? baseMessage : "");
        for (int i = 0; i < maxDotCount; i++) {
            maxText.append('.');
        }

        // 使用Paint计算文本宽度
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(loadingDialogBinding.tipTextView.getTextSize());
        textPaint.setTypeface(loadingDialogBinding.tipTextView.getTypeface());
        float textWidth = textPaint.measureText(maxText.toString());

        // 获取TextView的padding（如果View还未初始化，使用默认值）
        int paddingLeft;
        int paddingRight;
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
        fixedWidth = Math.min(fixedWidth, (int) (screenWidth * 4.0 / 5));

        ViewGroup.LayoutParams params = loadingDialogBinding.tipTextView.getLayoutParams();
        if (params == null) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(fixedWidth,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.CENTER_HORIZONTAL;
            params = lp;
        } else {
            params.width = fixedWidth;
            if (params instanceof LinearLayout.LayoutParams) {
                ((LinearLayout.LayoutParams) params).gravity = Gravity.CENTER_HORIZONTAL;
            }
        }
        loadingDialogBinding.tipTextView.setLayoutParams(params);
        loadingDialogBinding.tipTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        loadingDialogBinding.tipTextView.setTextAlignment(android.view.View.TEXT_ALIGNMENT_CENTER);
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
                maxDotCount = DEFAULT_MAX_DOT_COUNT; // 默认3个点
            }
        } else {
            // 不启用动态效果或者消息不包含点
            baseMessage = message;
            maxDotCount = DEFAULT_MAX_DOT_COUNT; // 默认3个点
        }
    }

    private void refreshLayoutWidth() {
        // 设置自适应宽度
        LinearLayout rootLayout = loadingDialogBinding.llProgress;
        ViewGroup.LayoutParams params = rootLayout.getLayoutParams();
        if (params == null) {
            params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
        } else {
            params.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        }
        rootLayout.setLayoutParams(params);
    }

    @Override
    public void show() {
        if (!canShow(getContext())) {
            return;
        }
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
        handler.removeCallbacks(updateTextRunnable);
        if (isShowing()) {
            super.dismiss();
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacks(updateTextRunnable);
    }
}