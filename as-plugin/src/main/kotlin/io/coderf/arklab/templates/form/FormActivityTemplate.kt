package io.coderf.arklab.templates.form

import com.android.tools.idea.wizard.template.Category
import com.android.tools.idea.wizard.template.Constraint
import com.android.tools.idea.wizard.template.FormFactor
import com.android.tools.idea.wizard.template.ModuleTemplateData
import com.android.tools.idea.wizard.template.PackageNameWidget
import com.android.tools.idea.wizard.template.TemplateData
import com.android.tools.idea.wizard.template.TextFieldWidget
import com.android.tools.idea.wizard.template.Thumb
import com.android.tools.idea.wizard.template.WizardUiContext
import com.android.tools.idea.wizard.template.activityToLayout
import com.android.tools.idea.wizard.template.impl.defaultPackageNameParameter
import com.android.tools.idea.wizard.template.stringParameter
import com.android.tools.idea.wizard.template.template
import io.coderf.arklab.templates.common.FileHeader
import io.coderf.arklab.templates.common.ModuleContext
import io.coderf.arklab.templates.common.Naming
import io.coderf.arklab.templates.common.registerActivity
import java.io.File

val formActivityTemplate
    get() = template {
        name = "表单 Activity"
        description = "表单页：Activity + 精简布局（ScrollView + CornerButton 提交）+ ViewModel + Repository"
        minApi = 24
        category = Category.Activity
        formFactor = FormFactor.Mobile
        screens = listOf(WizardUiContext.ActivityGallery, WizardUiContext.MenuEntry)
        thumb = { Thumb.NoThumb }

        val activityClass = stringParameter {
            name = "Activity Name"
            default = "FormActivity"
            constraints = listOf(Constraint.CLASS, Constraint.ACTIVITY, Constraint.NONEMPTY)
        }
        val layoutName = stringParameter {
            name = "Layout Name"
            default = "activity_form"
            constraints = listOf(Constraint.LAYOUT, Constraint.UNIQUE, Constraint.NONEMPTY)
            suggest = { activityToLayout(activityClass.value) }
        }
        val activityTitle = stringParameter {
            name = "Title Bar"
            default = "表单"
            constraints = listOf(Constraint.NONEMPTY)
            suggest = { Naming.featureFromActivity(activityClass.value) }
        }
        val packageName = defaultPackageNameParameter
        widgets(
            TextFieldWidget(activityClass),
            TextFieldWidget(layoutName),
            TextFieldWidget(activityTitle),
            PackageNameWidget(packageName),
        )

        recipe = { data: TemplateData ->
            formActivityRecipe(
                moduleData = data as ModuleTemplateData,
                activityClass = activityClass.value,
                layoutName = layoutName.value,
                activityTitle = activityTitle.value,
                packageName = packageName.value,
            )
        }
    }

fun com.android.tools.idea.wizard.template.RecipeExecutor.formActivityRecipe(
    moduleData: ModuleTemplateData,
    activityClass: String,
    layoutName: String,
    activityTitle: String,
    packageName: String,
) {
    val ctx = ModuleContext(moduleData, packageName)
    val feature = Naming.featureFromActivity(activityClass)
    val viewModelClass = "${feature}ViewModel"
    val repositoryClass = "${feature}RepositoryImpl"
    val bindingClass = Naming.layoutToBindingClass(layoutName)
    val activityPackage = ctx.pkg("activity")
    val viewModelPackage = ctx.pkg("viewmodel")
    val repositoryPackage = ctx.pkg("repository")
    val header = FileHeader.java()

    createDirectory(ctx.dir("activity"))
    createDirectory(ctx.dir("viewmodel"))
    createDirectory(ctx.dir("repository"))

    val activityFile = File(ctx.dir("activity"), "$activityClass.java")
    val viewModelFile = File(ctx.dir("viewmodel"), "$viewModelClass.java")
    val repositoryFile = File(ctx.dir("repository"), "$repositoryClass.java")
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
        binding.verifySubmit.setOnClickListener(v -> {
        });
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
import io.coderf.arklab.common.base.BaseView;
import io.coderf.arklab.common.base.BaseViewModel;
import $repositoryPackage.$repositoryClass;

$header
@HiltViewModel
public class $viewModelClass extends BaseViewModel<$repositoryClass, BaseView> {

    @Inject
    public $viewModelClass(@NonNull Application application) {
        super(application);
    }

    @Override
    protected $repositoryClass createRepository() {
        return new $repositoryClass(baseView);
    }
}
""".trimIndent() + "\n",
        viewModelFile,
    )

    save(
        """
package $repositoryPackage;

import io.coderf.arklab.common.base.BaseRepository;
import io.coderf.arklab.common.base.BaseView;

$header
public class $repositoryClass extends BaseRepository<BaseView> {

    public $repositoryClass(BaseView baseView) {
        super(baseView);
    }
}
""".trimIndent() + "\n",
        repositoryFile,
    )

    save(
        """
<?xml version="1.0" encoding="utf-8"?>
<layout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools">

    <data>
    </data>

    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@color/default_background"
        tools:context=".activity.$activityClass">

        <ScrollView
            android:id="@+id/scroll_view"
            android:layout_width="0dp"
            android:layout_height="0dp"
            android:background="@color/white"
            app:layout_constraintBottom_toTopOf="@+id/verify_submit"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent"
            app:layout_constraintVertical_weight="1">

            <androidx.constraintlayout.widget.ConstraintLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content" />
        </ScrollView>

        <io.coderf.arklab.common.widget.customview.CornerButton
            android:id="@+id/verify_submit"
            android:layout_width="match_parent"
            android:layout_height="@dimen/height_xxxl"
            android:gravity="center"
            android:text="提交"
            android:textColor="@color/white"
            android:textSize="@dimen/font_size_xxl"
            app:bgColor="@color/themeColor"
            app:radius="0dp"
            app:strokeWidth="0dp"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintTop_toBottomOf="@+id/scroll_view" />
    </androidx.constraintlayout.widget.ConstraintLayout>
</layout>
""".trimIndent() + "\n",
        layoutFile,
    )

    registerActivity(ctx.manifestOut, "$activityPackage.$activityClass")
    open(activityFile)
    open(viewModelFile)
    open(repositoryFile)
    open(layoutFile)
}
