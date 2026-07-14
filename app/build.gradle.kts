plugins {
    alias(libs.plugins.android.application)

    id("com.google.gms.google-services")

    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.quanlychitieu.doan"

    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.quanlychitieu.doan"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)

    // Firebase BoM quản lý phiên bản tương thích
    implementation(
        platform("com.google.firebase:firebase-bom:34.15.0")
    )

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth")

    // Firebase Crashlytics
    implementation("com.google.firebase:firebase-crashlytics")

    // Firebase Analytics giúp Crashlytics có breadcrumb logs
    implementation("com.google.firebase:firebase-analytics")

    // Đăng nhập Google
    implementation(
        "com.google.android.gms:play-services-auth:21.2.0"
    )

    // Đăng nhập Facebook
    implementation(
        "com.facebook.android:facebook-login:latest.release"
    )

    // Xuất Excel
    implementation("org.apache.poi:poi-ooxml:5.5.1")

    // Material Components
    implementation(
        "com.google.android.material:material:1.12.0"
    )

    // CardView
    implementation(
        "androidx.cardview:cardview:1.0.0"
    )

    // Charts
    implementation(
        "com.github.PhilJay:MPAndroidChart:v3.1.0"
    )

    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}