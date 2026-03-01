// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    configurations.classpath {
        resolutionStrategy.force(
            "com.squareup:javapoet:1.13.0",
            "org.jetbrains.kotlin:kotlin-metadata-jvm:2.3.10",
        )
    }
    dependencies {
        classpath("com.squareup:javapoet:1.13.0")
        classpath("org.jetbrains.kotlin:kotlin-metadata-jvm:2.3.10")
    }
}

allprojects {
    configurations.configureEach {
        resolutionStrategy.force("org.jetbrains.kotlin:kotlin-metadata-jvm:2.3.10")
    }
}

plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.compose) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
