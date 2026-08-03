package io.coderf.arklab.templates.basemvvm

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

val baseMvvmActivityTemplate
    get() = template {
        name = "BaseActivity 空白页"
        description =
            "创建继承 BaseActivity 的空白 Activity、同名 Hilt ViewModel 与 DataBinding 布局（无业务代码）"
        minApi = 24
        category = Category.Activity
        formFactor = FormFactor.Mobile
        screens = listOf(
            WizardUiContext.ActivityGallery,
            WizardUiContext.MenuEntry,
        )
        thumb = { Thumb.NoThumb }

        val activityClass = stringParameter {
            name = "Activity Name"
            default = "BlankActivity"
            help = "Activity 类名，需以 Activity 结尾"
            constraints = listOf(Constraint.CLASS, Constraint.ACTIVITY, Constraint.NONEMPTY)
        }

        val layoutName = stringParameter {
            name = "Layout Name"
            default = "activity_blank"
            help = "DataBinding 布局文件名（不含 .xml）"
            constraints = listOf(Constraint.LAYOUT, Constraint.UNIQUE, Constraint.NONEMPTY)
            suggest = { activityToLayout(activityClass.value) }
        }

        val activityTitle = stringParameter {
            name = "Title Bar"
            default = "Blank"
            help = "setTitleBar() 返回值"
            constraints = listOf(Constraint.NONEMPTY)
            suggest = {
                activityClass.value.removeSuffix("Activity").ifBlank { "Blank" }
            }
        }

        val packageName = defaultPackageNameParameter

        widgets(
            TextFieldWidget(activityClass),
            TextFieldWidget(layoutName),
            TextFieldWidget(activityTitle),
            PackageNameWidget(packageName),
        )

        recipe = { data: TemplateData ->
            baseMvvmActivityRecipe(
                moduleData = data as ModuleTemplateData,
                activityClass = activityClass.value,
                layoutName = layoutName.value,
                activityTitle = activityTitle.value,
                packageName = packageName.value,
            )
        }
    }
