package io.coderf.arklab.templates.fragment

import com.android.tools.idea.wizard.template.Category
import com.android.tools.idea.wizard.template.Constraint
import com.android.tools.idea.wizard.template.FormFactor
import com.android.tools.idea.wizard.template.ModuleTemplateData
import com.android.tools.idea.wizard.template.PackageNameWidget
import com.android.tools.idea.wizard.template.RecipeExecutor
import com.android.tools.idea.wizard.template.TemplateData
import com.android.tools.idea.wizard.template.TextFieldWidget
import com.android.tools.idea.wizard.template.Thumb
import com.android.tools.idea.wizard.template.WizardUiContext
import com.android.tools.idea.wizard.template.classToResource
import com.android.tools.idea.wizard.template.impl.defaultPackageNameParameter
import com.android.tools.idea.wizard.template.stringParameter
import com.android.tools.idea.wizard.template.template
import io.coderf.arklab.templates.common.FileHeader
import io.coderf.arklab.templates.common.ModuleContext
import io.coderf.arklab.templates.common.Naming
import java.io.File

val baseFragmentTemplate
    get() = template {
        name = "BaseFragment 空白页"
        description = "继承 BaseFragment 的空白 Fragment（含 newInstance）+ ViewModel + 布局"
        minApi = 24
        category = Category.Fragment
        formFactor = FormFactor.Mobile
        screens = listOf(WizardUiContext.FragmentGallery, WizardUiContext.MenuEntry)
        thumb = { Thumb.NoThumb }

        val fragmentClass = stringParameter {
            name = "Fragment Name"
            default = "BlankFragment"
            help = "Fragment 类名，需以 Fragment 结尾"
            constraints = listOf(Constraint.CLASS, Constraint.NONEMPTY, Constraint.UNIQUE)
        }
        val layoutName = stringParameter {
            name = "Layout Name"
            default = "fragment_blank"
            constraints = listOf(Constraint.LAYOUT, Constraint.UNIQUE, Constraint.NONEMPTY)
            suggest = { "fragment_${classToResource(fragmentClass.value.removeSuffix("Fragment"))}" }
        }
        val packageName = defaultPackageNameParameter
        widgets(
            TextFieldWidget(fragmentClass),
            TextFieldWidget(layoutName),
            PackageNameWidget(packageName),
        )

        recipe = { data: TemplateData ->
            baseFragmentRecipe(
                moduleData = data as ModuleTemplateData,
                fragmentClass = fragmentClass.value,
                layoutName = layoutName.value,
                packageName = packageName.value,
            )
        }
    }

fun RecipeExecutor.baseFragmentRecipe(
    moduleData: ModuleTemplateData,
    fragmentClass: String,
    layoutName: String,
    packageName: String,
) {
    val ctx = ModuleContext(moduleData, packageName)
    val feature = Naming.featureFromFragment(fragmentClass)
    val viewModelClass = "${feature}ViewModel"
    val bindingClass = Naming.layoutToBindingClass(layoutName)
    val fragmentPackage = ctx.pkg("fragment")
    val viewModelPackage = ctx.pkg("viewmodel")
    val header = FileHeader.java()

    createDirectory(ctx.dir("fragment"))
    createDirectory(ctx.dir("viewmodel"))

    val fragmentFile = File(ctx.dir("fragment"), "$fragmentClass.java")
    val viewModelFile = File(ctx.dir("viewmodel"), "$viewModelClass.java")
    val layoutFile = File(ctx.resOut, "layout${File.separator}$layoutName.xml")

    save(
        """
package $fragmentPackage;

import android.os.Bundle;

import dagger.hilt.android.AndroidEntryPoint;
import ${ctx.appPackage}.R;
import ${ctx.appPackage}.databinding.$bindingClass;
import $viewModelPackage.$viewModelClass;
import io.coderf.arklab.common.base.BaseFragment;

$header
@AndroidEntryPoint
public class $fragmentClass extends BaseFragment<$viewModelClass, $bindingClass> {

    public static $fragmentClass newInstance() {
        Bundle args = new Bundle();
        $fragmentClass fragment = new $fragmentClass();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.$layoutName;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
    }

    @Override
    protected void initData(Bundle bundle) {
    }
}
""".trimIndent() + "\n",
        fragmentFile,
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

    open(fragmentFile)
    open(viewModelFile)
    open(layoutFile)
}
