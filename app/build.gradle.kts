plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.shechaniahinformationsystem"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.shechaniahinformationsystem"
        minSdk = 24
        targetSdk = 34
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

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation (libs.volley)
    implementation (libs.okhttp3.okhttp)
    implementation (libs.logging.interceptor)
    implementation(libs.jbcrypt)
    implementation (libs.gson)
    implementation (libs.androidx.recyclerview.v121)

    implementation (libs.androidx.swiperefreshlayout)
    implementation (libs.glide)
    annotationProcessor (libs.compiler)
    implementation(libs.androidx.recyclerview)

}