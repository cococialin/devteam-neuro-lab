plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "ro.devteam.cnntest"
    compileSdk = 34

    defaultConfig {
        applicationId = "ro.devteam.cnntest"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    implementation(libs.fastcsv)
    implementation(libs.litert.gpu.api)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(libs.okhttp.v4120)

    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")

    implementation("com.jakewharton:butterknife:10.2.3")
    annotationProcessor("com.jakewharton:butterknife-compiler:10.2.3")
    implementation("com.nex3z:finger-paint-view:0.3.1")
    implementation("de.siegmar:fastcsv:3.3.1")

    // Build off of nightly TensorFlow Lite
    implementation("org.tensorflow:tensorflow-lite:2.16.1")
    implementation("org.tensorflow:tensorflow-lite-api:2.16.1")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.16.1")
    implementation("org.tensorflow:tensorflow-lite-gpu-delegate-plugin:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")

    implementation("androidx.multidex:multidex:2.0.1")
}