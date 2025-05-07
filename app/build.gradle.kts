plugins {
    id("com.android.application")
    id("com.chaquo.python")
    // id("kotlin-android") // если используете Kotlin
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

// --- Задача для переименования release APK ---
tasks.register<Copy>("renameReleaseApk") {
    val appName = "FITNESS"
    val versionName = android.defaultConfig.versionName ?: "1.0"
    val buildType = "release"

    val apkDir = layout.buildDirectory.dir("outputs/apk/$buildType").get().asFile
    val renamedDir = layout.buildDirectory.dir("outputs/renamedApk").get().asFile

    from(apkDir)
    include("app-$buildType-unsigned.apk", "app-$buildType.apk") // копируем signed и unsigned, если есть
    into(renamedDir)
    rename { originalName ->
        // Переименовываем в FITNESS-release-v1.0.apk, убираем -unsigned если есть
        val cleanName = originalName.removeSuffix("-unsigned.apk").removeSuffix(".apk")
        "$appName-$buildType-v$versionName.apk"
    }
}

// --- З
