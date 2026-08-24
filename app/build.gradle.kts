plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.aihearingassist"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.aihearingassist"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    flavorDimensions += "speechModel"

    productFlavors {
        create("englishIn") {
            dimension = "speechModel"
            buildConfigField("String", "BUNDLED_SPEECH_MODELS", "\"model-en-in\"")
            versionNameSuffix = "-en-in"
        }
        create("englishUs") {
            dimension = "speechModel"
            buildConfigField("String", "BUNDLED_SPEECH_MODELS", "\"model-en-us\"")
            versionNameSuffix = "-en-us"
        }
        create("hindi") {
            dimension = "speechModel"
            buildConfigField("String", "BUNDLED_SPEECH_MODELS", "\"model-hi\"")
            versionNameSuffix = "-hi"
        }
        create("full") {
            dimension = "speechModel"
            buildConfigField("String", "BUNDLED_SPEECH_MODELS", "\"model-en-us,model-en-in,model-hi\"")
            versionNameSuffix = "-full"
        }
    }

    sourceSets {
        getByName("englishIn") {
            assets.srcDir("src/englishIn/assets")
        }
        getByName("englishUs") {
            assets.srcDir("src/englishUs/assets")
        }
        getByName("hindi") {
            assets.srcDir("src/hindi/assets")
        }
        getByName("full") {
            assets.srcDirs("src/englishIn/assets", "src/englishUs/assets", "src/hindi/assets")
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = false
        }
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

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("net.java.dev.jna:jna:5.18.1@aar")
    implementation("com.alphacephei:vosk-android:0.3.75@aar")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.24")
}
