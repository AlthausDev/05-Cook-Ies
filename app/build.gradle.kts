plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.crashlytics)
    id("kotlin-kapt")
    kotlin("kapt")
    alias(libs.plugins.daggerHiltAndroid)
    id ("kotlin-parcelize")
}

android {
    namespace = "com.althaus.dev.cookIes"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.althaus.dev.cookIes"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true


        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.7"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    // --- Librerías Core de Android ---
    //implementation(libs.support.multidex)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)

    // --- Ciclo de Vida ---
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // --- Compose y UI ---
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)

    // --- Navegación ---
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // --- Firebase ---
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.realtime)
    implementation(libs.firebase.config)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.storage.ktx)

    // --- Firebase App Check ---
    implementation(libs.firebase.appcheck.playintegrity)
    implementation(libs.firebase.appcheck.safetynet)

    // --- Inyección de Dependencias con Hilt ---
    implementation(libs.hilt.android)
    implementation(libs.androidx.ui.test.android)
    kapt(libs.hilt.compiler)

    // --- Servicios de Google ---
    implementation(libs.googlePlayServicesAuth)
    implementation(libs.play.services.location)
    implementation(libs.googleid)

    // --- Utilidades para Compose ---
    implementation(libs.accompanist.flowlayout)

    // --- Manejo de Imágenes ---
    implementation(libs.coil)

    // --- Diseño Material ---
    implementation(libs.material)

    // --- Dependencias para Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
