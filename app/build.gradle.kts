import java.time.Duration
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
    alias(libs.plugins.paparazzi)
}

val ebbApplicationId = providers.gradleProperty("ebb.applicationId").get()
val ebbNamespace = providers.gradleProperty("ebb.namespace").get()

val ebbVersionPropertiesFile = rootProject.file("version.properties")
val ebbVersionProperties =
    Properties().apply {
        ebbVersionPropertiesFile.inputStream().use { load(it) }
    }
val ebbVersionMajor = ebbVersionProperties.getProperty("VERSION_MAJOR").toInt()
val ebbVersionMinor = ebbVersionProperties.getProperty("VERSION_MINOR").toInt()
val ebbVersionPatch = ebbVersionProperties.getProperty("VERSION_PATCH").toInt()
val ebbVersionName = "$ebbVersionMajor.$ebbVersionMinor.$ebbVersionPatch"
val ebbVersionCode = ebbVersionMajor * 10_000 + ebbVersionMinor * 100 + ebbVersionPatch

fun releaseSigningEnv(name: String) = providers.environmentVariable(name)

android {
    namespace = ebbNamespace
    compileSdk = 35

    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        create("release") {
            val storePath =
                releaseSigningEnv("EBB_KEYSTORE_FILE")
                    .orElse(providers.gradleProperty("ebb.keystore.file"))
            val storePassword = releaseSigningEnv("EBB_KEYSTORE_PASSWORD")
            val keyAlias = releaseSigningEnv("EBB_KEY_ALIAS")
            val keyPassword = releaseSigningEnv("EBB_KEY_PASSWORD")
            if (storePath.isPresent) {
                storeFile = file(storePath.get())
            }
            if (storePassword.isPresent) {
                this.storePassword = storePassword.get()
            }
            if (keyAlias.isPresent) {
                this.keyAlias = keyAlias.get()
            }
            if (keyPassword.isPresent) {
                this.keyPassword = keyPassword.get()
            }
        }
    }

    defaultConfig {
        applicationId = ebbApplicationId
        minSdk = 26
        targetSdk = 35
        versionCode = ebbVersionCode
        versionName = ebbVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            resValue("string", "app_name", "Ebb Debug")
        }
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")
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

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = true
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.google.material)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    kapt(libs.androidx.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    testImplementation(libs.room.testing)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.compose.ui.test.junit4)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

kapt {
    correctErrorTypes = true
}

val onboardingComparisonsDir = layout.buildDirectory.dir("onboarding-comparisons")

tasks.withType<Test>().configureEach {
    timeout.set(Duration.ofMinutes(10))
    maxParallelForks =
        (Runtime.getRuntime().availableProcessors() / 2).coerceIn(1, 4)
}

tasks.register<Copy>("collectOnboardingComparisons") {
    group = "verification"
    description = "Collect Paparazzi deltas/failures for CI onboarding-comparisons artifact"
    dependsOn("verifyPaparazziDebug")
    from(layout.buildDirectory.dir("paparazzi"))
    into(onboardingComparisonsDir)
    include("**/failures/**", "**/delta/**")
}