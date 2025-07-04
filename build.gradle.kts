// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}
// build.gradle (Project-level)
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {

        val hiltVersion = "2.46.1"
        classpath("com.google.dagger:hilt-android-gradle-plugin:$hiltVersion")
        classpath ("com.android.tools.build:gradle:8.1.0") // ou o mais recente
        classpath ("com.google.gms:google-services:4.4.0")  // se estiver usando Firebase
    }
}
