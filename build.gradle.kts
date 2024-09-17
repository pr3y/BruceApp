plugins {
    kotlin("multiplatform") version "1.8.0"
}

kotlin {
    jvm()
    linuxX64("linux")
    mingwX64("windows")
    androidArm64("android")

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }
        val linuxMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
            }
        }
        val windowsMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
            }
        }
    }
}

