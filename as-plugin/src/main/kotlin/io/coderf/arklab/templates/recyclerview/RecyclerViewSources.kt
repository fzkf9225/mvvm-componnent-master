package io.coderf.arklab.templates.recyclerview

import com.android.tools.idea.wizard.template.RecipeExecutor
import io.coderf.arklab.templates.common.FileHeader
import io.coderf.arklab.templates.common.ModuleContext
import io.coderf.arklab.templates.common.Naming
import io.coderf.arklab.templates.common.registerActivity
import java.io.File

data class RecyclerNames(
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

fun RecipeExecutor.generateRecyclerViewStack(
    ctx: ModuleContext,
    names: RecyclerNames,
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

    listOf("fragment", "viewmodel", "repository", "adapter", "bean").forEach {
        createDirectory(ctx.dir(it))
    }
    if (includeActivity) createDirectory(ctx.dir("activity"))
    createDirectory(File(ctx.resOut, "layout"))
    createDirectory(File(ctx.resOut, "navigation"))

    val beanFile = File(ctx.dir("bean"), "${names.beanClass}.java")
    save(
        """
package $beanPackage;

$header
public class ${names.beanClass} {
}
""".trimIndent() + "\n",
        beanFile,
    )

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

    val adapterFile = File(ctx.dir("adapter"), "${names.adapterClass}.java")
    save(
        """
package $adapterPackage;

import ${ctx.appPackage}.BR;
import ${ctx.appPackage}.R;
import ${ctx.appPackage}.databinding.$itemBinding;
import $beanPackage.${names.beanClass};
import io.coderf.arklab.common.base.BaseRecyclerViewAdapter;
import io.coderf.arklab.common.base.BaseViewHolder;

$header
public class ${names.adapterClass} extends BaseRecyclerViewAdapter<${names.beanClass}, $itemBinding> {

    @Override
    public void onBindHolder(BaseViewHolder<$itemBinding> holder, int pos) {
        holder.getBinding().setVariable(BR.item, mList.get(pos));
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

    val repositoryFile = File(ctx.dir("repository"), "${names.repositoryClass}.java")
    save(
        """
package $repositoryPackage;

import io.coderf.arklab.common.base.BaseRepository;
import io.coderf.arklab.common.base.BaseView;

$header
public class ${names.repositoryClass} extends BaseRepository<BaseView> {

    public ${names.repositoryClass}(BaseView baseView) {
        super(baseView);
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
import io.coderf.arklab.common.base.BaseRecyclerViewModel;
import io.coderf.arklab.common.base.BaseView;
import io.coderf.arklab.common.bean.base.PageBean;
import $beanPackage.${names.beanClass};
import $repositoryPackage.${names.repositoryClass};

$header
@HiltViewModel
public class ${names.viewModelClass} extends BaseRecyclerViewModel<${names.repositoryClass}, BaseView, ${names.beanClass}> {

    @Inject
    public ${names.viewModelClass}(@NonNull Application application) {
        super(application);
    }

    @Override
    protected ${names.repositoryClass} createRepository() {
        return new ${names.repositoryClass}(baseView);
    }

    public void loadData(int mCurrentPage) {
        PageBean<${names.beanClass}> pageBean = new PageBean<>();
        listLiveData.setValue(pageBean);
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
import dagger.hilt.android.AndroidEntryPoint
import io.coderf.arklab.common.base.BaseRecyclerViewFragment
import io.coderf.arklab.common.databinding.SmartrecyclerviewBinding
import $adapterPackage.${names.adapterClass}
import $beanPackage.${names.beanClass}
import $viewModelPackage.${names.viewModelClass}

$ktHeader
@AndroidEntryPoint
class ${names.fragmentClass} :
    BaseRecyclerViewFragment<${names.viewModelClass}, SmartrecyclerviewBinding, ${names.beanClass}>() {

    override fun initData(bundle: Bundle?) {
        super.initData(bundle)
        requestData()
    }

    override fun requestData() {
        super.requestData()
        mViewModel?.loadData(mCurrentPage)
    }

    override fun getRecyclerAdapter() = ${names.adapterClass}().apply {
        setOnItemClickListener(this@${names.fragmentClass})
        setOnItemLongClickListener(this@${names.fragmentClass})
    }
}
""".trimIndent() + "\n",
        fragmentFile,
    )

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
        tools:layout="@layout/smartrecyclerview" />
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
    open(repositoryFile)
    open(viewModelFile)
    open(fragmentFile)
}
