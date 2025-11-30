plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.pelagiahotelapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.pelagiahotelapp"
        minSdk = 24
        targetSdk = 36
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
}

dependencies {
    implementation("com.cloudinary:cloudinary-android:2.3.1")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))
    implementation("com.github.bumptech.glide:glide:4.16.0")
// If you need OkHttp integration (recommended for better performance)
    implementation("com.github.bumptech.glide:okhttp3-integration:4.16.0")
    //maps
    implementation("com.google.android.gms:play-services-location:21.3.0")
// For: org.osmdroid.*
// (MapView, Marker, GeoPoint, Configuration, etc.)
    implementation("org.osmdroid:osmdroid-android:6.1.20")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}