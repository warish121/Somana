plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)




}

android {
    namespace = "com.example.anew"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.anew"
        minSdk = 30
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
        viewBinding = true
    }
    buildFeatures {
        dataBinding = true

    }
    



}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Cloudflare R2 (S3-compatible)
    implementation("aws.sdk.kotlin:s3:1.0.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.16.0"))




    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.cloudinary:cloudinary-android:2.3.1")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation ("com.github.Drjacky:ImagePicker:2.3.22")

    implementation ("com.backendless:backendless:6.3.1")

    // Optional for image loading
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")
















}