plugins {
    id("com.android.application")
    id("com.chaquo.python")

}

android {
    namespace = "com.example.fitness"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.fitness"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
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
        debug {
            // Можно добавить настройки для debug, если нужно
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
    }
}

chaquopy {
    // pip { install("requests") }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("androidx.navigation:navigation-fragment:2.7.5")
    implementation("androidx.navigation:navigation-ui:2.7.5")
}

// --- Задача для переименования APK после сборки ---
tasks.register<Copy>("renameApk") {
    val appName = "FITNESS"
    val versionName = android.defaultConfig.versionName ?: "1.0"

    val apkDir = layout.buildDirectory.dir("outputs/apk").get().asFile
    val renamedDir = layout.buildDirectory.dir("outputs/renamedApk").get().asFile

    from(apkDir)
    include("**/*.apk") // копируем все apk из outputs/apk (и release, debug и др.)

    into(renamedDir)

    rename { fileName ->
        // Пример переименования: FITNESS-release-v1.0.apk
        val buildType = when {
            fileName.contains("release") -> "release"
            fileName.contains("debug") -> "debug"
            else -> "unknown"
        }
        "$appName-$buildType-v$versionName.apk"
    }
}

// Запускаем задачу переименования после сборки assemble (включая release и debug)
tasks.named("assemble") {
    finalizedBy("renameApk")
}
