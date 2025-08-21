plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.google.dagger.hilt.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.equationl.wxsteplog"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.equationl.wxsteplog"
        minSdk = 24
        targetSdk = 35
        versionCode = 8
        versionName = "1.0.8"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        dataBinding = true
    }
    viewBinding {
        enable = true
    }

    lint {
        warning.add("NotificationPermission")
    }

}

dependencies {
    implementation(project(":aiapi"))
    implementation(project(":common"))

    val includePaidModule = rootProject.providers.gradleProperty("include.paid.module").orNull?.toBoolean() ?: false
    if (includePaidModule) {
        implementation(project(":aipro"))
    }

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.constraintlayout)

    // FIXME 这个版本有 repeat BUG，已经提了 PR，但是还没合并，所以暂时用我自己 fork 的
    implementation(libs.assists.base)
    implementation(libs.gson)
    implementation(libs.hilt.android)
    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m3)
    implementation(libs.dialog.core)
    implementation(libs.baserecyclerviewadapterhelper)
    implementation (libs.markwon.core)
    implementation (libs.html)

    ksp(libs.androidx.room.compiler)
    ksp(libs.dagger.compiler)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}