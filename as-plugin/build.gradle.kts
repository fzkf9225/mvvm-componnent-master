plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.3.0"
    id("org.jetbrains.intellij.platform") version "2.5.0"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

kotlin {
    jvmToolchain(21)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        local(providers.gradleProperty("androidStudioPath"))
        bundledPlugin("org.jetbrains.android")
        bundledPlugin("com.intellij.java")
    }
}

intellijPlatform {
    pluginConfiguration {
        id = "io.coderf.arklab.templates.basemvvm"
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("pluginVersion")

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            untilBuild = providers.gradleProperty("pluginUntilBuild")
        }

        description = """
            Adds <b>BaseActivity 空白页</b> to Android Studio native
            <code>New → Activity</code> gallery.<br/>
            Generates blank BaseActivity + Hilt ViewModel + DataBinding layout.
        """.trimIndent()
    }

    buildSearchableOptions = false
}

tasks {
    wrapper {
        gradleVersion = "8.13"
    }
}
