// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {

    extra["kotlin_version"] = "1.1.1"
    extra["supportLibVersion"] = "25.3.0"

    repositories {
        google()  // Google's Maven repository
        mavenCentral()  // Maven Central repository
    }

    dependencies {
        classpath ("com.android.tools.build:gradle:8.2.1")
        classpath ("com.google.gms:google-services:4.4.0")
        classpath ("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.10")
        classpath ("com.android.tools.build:gradle:8.2.1")
    }
}

plugins {
    id("com.android.application") version "8.2.1" apply false
    id ("com.android.library") version "7.3.1" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false

}
