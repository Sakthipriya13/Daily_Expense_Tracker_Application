plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id ("kotlin-kapt")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.expensetrackerapplication"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.expensetrackerapplication"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        multiDexEnabled = true

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
//    kotlinOptions {
//        jvmTarget = "17"
//    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures{
        dataBinding = true
    }

    kapt {
        arguments {
            // This tells Room where to export the database schema
            arg("room.schemaLocation", "$projectDir/schemas")
            arg("room.incremental", "true")
            arg("room.expandProjection", "true")
        }
    }

}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.litert)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.protolite.well.known.types)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.gridlayout)
    implementation(libs.androidx.ui.text)
    implementation(libs.androidx.glance)
    implementation(libs.androidx.core.i18n)
    implementation(libs.places)
//    implementation(libs.firebase.auth.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


    // Room runtime
    implementation (libs.androidx.room.runtime)

    // Room compiler (for annotation processing)
    kapt (libs.androidx.room.compiler.v261)   // for Kotlin
    // or
    annotationProcessor (libs.room.compiler)  // for Java

    // Room Kotlin Extensions and Coroutines support
    implementation (libs.androidx.room.ktx)

    // Optional: Testing Room database
    testImplementation (libs.androidx.room.testing)

    // Optional: Paging support with Room
    implementation (libs.androidx.room.paging)

    implementation(libs.androidx.sqlite.bundled)

    implementation(libs.poi)
    implementation(libs.poi.ooxml)

    implementation(libs.glide)

    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.multidex)

    implementation(libs.anychart.android)

    implementation(libs.mpandroidchart)

    implementation(libs.williamchart)

    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))

    // Firestore
    implementation("com.google.firebase:firebase-firestore-ktx")

    // Analytics (optional)
    implementation("com.google.firebase:firebase-analytics-ktx")

    implementation("com.google.firebase:firebase-auth-ktx")

    implementation("androidx.work:work-runtime-ktx:2.9.0")
}
