package io.coderf.arklab.templates.common

import com.android.tools.idea.wizard.template.ModuleTemplateData
import com.android.tools.idea.wizard.template.RecipeExecutor
import java.io.File

class ModuleContext(
    val moduleData: ModuleTemplateData,
    packageName: String,
) {
    val projectData = moduleData.projectTemplateData
    val resOut: File = moduleData.resDir
    val manifestOut: File = moduleData.manifestDir
    val basePackage: String = Naming.basePackage(packageName)
    val appPackage: String = Naming.appPackage(
        moduleData.namespace,
        projectData.applicationPackage,
        basePackage,
    )

    /** e.g. `app/src/main/java/io/coderf/arklab/demo` */
    val packageRoot: File = Naming.resolvePackageRoot(moduleData.srcDir, basePackage)

    /** Subpackage folder under [packageRoot], e.g. dir("repository") → `.../demo/repository` */
    fun dir(vararg segments: String): File =
        File(packageRoot, segments.joinToString(File.separator))

    fun pkg(vararg segments: String): String =
        listOf(basePackage, *segments).joinToString(".")
}

fun RecipeExecutor.registerActivity(manifestOut: File, activityFqn: String) {
    mergeXml(
        """
        <manifest xmlns:android="http://schemas.android.com/apk/res/android">
            <application>
                <activity
                    android:name="$activityFqn"
                    android:exported="false" />
            </application>
        </manifest>
        """.trimIndent(),
        File(manifestOut, "AndroidManifest.xml"),
    )
}
