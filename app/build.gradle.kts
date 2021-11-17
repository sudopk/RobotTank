plugins {
  id("com.android.application")
  id("kotlin-android")
}

android {
  compileSdk = 31

  defaultConfig {
    applicationId = "com.sudopk.robottank"
    minSdk = 21
    targetSdk = compileSdk
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildFeatures {
    // Enables Jetpack Compose for this module
    compose = true
  }

  composeOptions {
    kotlinCompilerExtensionVersion = "1.0.5"
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
  implementation("androidx.core:core-ktx:1.7.0")
  implementation("androidx.appcompat:appcompat:1.3.1")
  implementation("com.google.android.material:material:1.4.0")

  // Integration with activities
  implementation("androidx.activity:activity-compose:1.4.0")
  // Compose Material Design
  implementation("androidx.compose.material:material:1.0.5")
  // Animations
  implementation("androidx.compose.animation:animation:1.0.5")
  // Tooling support (Previews, etc.)
  implementation("androidx.compose.ui:ui-tooling:1.0.5")
  // Integration with ViewModels
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.4.0")
  // Integration with observables
  implementation("androidx.compose.runtime:runtime-livedata:1.0.5")


  androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.0.5")
  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.1.3")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}