plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("androidx.navigation.safeargs.kotlin") // Added Safe Args plugin
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
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
}

dependencies {
    // 正确引用rootProject的扩展属性
    val coreKtxVersion = rootProject.extra["coreKtxVersion"] as String
    val appcompatVersion = rootProject.extra["appcompatVersion"] as String
    val materialVersion = rootProject.extra["materialVersion"] as String
    val activityVersion = rootProject.extra["activityVersion"] as String
    val constraintlayoutVersion = rootProject.extra["constraintlayoutVersion"] as String
    val navigationVersion = rootProject.extra["navigationVersion"] as String
    val lifecycleVersion = rootProject.extra["lifecycleVersion"] as String
    val recyclerviewVersion = rootProject.extra["recyclerviewVersion"] as String
    val cardviewVersion = rootProject.extra["cardviewVersion"] as String
    val viewpager2Version = rootProject.extra["viewpager2Version"] as String
    val glideVersion = rootProject.extra["glideVersion"] as String
    val junitVersion = rootProject.extra["junitVersion"] as String
    val junitExtVersion = rootProject.extra["junitExtVersion"] as String
    val espressoCoreVersion = rootProject.extra["espressoCoreVersion"] as String

    implementation("androidx.core:core-ktx:$coreKtxVersion")
    implementation("androidx.appcompat:appcompat:$appcompatVersion")
    implementation("com.google.android.material:material:$materialVersion")
    implementation("androidx.activity:activity:$activityVersion")
    implementation("androidx.constraintlayout:constraintlayout:$constraintlayoutVersion")
    
    // Navigation Components
    implementation("androidx.navigation:navigation-fragment-ktx:$navigationVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navigationVersion")
    
    // Lifecycle components
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    
    // UI Components
    implementation("androidx.recyclerview:recyclerview:$recyclerviewVersion")
    implementation("androidx.cardview:cardview:$cardviewVersion")
    implementation("androidx.viewpager2:viewpager2:$viewpager2Version")
    
    // Image loading
    implementation("com.github.bumptech.glide:glide:$glideVersion")
    
    testImplementation("junit:junit:$junitVersion")
    androidTestImplementation("androidx.test.ext:junit:$junitExtVersion")
    androidTestImplementation("androidx.test.espresso:espresso-core:$espressoCoreVersion")
}