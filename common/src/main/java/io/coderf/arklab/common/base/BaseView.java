package io.coderf.arklab.common.base;

import androidx.annotation.StringRes;

/**
 * 作者： fz
 * 时间： 2019/7/30
 * 描述：
 */

public interface BaseView {
    /**
     * 显示dialog
     *
     * @param dialogMessage         文本内容
     * @param enableDynamicEllipsis 动态播放省略号
     */
    void showLoading(String dialogMessage, boolean enableDynamicEllipsis);

    /**
     * 更新dialog
     */
    void refreshLoading(String dialogMessage);

    /**
     * 隐藏 dialog
     */

    void hideLoading();

    /**
     * 显示错误信息
     *
     * @param msg 吐司提示的问题
     */
    void showToast(String msg);
    /**
     * 显示错误信息
     *
     * @param strRes 字符资源id
     */
    void showToast(@StringRes int strRes);
    /**
     * 错误码
     */
    void onErrorCode(BaseResponse model);

}
