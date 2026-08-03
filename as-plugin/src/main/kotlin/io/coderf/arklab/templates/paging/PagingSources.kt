package io.coderf.arklab.templates.paging

import com.android.tools.idea.wizard.template.RecipeExecutor
import io.coderf.arklab.templates.common.FileHeader
import io.coderf.arklab.templates.common.ModuleContext
import io.coderf.arklab.templates.common.Naming
import io.coderf.arklab.templates.common.registerActivity
import java.io.File

data class PagingNames(
    val feature: String,
    val fragmentClass: String = "${feature}Fragment",
    val activityClass: String = "${feature}Activity",
    val viewModelClass: String = "${feature}ViewModel",
    val repositoryClass: String = "${feature}RepositoryImpl",
    val adapterClass: String = "${feature}Adapter",
    val beanClass: String = "${feature}Bean",
    val itemLayout: String = "item_${Naming.toSnake(feature)}",
    val activityLayout: String = "activity_${Naming.toSnake(feature)}",
    val navGraph: String = "${Naming.toSnake(feature)}_navigation",
    val fragmentContainerId: String = "${Naming.toSnake(feature)}_fragment_view",
    val navFragmentId: String = "${Naming.toSnake(feature)}_page",
)

fun RecipeExecutor.generatePagingStack(
    ctx: ModuleContext,
    names: PagingNames,
    flow: Boolean,
    includeActivity: Boolean,
    activityTitle: String,
) {
    val header = FileHeader.java()
    val ktHeader = FileHeader.kotlin()
    val fragmentPackage = ctx.pkg("fragment")
    val activityPackage = ctx.pkg("activity")
    val viewModelPackage = ctx.pkg("viewmodel")
    val repositoryPackage = ctx.pkg("repository")
    val adapterPackage = ctx.pkg("adapter")
    val beanPackage = ctx.pkg("bean")
    val itemBinding = Naming.layoutToBindingClass(names.itemLayout)
    val activityBinding = Naming.layoutToBindingClass(names.activityLayout)

    val dirs = listOf("fragment", "viewmodel", "repository", "adapter", "bean").map { ctx.dir(it) }
    dirs.forEach { createDirectory(it) }
    if (includeActivity) createDirectory(ctx.dir("activity"))
    createDirectory(File(ctx.resOut, "layout"))
    createDirectory(File(ctx.resOut, "navigation"))

    // Bean
    val beanFile = File(ctx.dir("bean"), "${names.beanClass}.java")
    save(
        """
package $beanPackage;

import io.coderf.arklab.common.bean.base.BasePagingBean;

$header
public class ${names.beanClass} extends BasePagingBean {
}
""".trimIndent() + "\n",
        beanFile,
    )

    // Item layout
    val itemLayoutFile = File(ctx.resOut, "layout${File.separator}${names.itemLayout}.xml")
    save(
        """
<?xml version="1.0" encoding="utf-8"?>
<layout xmlns:android="http://schemas.android.com/apk/res/android">

    <data>
        <variable
            name="item"
            type="$beanPackage.${names.beanClass}" />
    </data>

    <FrameLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:padding="16dp"
        android:background="@color/white" />
</layout>
""".trimIndent() + "\n",
        itemLayoutFile,
    )

    // Adapter (Java)
    val adapterFile = File(ctx.dir("adapter"), "${names.adapterClass}.java")
    save(
        """
package $adapterPackage;

import ${ctx.appPackage}.BR;
import ${ctx.appPackage}.R;
import ${ctx.appPackage}.databinding.$itemBinding;
import $beanPackage.${names.beanClass};
import io.coderf.arklab.common.base.BasePagingAdapter;
import io.coderf.arklab.common.base.BaseViewHolder;
import io.coderf.arklab.common.base.DefaultDiffCallback;

$header
public class ${names.adapterClass} extends BasePagingAdapter<${names.beanClass}, $itemBinding> {

    public ${names.adapterClass}() {
        super(new DefaultDiffCallback<>());
    }

    @Override
    public void onBindHolder(BaseViewHolder<$itemBinding> holder, ${names.beanClass} item, int pos) {
        holder.getBinding().setVariable(BR.item, item);
        holder.getBinding().executePendingBindings();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.${names.itemLayout};
    }
}
""".trimIndent() + "\n",
        adapterFile,
    )

    if (flow) {
        generateFlowRepositoryViewModelFragment(
            ctx, names, header, ktHeader,
            fragmentPackage, viewModelPackage, repositoryPackage, adapterPackage, beanPackage,
        )
    } else {
        generateLiveDataRepositoryViewModelFragment(
            ctx, names, header,
            fragmentPackage, viewModelPackage, repositoryPackage, adapterPackage, beanPackage,
        )
    }

    if (includeActivity) {
        val activityFile = File(ctx.dir("activity"), "${names.activityClass}.java")
        val activityLayoutFile = File(ctx.resOut, "layout${File.separator}${names.activityLayout}.xml")
        val navFile = File(ctx.resOut, "navigation${File.separator}${names.navGraph}.xml")

        save(
            """
package $activityPackage;

import android.os.Bundle;

import dagger.hilt.android.AndroidEntryPoint;
import ${ctx.appPackage}.R;
import ${ctx.appPackage}.databinding.$activityBinding;
import io.coderf.arklab.common.base.BaseActivity;
import io.coderf.arklab.common.viewmodel.EmptyViewModel;

$header
@AndroidEntryPoint
public class ${names.activityClass} extends BaseActivity<EmptyViewModel, $activityBinding> {

    @Override
    protected int getLayoutId() {
        return R.layout.${names.activityLayout};
    }

    @Override
    public String setTitleBar() {
        return "$activityTitle";
    }

    @Override
    public void initView(Bundle savedInstanceState) {
    }

    @Override
    public void initData(Bundle bundle) {
    }
}
""".trimIndent() + "\n",
            activityFile,
        )

        save(
            """
<?xml version="1.0" encoding="utf-8"?>
<layout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools">

    <data>
    </data>

    <androidx.fragment.app.FragmentContainerView
        android:id="@+id/${names.fragmentContainerId}"
        android:name="androidx.navigation.fragment.NavHostFragment"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@color/default_background"
        app:defaultNavHost="true"
        app:navGraph="@navigation/${names.navGraph}"
        tools:context=".activity.${names.activityClass}" />
</layout>
""".trimIndent() + "\n",
            activityLayoutFile,
        )

        save(
            """
<?xml version="1.0" encoding="utf-8"?>
<navigation xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/${names.navGraph}"
    app:startDestination="@+id/${names.navFragmentId}">

    <fragment
        android:id="@+id/${names.navFragmentId}"
        android:name="$fragmentPackage.${names.fragmentClass}"
        android:label="${names.feature}"
        tools:layout="@layout/base_smart_paging" />
</navigation>
""".trimIndent() + "\n",
            navFile,
        )

        registerActivity(ctx.manifestOut, "$activityPackage.${names.activityClass}")
        open(activityFile)
        open(activityLayoutFile)
        open(navFile)
    }

    open(beanFile)
    open(adapterFile)
    open(itemLayoutFile)
}

