plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
    namespace = "com.reeltracker"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.reeltracker"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            // Signed with the debug key so the release APK is directly installable (sideload).
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
