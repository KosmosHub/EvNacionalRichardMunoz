plugins {
    alias(libs.plugins.android.application)
    // 1. ACTIVAMOS EL PLUGIN DE GOOGLE SERVICES AQUÍ
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.conectamobile"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.conectamobile"
        minSdk = 26
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
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // --- AGREGADO PARA FIREBASE ---
    // Importamos la plataforma (BOM) para que controle las versiones
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))

    // Librerías específicas (sin poner versión, el BOM se encarga)
    implementation("com.google.firebase:firebase-auth")        // Autenticación
    implementation("com.google.firebase:firebase-database")    // Base de datos Realtime

    //autenticacion de google
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    //glide
    implementation("com.github.bumptech.glide:glide:4.16.0")

    implementation("com.github.hannesa2:paho.mqtt.android:4.3")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")

    // Soporte para servicios legacy (ayuda a que Paho no falle en Android nuevos)
    implementation("androidx.legacy:legacy-support-v4:1.0.0")

}