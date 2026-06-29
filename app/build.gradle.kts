import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

// local.properties에서 API 키 읽기
val localProperties = Properties().apply {
    val localPropsFile = rootProject.file("local.properties")
    if (localPropsFile.exists()) load(localPropsFile.inputStream())
}

android {
    namespace = "com.example.termproject"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.termproject"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            "\"${localProperties["GEMINI_API_KEY"] ?: ""}\""
        )
        buildConfigField(
            "String",
            "GEMINI_API_KEY_2",
            "\"${localProperties["GEMINI_API_KEY_2"] ?: ""}\""
        )
        buildConfigField(
            "String",
            "GEMINI_API_KEY_3",
            "\"${localProperties["GEMINI_API_KEY_3"] ?: ""}\""
        )
        manifestPlaceholders["MAPS_API_KEY"] = localProperties["MAPS_API_KEY"]?.toString() ?: ""
    }

    buildTypes {
        debug {
            buildConfigField("String", "GEMINI_API_KEY", "\"${localProperties["GEMINI_API_KEY"] ?: ""}\"")
            buildConfigField("String", "GEMINI_API_KEY_2", "\"${localProperties["GEMINI_API_KEY_2"] ?: ""}\"")
            buildConfigField("String", "GEMINI_API_KEY_3", "\"${localProperties["GEMINI_API_KEY_3"] ?: ""}\"")
        }
        release {
            // Make sure to define the key for release as well
            buildConfigField("String", "GEMINI_API_KEY", "\"${localProperties["GEMINI_API_KEY"] ?: ""}\"")
            buildConfigField("String", "GEMINI_API_KEY_2", "\"${localProperties["GEMINI_API_KEY_2"] ?: ""}\"")
            buildConfigField("String", "GEMINI_API_KEY_3", "\"${localProperties["GEMINI_API_KEY_3"] ?: ""}\"")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    // TFLite 모델을 압축 없이 포함 (내부 저장소 파일은 이 설정 불필요하지만 혹시 남은 파일 대비)
    androidResources {
        noCompress += listOf("tflite")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11

    }
    kotlinOptions {
        jvmTarget = "11"
    }


    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        // 사용 중인 Kotlin 버전에 맞는 Compose Compiler 버전을 입력해야 합니다.
        // 예: Kotlin 1.9.0을 사용 중이라면 "1.5.1"
        kotlinCompilerExtensionVersion = "1.5.14"
    }

}

dependencies {

    implementation(platform("com.google.firebase:firebase-bom:32.2.0"))

    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")   // ML 모델 다운로드용
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3") // Task.await()


    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("androidx.compose.ui:ui:1.7.8")
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("androidx.compose.animation:animation:1.7.8")
    implementation("androidx.activity:activity-compose:1.9.3")
    // 다양한 아이콘 지원을 위해 포함 (선택사항)
    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    // MVVM: ViewModel + StateFlow Compose 연동
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("org.tensorflow:tensorflow-lite:2.16.1")
    implementation("org.tensorflow:tensorflow-lite-select-tf-ops:2.16.1")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    // JSON 직렬화 (JournalRepository 영속화 + Gemini 응답 파싱)
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(platform("androidx.compose:compose-bom:2025.01.00"))
    // Gemini AI SDK
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    // Google Maps & Compose Maps & GPS Play Services Location
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.maps.android:maps-compose:6.1.2")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.credentials:credentials:1.3.0-rc01")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0-rc01")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
}
apply(plugin = "com.google.gms.google-services")