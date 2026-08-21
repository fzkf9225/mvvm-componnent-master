package io.coderf.arklab.common.inter;

import io.coderf.arklab.common.base.BaseResponse;

/**
 * 数据层（Repository）与界面之间的「请求侧 UI」通道：加载框、Toast、业务错误码回调。
 * <p>
 * 约定：
 * <ul>
 *   <li>Repository 内<strong>禁止</strong>再直接调用 {@code baseView.showLoading} / {@code onErrorCode} 等；只调用本接口。</li>
 *   <li>默认由 {@link io.coderf.arklab.common.base.BaseViewModel} 注入 {@link io.coderf.arklab.common.base.NetworkRequestUiHost}，
 *   页面经 {@link io.coderf.arklab.common.base.NetworkRequestUiBinder} 订阅后落到 {@link io.coderf.arklab.common.base.BaseView}。</li>
 *   <li>{@link io.coderf.arklab.common.base.BaseView} 仍可由 {@link io.coderf.arklab.common.repository.IRepository#setBaseView} 持有，
 *   表示「当前页面」，供非网络 UI 的遗留逻辑使用；请求相关 UI 一律走本接口。</li>
 * </ul>
 */
public interface RequestUiCallback {

    void showLoading(String dialogMessage, boolean enableDynamicEllipsis);

    void hideLoading();

    void refreshLoading(String dialogMessage);

    void showToast(String msg);

    void onErrorCode(BaseResponse<?> model);
}
