plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.example.booktracker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.booktracker"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.google.firebase.messaging)
    implementation(libs.viewpump)
    implementation(libs.calligraphy3)
    implementation(libs.transformationlayout)
    implementation(libs.library)
    implementation(libs.mpandroidchart)
    implementation(libs.viewpump)
    implementation(libs.sdp.android)
    implementation(libs.ssp.android)
    implementation(libs.material.v190)
    implementation(libs.toasty)
    implementation(libs.epoxy)
    implementation(libs.circleimageview)
    implementation(libs.material.dialogs.core)
    implementation(libs.shimmer)
    implementation(libs.core)
    implementation(libs.google.material.typeface.v4003kotlin)
    implementation(libs.materialdesignlibrary.v13)
    implementation(libs.google.material.v190)
    implementation(libs.lottie)
    implementation(libs.material.v180)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.auth.v2070)
    implementation(libs.volley)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.crashlytics)
    implementation(libs.google.firebase.analytics)
    implementation(libs.play.services.tagmanager)
    implementation(libs.firebase.firestore.v2400)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.glide.v4142)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    annotationProcessor(libs.compiler.v4110)
    implementation(libs.glide)
    implementation(libs.annotations)
    implementation(libs.annotation)
    implementation(libs.recyclerview)
    implementation(libs.books)
    annotationProcessor(libs.compiler)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.gson)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}