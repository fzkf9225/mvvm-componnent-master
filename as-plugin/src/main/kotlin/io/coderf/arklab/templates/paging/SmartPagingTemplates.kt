package io.coderf.arklab.templates.paging

import com.android.tools.idea.wizard.template.Category
import com.android.tools.idea.wizard.template.Constraint
import com.android.tools.idea.wizard.template.FormFactor
import com.android.tools.idea.wizard.template.ModuleTemplateData
import com.android.tools.idea.wizard.template.PackageNameWidget
import com.android.tools.idea.wizard.template.TemplateData
import com.android.tools.idea.wizard.template.TextFieldWidget
import com.android.tools.idea.wizard.template.Thumb
import com.android.tools.idea.wizard.template.WizardUiContext
import com.android.tools.idea.wizard.template.impl.defaultPackageNameParameter
import com.android.tools.idea.wizard.template.stringParameter
import com.android.tools.idea.wizard.template.template
import io.coderf.arklab.templates.common.ModuleContext

val smartPagingFragmentTemplate
    get() = template {
        name = "SmartPaging Fragment"
        description = "BaseSmartPagingFragment（LiveData）+ Adapter/Bean/ViewModel/Repository 空白模板"
        minApi = 24
        category = Category.Fragment
        formFactor = FormFactor.Mobile
        screens = listOf(WizardUiContext.FragmentGallery, WizardUiContext.MenuEntry)
        thumb = { Thumb.NoThumb }

        val featureName = stringParameter {
            name = "Feature Name"
            default = "Blank"
            help = "不含 Fragment 后缀，如 Order → OrderFragment / OrderViewModel ..."
            constraints = listOf(Constraint.CLASS, Constraint.NONEMPTY, Constraint.UNIQUE)
        }
        val packageName = defaultPackageNameParameter
        widgets(TextFieldWidget(featureName), PackageNameWidget(packageName))

        recipe = { data: TemplateData ->
            generatePagingStack(
                ctx = ModuleContext(data as ModuleTemplateData, packageName.value),
                names = PagingNames(featureName.value),
                flow = false,
                includeActivity = false,
                activityTitle = featureName.value,
            )
        }
    }

val smartFlowPagingFragmentTemplate
    get() = template {
        name = "SmartFlowPaging Fragment"
        description = "BaseSmartPagingFragment（Flow）+ Adapter/Bean/ViewModel/Repository 空白模板"
        minApi = 24
        category = Category.Fragment
        formFactor = FormFactor.Mobile
        screens = listOf(WizardUiContext.FragmentGallery, WizardUiContext.MenuEntry)
        thumb = { Thumb.NoThumb }

        val featureName = stringParameter {
            name = "Feature Name"
            default = "Blank"
            help = "不含 Fragment 后缀"
            constraints = listOf(Constraint.CLASS, Constraint.NONEMPTY, Constraint.UNIQUE)
        }
        val packageName = defaultPackageNameParameter
        widgets(TextFieldWidget(featureName), PackageNameWidget(packageName))

        recipe = { data: TemplateData ->
            generatePagingStack(
                ctx = ModuleContext(data as ModuleTemplateData, packageName.value),
                names = PagingNames(featureName.value),
                flow = true,
                includeActivity = false,
                activityTitle = featureName.value,
            )
        }
    }

val smartPagingActivityTemplate
    get() = template {
        name = "SmartPaging Activity+Fragment"
        description = "空白 Activity（FragmentContainerView+Navigation）+ SmartPaging Fragment 全套"
        minApi = 24
        category = Category.Activity
        formFactor = FormFactor.Mobile
        screens = listOf(WizardUiContext.ActivityGallery, WizardUiContext.MenuEntry)
        thumb = { Thumb.NoThumb }

        val featureName = stringParameter {
            name = "Feature Name"
            default = "Blank"
            help = "不含 Activity 后缀"
            constraints = listOf(Constraint.CLASS, Constraint.NONEMPTY, Constraint.UNIQUE)
        }
        val activityTitle = stringParameter {
            name = "Title Bar"
            default = "Blank"
            constraints = listOf(Constraint.NONEMPTY)
            suggest = { featureName.value }
        }
        val packageName = defaultPackageNameParameter
        widgets(
            TextFieldWidget(featureName),
            TextFieldWidget(activityTitle),
            PackageNameWidget(packageName),
        )

        recipe = { data: TemplateData ->
            generatePagingStack(
                ctx = ModuleContext(data as ModuleTemplateData, packageName.value),
                names = PagingNames(featureName.value),
                flow = false,
                includeActivity = true,
                activityTitle = activityTitle.value,
            )
        }
    }

val smartFlowPagingActivityTemplate
    get() = template {
        name = "SmartFlowPaging Activity+Fragment"
        description = "空白 Activity（FragmentContainerView+Navigation）+ SmartFlowPaging Fragment 全套"
        minApi = 24
        category = Category.Activity
        formFactor = FormFactor.Mobile
        screens = listOf(WizardUiContext.ActivityGallery, WizardUiContext.MenuEntry)
        thumb = { Thumb.NoThumb }

        val featureName = stringParameter {
            name = "Feature Name"
            default = "Blank"
            help = "不含 Activity 后缀"
            constraints = listOf(Constraint.CLASS, Constraint.NONEMPTY, Constraint.UNIQUE)
        }
        val activityTitle = stringParameter {
            name = "Title Bar"
            default = "Blank"
            constraints = listOf(Constraint.NONEMPTY)
            suggest = { featureName.value }
        }
        val packageName = defaultPackageNameParameter
        widgets(
            TextFieldWidget(featureName),
            TextFieldWidget(activityTitle),
            PackageNameWidget(packageName),
        )

        recipe = { data: TemplateData ->
            generatePagingStack(
                ctx = ModuleContext(data as ModuleTemplateData, packageName.value),
                names = PagingNames(featureName.value),
                flow = true,
                includeActivity = true,
                activityTitle = activityTitle.value,
            )
        }
    }
