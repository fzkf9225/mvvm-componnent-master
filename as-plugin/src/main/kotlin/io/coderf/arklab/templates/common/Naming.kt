package io.coderf.arklab.templates.common

import com.android.tools.idea.wizard.template.camelCaseToUnderlines
import java.io.File

object Naming {
    fun featureFromActivity(activityClass: String): String =
        activityClass.removeSuffix("Activity").ifBlank { "Blank" }

    fun featureFromFragment(fragmentClass: String): String =
        fragmentClass.removeSuffix("Fragment").ifBlank { "Blank" }

    fun layoutToBindingClass(layoutName: String): String =
        layoutName
            .split('_')
            .joinToString("") { part -> part.replaceFirstChar { it.titlecaseChar() } } + "Binding"

    fun toSnake(name: String): String = camelCaseToUnderlines(name)

    /**
     * Resolve the directory that corresponds to [basePackage], e.g.
     * `.../java/io/coderf/arklab/demo`.
     *
     * Handles both cases from Android Studio:
     * - srcDir already points at the selected package folder
     * - srcDir is the java/kotlin source root
     */
    fun resolvePackageRoot(srcDir: File, basePackage: String): File {
        val packagePath = basePackage.replace('.', File.separatorChar)
        val normalized = srcDir.absolutePath.replace('/', File.separatorChar)

        if (normalized.endsWith(packagePath)) {
            return File(normalized)
        }

        // Walk up to java/kotlin source root, then append package path.
        var current: File? = srcDir
        while (current != null) {
            if (current.name == "java" || current.name == "kotlin") {
                return File(current, packagePath)
            }
            current = current.parentFile
        }

        // Last resort: treat srcDir as source root.
        return File(srcDir, packagePath)
    }

    fun basePackage(packageName: String): String = when {
        packageName.endsWith(".activity") -> packageName.removeSuffix(".activity")
        packageName.endsWith(".fragment") -> packageName.removeSuffix(".fragment")
        packageName.endsWith(".viewmodel") -> packageName.removeSuffix(".viewmodel")
        packageName.endsWith(".adapter") -> packageName.removeSuffix(".adapter")
        packageName.endsWith(".repository") -> packageName.removeSuffix(".repository")
        packageName.endsWith(".bean") -> packageName.removeSuffix(".bean")
        else -> packageName
    }

    fun appPackage(
        namespace: String,
        applicationPackage: String?,
        basePackage: String,
    ): String = namespace.ifBlank { applicationPackage.orEmpty() }.ifBlank { basePackage }
}
