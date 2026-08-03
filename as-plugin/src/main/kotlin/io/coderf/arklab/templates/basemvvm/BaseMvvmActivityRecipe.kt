package io.coderf.arklab.templates.basemvvm

import com.android.tools.idea.wizard.template.ModuleTemplateData
import com.android.tools.idea.wizard.template.RecipeExecutor
import io.coderf.arklab.templates.common.FileHeader
import io.coderf.arklab.templates.common.ModuleContext
import io.coderf.arklab.templates.common.Naming
import io.coderf.arklab.templates.common.registerActivity
import java.io.File

fun RecipeExecutor.baseMvvmActivityRecipe(
    moduleData: ModuleTemplateData,
    activityClass: String,
    layoutName: String,
    activityTitle: String,
    packageName: String,
) {
    val ctx = ModuleContext(moduleData, packageName)
    val featureName = Naming.featureFromActivity(activityClass)
    val viewModelClass = "${featureName}ViewModel"
    val bindingClass = Naming.layoutToBindingClass(layoutName)
    val activityPackage = ctx.pkg("activity")
    val viewModelPackage = ctx.pkg("viewmodel")
    val header = FileHeader.java()

    val activityDir = ctx.dir("activity")
    val viewModelDir = ctx.dir("viewmodel")
    createDirectory(activityDir)
    createDirectory(viewModelDir)

    val activityFile = File(activityDir, "$activityClass.java")
    val viewModelFile = File(viewModelDir, "$viewModelClass.java")
    val layoutFile = File(ctx.resOut, "layout${File.separator}$layoutName.xml")

    save(
        """
package $activityPackage;

import android.os.Bundle;

import dagger.hilt.android.AndroidEntryPoint;
import ${ctx.appPackage}.R;
import ${ctx.appPackage}.databinding.$bindingClass;
import $viewModelPackage.$viewModelClass;
import io.coderf.arklab.common.base.BaseActivity;

$header
@AndroidEntryPoint
public class $activityClass extends BaseActivity<$viewModelClass, $bindingClass> {

    @Override
    protected int getLayoutId() {
        return R.layout.$layoutName;
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
package $viewModelPackage;

import android.app.Application;

import androidx.annotation.NonNull;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.coderf.arklab.common.base.BaseRepository;
import io.coderf.arklab.common.base.BaseView;
import io.coderf.arklab.common.base.BaseViewModel;

$header
@HiltViewModel
public class $viewModelClass extends BaseViewModel<BaseRepository<BaseView>, BaseView> {

    @Inject
    public $viewModelClass(@NonNull Application application) {
        super(application);
    }

    @Override
    protected BaseRepository<BaseView> createRepository() {
        return null;
    }
}
""".trimIndent() + "\n",
        viewModelFile,
    )

    save(
        """
<?xml version="1.0" encoding="utf-8"?>
<layout xmlns:android="http://schemas.android.com/apk/res/android">

    <data>
    </data>

    <FrameLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@color/default_background" />
</layout>
""".trimIndent() + "\n",
        layoutFile,
    )

    registerActivity(ctx.manifestOut, "$activityPackage.$activityClass")
    open(activityFile)
    open(viewModelFile)
    open(layoutFile)
}
