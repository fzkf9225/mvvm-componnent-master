package pers.fz.mvvm.base;

/**
 * 作者： fz
 * 时间： 2019/7/30
 * 描述：
 */

public interface BaseView {
    /**
     * 显示dialog
     */
    void showLoading(String dialogMessage);
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
     * 错误码
     */
    void onErrorCode(BaseModelEntity model);

}
