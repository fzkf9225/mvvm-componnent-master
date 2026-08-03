package io.coderf.arklab.templates.recyclerview

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

val recyclerViewFragmentTemplate
    get() = template {
        name = "BaseRecyclerView Fragment"
        description = "BaseRecyclerViewFragment + Adapter/Bean/ViewModel/Repository 空白模板"
        minApi = 24
        category = Category.Fragment
        formFactor = FormFactor.Mobile
        screens = listOf(WizardUiContext.FragmentGallery, WizardUiContext.MenuEntry)
        thumb = { Thumb.NoThumb }

        val featureName = stringParameter {
            name = "Feature Name"
            default = "Blank"
            constraints = listOf(Constraint.CLASS, Constraint.NONEMPTY, Constraint.UNIQUE)
        }
        val packageName = defaultPackageNameParameter
        widgets(TextFieldWidget(featureName), PackageNameWidget(packageName))

        recipe = { data: TemplateData ->
            generateRecyclerViewStack(
                ctx = ModuleContext(data as ModuleTemplateData, packageName.value),
                names = RecyclerNames(featureName.value),
                includeActivity = false,
                activityTitle = featureName.value,
            )
        }
    }

val recyclerViewActivityTemplate
    get() = template {
        name = "BaseRecyclerView Activity+Fragment"
        description = "空白 Activity（FragmentContainerView+Navigation）+ BaseRecyclerView Fragment 全套"
        minApi = 24
        category = Category.Activity
        formFactor = FormFactor.Mobile
        screens = listOf(WizardUiContext.ActivityGallery, WizardUiContext.MenuEntry)
        thumb = { Thumb.NoThumb }

        val featureName = stringParameter {
            name = "Feature Name"
            default = "Blank"
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
            generateRecyclerViewStack(
                ctx = ModuleContext(data as ModuleTemplateData, packageName.value),
                names = RecyclerNames(featureName.value),
                includeActivity = true,
                activityTitle = activityTitle.value,
            )
        }
    }