private fun RecipeExecutor.generateLiveDataRepositoryViewModelFragment(
    ctx: ModuleContext,
    names: PagingNames,
    header: String,
    fragmentPackage: String,
    viewModelPackage: String,
    repositoryPackage: String,
    adapterPackage: String,
    beanPackage: String,
) {
    val repositoryFile = File(ctx.dir("repository"), "${names.repositoryClass}.java")
    save(
        """
package $repositoryPackage;

import java.util.Collections;
import java.util.List;

import io.coderf.arklab.common.base.BaseView;
import io.coderf.arklab.common.repository.PagingRepositoryImpl;
import ${ctx.appPackage}.api.ApiServiceHelper;
import $beanPackage.${names.beanClass};
import io.reactivex.rxjava3.core.Observable;

$header
public class ${names.repositoryClass} extends PagingRepositoryImpl<ApiServiceHelper, ${names.beanClass}, BaseView> {

    public ${names.repositoryClass}(BaseView baseView, ApiServiceHelper apiService) {
        super(baseView, apiService);
    }

    @Override
    public Observable<List<${names.beanClass}>> requestPaging(int currentPage, int pageSize) {
        return Observable.just(Collections.emptyList());
    }
}
""".trimIndent() + "\n",
        repositoryFile,
    )

    val viewModelFile = File(ctx.dir("viewmodel"), "${names.viewModelClass}.java")
    save(
        """
package $viewModelPackage;

import android.app.Application;

import androidx.annotation.NonNull;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.coderf.arklab.common.api.RepositoryFactory;
import io.coderf.arklab.common.base.BaseView;
import io.coderf.arklab.common.viewmodel.PagingViewModel;
import ${ctx.appPackage}.api.ApiServiceHelper;
import $beanPackage.${names.beanClass};
import $repositoryPackage.${names.repositoryClass};

$header
@HiltViewModel
public class ${names.viewModelClass} extends PagingViewModel<${names.repositoryClass}, ${names.beanClass}, BaseView> {

    @Inject
    ApiServiceHelper apiServiceHelper;

    @Inject
    public ${names.viewModelClass}(@NonNull Application application) {
        super(application);
    }

    @Override
    protected ${names.repositoryClass} createRepository() {
        return RepositoryFactory.create(${names.repositoryClass}.class, baseView, apiServiceHelper);
    }
}
""".trimIndent() + "\n",
        viewModelFile,
    )

    val fragmentFile = File(ctx.dir("fragment"), "${names.fragmentClass}.java")
    save(
        """
package $fragmentPackage;

import android.os.Bundle;

import dagger.hilt.android.AndroidEntryPoint;
import io.coderf.arklab.common.base.BasePagingAdapter;
import io.coderf.arklab.common.base.BaseSmartPagingFragment;
import io.coderf.arklab.common.databinding.BaseSmartPagingBinding;
import $adapterPackage.${names.adapterClass};
import $beanPackage.${names.beanClass};
import $viewModelPackage.${names.viewModelClass};

$header
@AndroidEntryPoint
public class ${names.fragmentClass} extends BaseSmartPagingFragment<${names.viewModelClass}, BaseSmartPagingBinding, ${names.beanClass}> {

    @Override
    protected BasePagingAdapter<${names.beanClass}, ?> getRecyclerAdapter() {
        return new ${names.adapterClass}();
    }

    @Override
    protected void initData(Bundle bundle) {
        super.initData(bundle);
        mViewModel.getItems().observe(this, observer);
        onRefresh(binding.smartFreshLayout);
    }
}
""".trimIndent() + "\n",
        fragmentFile,
    )

    open(repositoryFile)
    open(viewModelFile)
    open(fragmentFile)
}

