plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

import java.io.File
import java.util.Properties

android {
    namespace = "com.sohrab.baghbakri"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.sohrab.baghbakri"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val secretPropsFile = rootProject.file("signing-secret/keystore.properties")
            val rootPropsFile = rootProject.file("keystore.properties")
            val keystorePropsFile = when {
                secretPropsFile.exists() -> secretPropsFile
                rootPropsFile.exists() -> rootPropsFile
                else -> null
            }
            val envStoreFile = System.getenv("SIGNING_STORE_FILE")
            when {
                keystorePropsFile != null -> {
                    val keystoreProps = Properties().apply {
                        keystorePropsFile.inputStream().use { load(it) }
                    }
                    val storePath = keystoreProps["storeFile"] as String
                    storeFile = if (File(storePath).isAbsolute) {
                        file(storePath)
                    } else {
                        // Relative paths are relative to the properties file's folder
                        keystorePropsFile.parentFile.resolve(storePath)
                    }
                    storePassword = keystoreProps["storePassword"] as String
                    keyAlias = keystoreProps["keyAlias"] as String
                    keyPassword = keystoreProps["keyPassword"] as String
                }
                !envStoreFile.isNullOrBlank() -> {
                    storeFile = file(envStoreFile)
                    storePassword = System.getenv("SIGNING_STORE_PASSWORD")
                    keyAlias = System.getenv("SIGNING_KEY_ALIAS")
                    keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
                }
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            val releaseSigning = signingConfigs.getByName("release")
            if (releaseSigning.storeFile != null) {
                signingConfig = releaseSigning
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
