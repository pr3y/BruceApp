import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

repositories {
    google()
    mavenCentral()
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation("androidx.compose.material:material-icons-extended:1.5.4")
            implementation("androidx.compose.material3:material3:1.1.2")
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(projects.shared)
            implementation("com.fazecast:jSerialComm:2.9.2")
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation("com.fazecast:jSerialComm:2.9.2")
        }
    }
}

android {
    namespace = "bruce.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "bruce.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 3
        versionName = "1.3"
    }

    signingConfigs {
        create("release") {
            storeFile = file("bruceapp.keystore")
            storePassword = "bruceapp"
            keyAlias = "bruceapp"
            keyPassword = "bruceapp"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
        getByName("debug") {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    dependencies {
        debugImplementation(compose.uiTooling)
    }
}
dependencies {
    implementation(libs.androidx.navigation.compose)
}

compose.desktop {
    application {
        mainClass = "bruce.app.MainKt"

        nativeDistributions {
            // Explicitly request AppImage (Linux), Exe (Windows), and Dmg (macOS)
            targetFormats(
                TargetFormat.AppImage,  // Linux portable executable
                TargetFormat.Exe,      // Windows standalone .exe
                TargetFormat.Dmg       // macOS .app bundle inside .dmg
            )

            packageName = "bruce.app"
            packageVersion = "1.1.0"

            // Linux-specific settings (AppImage)
            linux {
                appCategory = "Utility"
                menuGroup = "bruce.app"
            }

            // Windows-specific settings (.exe)
            windows {
                menuGroup = "bruce.app"
                upgradeUuid = "your-random-uuid" // Generate via `uuidgen`
            }

            // macOS-specific settings (.dmg)
            macOS {
                bundleID = "com.bruce.app"
                dockName = "Bruce App"
            }
        }
    }
}
