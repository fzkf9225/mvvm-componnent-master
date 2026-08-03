package io.coderf.arklab.templates

import com.android.tools.idea.wizard.template.Template
import com.android.tools.idea.wizard.template.WizardTemplateProvider
import io.coderf.arklab.templates.basemvvm.baseMvvmActivityTemplate
import io.coderf.arklab.templates.form.formActivityTemplate
import io.coderf.arklab.templates.fragment.baseFragmentTemplate
import io.coderf.arklab.templates.paging.smartFlowPagingActivityTemplate
import io.coderf.arklab.templates.paging.smartFlowPagingFragmentTemplate
import io.coderf.arklab.templates.paging.smartPagingActivityTemplate
import io.coderf.arklab.templates.paging.smartPagingFragmentTemplate
import io.coderf.arklab.templates.recyclerview.recyclerViewActivityTemplate
import io.coderf.arklab.templates.recyclerview.recyclerViewFragmentTemplate

class ArkLabTemplateProvider : WizardTemplateProvider() {
    override fun getTemplates(): List<Template> = listOf(
        baseMvvmActivityTemplate,
        baseFragmentTemplate,
        smartPagingFragmentTemplate,
        smartFlowPagingFragmentTemplate,
        recyclerViewFragmentTemplate,
        smartPagingActivityTemplate,
        smartFlowPagingActivityTemplate,
        recyclerViewActivityTemplate,
        formActivityTemplate,
    )
}