private fun RecipeExecutor.generateFlowRepositoryViewModelFragment(
    ctx: ModuleContext,
    names: PagingNames,
    header: String,
    ktHeader: String,
    fragmentPackage: String,
    viewModelPackage: String,
    repositoryPackage: String,
    adapterPackage: String,
    beanPackage: String,
) {
    val repositoryFile = File(ctx.dir("repository"), "${names.repositoryClass}.kt")
    save(
        """
package $repositoryPackage

import io.coderf.arklab.common.base.BaseView
import io.coderf.arklab.common.repository.PagingFlowRepositoryImpl
import ${ctx.appPackage}.api.ApiServiceHelper
import $beanPackage.${names.beanClass}
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

$ktHeader
class ${names.repositoryClass} :
    PagingFlowRepositoryImpl<ApiServiceHelper, ${names.beanClass}, BaseView> {

    constructor(baseView: BaseView, apiService: ApiServiceHelper) : super(baseView, apiService)

    override suspend fun requestPaging(
        currentPage: Int,
        pageSize: Int
    ): Flow<List<${names.beanClass}>>? {
        return flowOf(emptyList())
    }
}
""".trimIndent() + "\n",
        repositoryFile,
    )

    val viewModelFile = File(ctx.dir("viewmodel"), "${names.viewModelClass}.kt")
    save(
        """
package $viewModelPackage

import android.app.Application
import dagger.hilt.android.lifecycle.HiltViewModel
import io.coderf.arklab.common.api.RepositoryFactory
import io.coderf.arklab.common.base.BaseView
import io.coderf.arklab.common.viewmodel.FlowPagingViewModel
import ${ctx.appPackage}.api.ApiServiceHelper
import $beanPackage.${names.beanClass}
import $repositoryPackage.${names.repositoryClass}
import javax.inject.Inject

$ktHeader
@HiltViewModel
class ${names.viewModelClass} @Inject constructor(application: Application) :
    FlowPagingViewModel<${names.repositoryClass}, ${names.beanClass}, BaseView>(application) {

    @Inject
    lateinit var apiServiceHelper: ApiServiceHelper

    override fun createRepository(): ${names.repositoryClass} {
        return RepositoryFactory.createFlow(
            ${names.repositoryClass}::class.java,
            baseView,
            apiServiceHelper
        )
    }
}
""".trimIndent() + "\n",
        viewModelFile,
    )

    val fragmentFile = File(ctx.dir("fragment"), "${names.fragmentClass}.kt")
    save(
        """
package $fragmentPackage

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.coderf.arklab.common.base.BaseSmartPagingFragment
import io.coderf.arklab.common.databinding.BaseSmartPagingBinding
import $adapterPackage.${names.adapterClass}
import $beanPackage.${names.beanClass}
import $viewModelPackage.${names.viewModelClass}
import kotlinx.coroutines.launch

$ktHeader
@AndroidEntryPoint
class ${names.fragmentClass} :
    BaseSmartPagingFragment<${names.viewModelClass}, BaseSmartPagingBinding, ${names.beanClass}>() {

    override fun getRecyclerAdapter() = ${names.adapterClass}()

    override fun initData(bundle: Bundle?) {
        super.initData(bundle)
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mViewModel.dataFlow.collect { pagingData ->
                    adapter.submitData(viewLifecycleOwner.lifecycle, pagingData)
                }
            }
        }
    }
}
""".trimIndent() + "\n",
        fragmentFile,
    )

    open(repositoryFile)
    open(viewModelFile)
    open(fragmentFile)
}
