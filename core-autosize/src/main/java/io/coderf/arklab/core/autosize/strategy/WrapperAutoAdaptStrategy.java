package io.coderf.arklab.core.autosize.strategy;

import android.app.Activity;

import io.coderf.arklab.core.autosize.AutoSizeConfig;
import io.coderf.arklab.core.autosize.OnAdaptListener;

/**
 * {@link AutoAdaptStrategy} 的包装者, 用于给 {@link AutoAdaptStrategy} 的实现类增加一些额外的职责
 * <p>
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2018/10/30 15:07
 */
public class WrapperAutoAdaptStrategy implements AutoAdaptStrategy {
    private final AutoAdaptStrategy mAutoAdaptStrategy;

    public WrapperAutoAdaptStrategy(AutoAdaptStrategy autoAdaptStrategy) {
        mAutoAdaptStrategy = autoAdaptStrategy;
    }

    @Override
    public void applyAdapt(Object target, Activity activity) {
        // 旋转 / 分屏 / 多窗口：适配前刷新当前窗口尺寸
        if (activity != null) {
            AutoSizeConfig.getInstance().refreshScreenSize(activity);
        }
        OnAdaptListener listener = AutoSizeConfig.getInstance().getOnAdaptListener();
        if (listener != null) {
            listener.onAdaptBefore(target, activity);
        }
        if (mAutoAdaptStrategy != null) {
            mAutoAdaptStrategy.applyAdapt(target, activity);
        }
        if (listener != null) {
            listener.onAdaptAfter(target, activity);
        }
    }
}
