package io.coderf.arklab.common.inter;

import io.coderf.arklab.common.base.BaseResponse;

/**
 * 请求过程 UI 能力：加载框、Toast、业务错误码。
 * <p>
 * 两处使用，职责不同：
 * <ul>
 *   <li><b>写侧</b>：Repository 只调用本接口；默认注入的是
 *   {@link io.coderf.arklab.common.base.NetworkRequestUiHost}（写 LiveData，不碰页面）。</li>
 *   <li><b>渲染侧</b>：{@link io.coderf.arklab.common.base.BaseActivity} /
 *   {@link io.coderf.arklab.common.base.BaseFragment} 实现本接口，由
 *   {@link io.coderf.arklab.common.base.NetworkRequestUiBinder} 把 Host 状态落到真正 UI。</li>
 * </ul>
 * Repository 永远只拿 Host，不要把 Activity/Fragment 当 RequestUiCallback 注入仓库。
 *
 * @author fz
 * @version 1.2
 * @since 1.0
 * @updated 2026/9/12
 */
public interface RequestUiCallback {

    void showLoading(String dialogMessage, boolean enableDynamicEllipsis);

    void hideLoading();

    void refreshLoading(String dialogMessage);

    void showToast(String msg);

    void onErrorCode(BaseResponse<?> model);
}
