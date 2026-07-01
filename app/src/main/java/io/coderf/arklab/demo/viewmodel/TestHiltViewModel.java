package io.coderf.arklab.demo.viewmodel;

import android.app.Application;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import dagger.Lazy;
import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.android.lifecycle.HiltViewModel;
import io.coderf.arklab.common.base.BaseRepository;
import io.coderf.arklab.common.base.BaseView;
import io.coderf.arklab.common.base.BaseViewModel;
import io.coderf.arklab.common.inter.RetryService;
import io.coderf.arklab.common.repository.RepositoryImpl;
import io.coderf.arklab.demo.R;
import io.coderf.arklab.demo.bean.HiltExpensiveService;
import io.coderf.arklab.demo.bean.HiltSingletonCounter;
import io.coderf.arklab.demo.bean.HiltTestBean;
import io.coderf.arklab.demo.bean.HiltViewModelScopedToken;
import io.coderf.arklab.demo.impl.UserServiceEntryPoint;
import io.coderf.arklab.demo.inter.HiltLogPlugin;
import io.coderf.arklab.demo.inter.HiltUserService;
import io.coderf.arklab.demo.module.HiltUserServiceModule;

/**
 * Created by fz on 2024/5/31 11:31
 * describe :
 */
@HiltViewModel
public class TestHiltViewModel extends BaseViewModel<BaseRepository<BaseView>, BaseView> {
    @Inject
    @HiltUserServiceModule.HiltUser
    HiltUserService hiltUserService;

    @Inject
    HiltTestBean hiltTestBean;

    @Inject
    @HiltUserServiceModule.NewHiltUser
    HiltUserService newHiltUserService;

    @Inject
    @HiltUserServiceModule.ContextHiltUser
    HiltUserService contextHiltUserService;

    @Inject
    public RetryService retryService;

    // ---- 进阶用法演示 ----

    @Inject
    @Named("env_dev")
    String devEnv;

    @Inject
    @Named("env_prod")
    String prodEnv;

    @Inject
    HiltSingletonCounter singletonCounter;

    @Inject
    Lazy<HiltExpensiveService> lazyExpensiveService;

    @Inject
    Provider<HiltExpensiveService> expensiveServiceProvider;

    @Inject
    Set<HiltLogPlugin> logPlugins;

    @Inject
    String logPluginSummary;

    @Inject
    HiltViewModelScopedToken viewModelScopedToken;

    /** 演示结果输出，绑定到界面 TextView。 */
    public final MutableLiveData<String> demoResult = new MutableLiveData<>("点击按钮查看 Hilt 注入效果");

    @Inject
    public TestHiltViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected RepositoryImpl createRepository() {
        return null;
    }

    public void onClick(View view) {
        if (R.id.button_inter == view.getId()) {
            hiltUserService.onLogin("张三", "123456");
            demoResult.setValue("接口注入 @Binds + @HiltUser → 见 Logcat");
        } else if (R.id.button_entity == view.getId()) {
            demoResult.setValue("实体类 @Inject 构造器: " + hiltTestBean);
        } else if (R.id.button_multi_impl == view.getId()) {
            newHiltUserService.onLogin("李四", "000000");
            demoResult.setValue("多实现 @Qualifier @NewHiltUser → 见 Logcat");
        } else if (R.id.button_context_impl == view.getId()) {
            contextHiltUserService.onLogin("王五", "666666");
            demoResult.setValue("Application 注入 @ContextHiltUser → 见 Logcat");
        } else if (R.id.button_entryPoint == view.getId()) {
            UserServiceEntryPoint userServiceEntryPoint = EntryPointAccessors.fromApplication(
                    view.getContext(), UserServiceEntryPoint.class);
            if (userServiceEntryPoint == null) {
                demoResult.setValue("EntryPoint 为空");
            } else {
                userServiceEntryPoint.getHiltUserServiceImpl().onLogin("赵六", "888888");
                demoResult.setValue("EntryPoint 非 Hilt 类中获取依赖 → 见 Logcat");
            }
        } else if (R.id.button_named == view.getId()) {
            demoResult.setValue("@Named 限定符:\n  dev=" + devEnv + "\n  prod=" + prodEnv);
        } else if (R.id.button_singleton == view.getId()) {
            int count = singletonCounter.incrementAndGet();
            demoResult.setValue("@Singleton 单例共享计数: " + count + "\n" + singletonCounter);
        } else if (R.id.button_lazy == view.getId()) {
            HiltExpensiveService first = lazyExpensiveService.get();
            HiltExpensiveService second = lazyExpensiveService.get();
            demoResult.setValue("Lazy<T> 懒加载（同实例）:\n"
                    + first.describe() + "\n"
                    + second.describe() + "\n"
                    + "sameInstance=" + (first == second));
        } else if (R.id.button_provider == view.getId()) {
            HiltExpensiveService first = expensiveServiceProvider.get();
            HiltExpensiveService second = expensiveServiceProvider.get();
            demoResult.setValue("Provider<T> 每次 get() 新实例:\n"
                    + first.describe() + "\n"
                    + second.describe() + "\n"
                    + "sameInstance=" + (first == second));
        } else if (R.id.button_multibinding == view.getId()) {
            StringBuilder sb = new StringBuilder("@IntoSet 多绑定 (").append(logPlugins.size()).append(" 个):\n");
            sb.append("summary=").append(logPluginSummary).append('\n');
            for (HiltLogPlugin plugin : logPlugins) {
                plugin.log("Hilt demo message");
                sb.append(" - ").append(plugin.name()).append('\n');
            }
            demoResult.setValue(sb.toString());
        } else if (R.id.button_viewmodel_scope == view.getId()) {
            demoResult.setValue("ViewModelComponent 作用域:\n"
                    + viewModelScopedToken + "\n"
                    + "（旋转屏幕后 token 不变）");
        }
    }
}
