package io.coderf.arklab.core.autosize.lifecycle;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import io.coderf.arklab.core.autosize.strategy.AutoAdaptStrategy;

/**
 * {@link FragmentLifecycleCallbacksImplToAndroidx} 可用来代替在 BaseFragment 中加入适配代码的传统方式
 * {@link FragmentLifecycleCallbacksImplToAndroidx} 这种方案类似于 AOP, 面向接口, 侵入性低, 方便统一管理, 扩展性强, 并且也支持适配三方库的 {@link Fragment}
 * <p>
 *
 * @author fz
 * @version 1.0
 * @since 1.0
 * @created 2018/8/25 13:52
 */
public class FragmentLifecycleCallbacksImplToAndroidx extends FragmentManager.FragmentLifecycleCallbacks {
    /**
     * 屏幕适配逻辑策略类
     */
    private AutoAdaptStrategy mAutoAdaptStrategy;

    public FragmentLifecycleCallbacksImplToAndroidx(AutoAdaptStrategy autoAdaptStrategy) {
        mAutoAdaptStrategy = autoAdaptStrategy;
    }

    @Override
    public void onFragmentCreated(FragmentManager fm, Fragment f, Bundle savedInstanceState) {
        if (mAutoAdaptStrategy != null) {
            mAutoAdaptStrategy.applyAdapt(f, f.getActivity());
        }
    }

    /**
     * 设置屏幕适配逻辑策略类
     *
     * @param autoAdaptStrategy {@link AutoAdaptStrategy}
     */
    public void setAutoAdaptStrategy(AutoAdaptStrategy autoAdaptStrategy) {
        mAutoAdaptStrategy = autoAdaptStrategy;
    }
}
