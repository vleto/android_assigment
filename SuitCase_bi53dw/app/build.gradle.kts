plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id ("org.jetbrains.kotlin.android")

}

android {
    namespace = "com.shlin.vlet"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.bi52ex.nathan"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {


    implementation(fileTree("libs") { include("*.jar") })
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation ("com.intuit.sdp:sdp-android:1.1.0")
    implementation ("com.intuit.ssp:ssp-android:1.1.0")

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    // Add the dependency for the Firebase SDK for Google Analytics
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-database")
    implementation ("com.google.firebase:firebase-storage")
    implementation ("com.google.firebase:firebase-auth")

    implementation ("com.google.firebase:firebase-firestore")
    implementation ("com.google.firebase:firebase-functions")


    implementation ("com.github.bumptech.glide:glide:4.11.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.11.0")
    implementation ("de.hdodenhof:circleimageview:3.1.0")

    implementation("com.jakewharton:butterknife:10.2.3")

    implementation ("com.yanzhenjie:album:2.1.3")

    implementation("com.nhaarman.listviewanimations:lib-core:3.1.0")
    implementation("com.nhaarman.listviewanimations:lib-manipulation:3.1.0")

    implementation("com.airbnb.android:lottie:6.3.0")

}