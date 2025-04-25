plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.strokefree"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.strokefree"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.0")

    implementation( "com.microsoft.onnxruntime:onnxruntime-android:latest.release")

    // Jetpack Compose ViewModel Integration
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth-ktx")

    //google sign in
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation("androidx.credentials:credentials:1.5.0-rc01")
    implementation ("com.google.android.libraries.identity.googleid:googleid:1.0.0")
    //icons
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    //datepicker
    implementation("com.google.android.material:material:1.12.0")

    // animation
    implementation("com.google.accompanist:accompanist-navigation-animation:0.36.0")
    implementation("androidx.navigation:navigation-compose:2.8.7")
    implementation("androidx.compose.animation:animation:1.7.8")

    //scrollbar
//    implementation("com.google.accompanist:accompanist-scrollbar:0.37.2")

    //async image to use image URL
    implementation("io.coil-kt.coil3:coil-compose:3.1.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.1.0")

    //webview
    implementation("com.google.accompanist:accompanist-webview:0.31.1-alpha")

}
apply(plugin = "com.google.gms.google-services")
