// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    // 在Kotlin DSL中正确定义扩展属性的方式
    extra.apply {
        set("agpVersion", "8.8.2")
        set("kotlinVersion", "1.9.24")
        set("coreKtxVersion", "1.15.0")
        set("junitVersion", "4.13.2")
        set("junitExtVersion", "1.2.1") 
        set("espressoCoreVersion", "3.6.1")
        set("appcompatVersion", "1.7.0")
        set("materialVersion", "1.12.0")
        set("activityVersion", "1.10.1")
        set("constraintlayoutVersion", "2.2.1")
        set("navigationVersion", "2.7.7")
        set("lifecycleVersion", "2.7.0")
        set("recyclerviewVersion", "1.3.2")
        set("cardviewVersion", "1.0.0")
        set("viewpager2Version", "1.0.0")
        set("glideVersion", "4.16.0")
    }
    
    repositories {
        google()
        mavenCentral()
    }
    
    dependencies {
        classpath("com.android.tools.build:gradle:${project.extra["agpVersion"]}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${project.extra["kotlinVersion"]}")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:${project.extra["navigationVersion"]}")
    }
}

// 设置所有子项目的公共配置
allprojects {
    repositories {
        google()
        mavenCentral()
    }
}