package pers.fz.media.listener;

public interface OnLoadingListener {
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


}
