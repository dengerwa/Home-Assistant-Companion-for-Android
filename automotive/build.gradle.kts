import com.google.gms.googleservices.GoogleServicesPlugin.GoogleServicesPluginConfig

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.google.services)
    alias(libs.plugins.hilt)
}

android {
    namespace = "io.homeassistant.companion.android"

    compileSdk = libs.versions.androidSdk.compile.get().toInt()

    ndkVersion = "21.3.6528147"

    useLibrary("android.car")

    defaultConfig {
        applicationId = "io.homeassistant.companion.android"
        minSdk = libs.versions.androidSdk.automotive.min.get().toInt()
        targetSdk = libs.versions.androidSdk.target.get().toInt()

        versionName = project.version.toString()
        // We add 2 because the app, wear (+1) and automotive versions need to have different version codes.
        versionCode = (System.getenv("VERSION_CODE")?.toIntOrNull() ?: 1) + 3

        manifestPlaceholders["sentryRelease"] = "$applicationId@$versionName"
        manifestPlaceholders["sentryDsn"] = System.getenv("SENTRY_DSN") ?: ""

        bundle {
            language {
                enableSplit = false
            }
        }
    }

    sourceSets {
        getByName("main") {
            java {
                srcDirs("../app/src/main/java")
            }
            assets {
                srcDirs("../app/src/main/assets")
            }
            res {
                srcDirs("../app/src/main/res")
            }
        }
        create("full") {
            java {
                srcDirs("../app/src/full/java")
            }
            res {
                srcDirs("../app/src/full/res")
            }
        }
        create("minimal") {
            java {
                srcDirs("../app/src/minimal/java")
            }
            res {
                srcDirs("../app/src/minimal/res")
            }
        }
        getByName("debug") {
            res {
                srcDirs("../app/src/debug/res")
            }
        }
    }

    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    kotlinOptions {
        jvmTarget = libs.versions.javaVersion.get()
    }

    compileOptions {
        sourceCompatibility(libs.versions.javaVersion.get())
        targetCompatibility(libs.versions.javaVersion.get())
    }

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_PATH") ?: "release_keystore.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
            keyAlias = System.getenv("KEYSTORE_ALIAS") ?: ""
            keyPassword = System.getenv("KEYSTORE_ALIAS_PASSWORD") ?: ""
            enableV1Signing = true
            enableV2Signing = true
        }
    }

    buildTypes {
        named("debug").configure {
            applicationIdSuffix = ".debug"
        }
        named("release").configure {
            isDebuggable = false
            isJniDebuggable = false
            signingConfig = signingConfigs.getByName("release")
        }
    }
    flavorDimensions.add("version")
    productFlavors {
        create("minimal") {
            applicationIdSuffix = ".minimal"
            versionNameSuffix = "-minimal"
        }
        create("full") {
            applicationIdSuffix = ""
            versionNameSuffix = "-full"
        }

        // Generate a list of application ids into BuildConfig
        val values = productFlavors.joinToString {
            "\"${it.applicationId ?: defaultConfig.applicationId}${it.applicationIdSuffix}\""
        }

        defaultConfig.buildConfigField("String[]", "APPLICATION_IDS", "{$values}")
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform {
            includeEngines("spek2")
        }
    }

    lint {
        abortOnError = false
        disable += "MissingTranslation"
    }

    kapt {
        correctErrorTypes = true
    }
}

dependencies {
    implementation(project(":common"))

    implementation(libs.blurView)

    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    "fullImplementation"(libs.kotlinx.coroutines.play.services)

    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    implementation(libs.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.preference.ktx)
    implementation(libs.material)
    implementation(libs.fragment.ktx)

    implementation(libs.jackson.module.kotlin)
    implementation(libs.okhttp)
    implementation(libs.picasso)

    "fullImplementation"(libs.play.services.location)
    "fullImplementation"(libs.play.services.home)
    "fullImplementation"(libs.play.services.threadnetwork)
    "fullImplementation"(platform(libs.firebase.bom))
    "fullImplementation"(libs.firebase.messaging)
    "fullImplementation"(libs.sentry.android)
    "fullImplementation"(libs.play.services.wearable)
    "fullImplementation"(libs.wear.remote.interactions)

    implementation(libs.biometric)
    implementation(libs.webkit)

    implementation(libs.bundles.media3)
    "fullImplementation"(libs.media3.datasource.cronet)
    "minimalImplementation"(libs.media3.datasource.cronet) {
        exclude(group = "com.google.android.gms", module = "play-services-cronet")
    }
    "minimalImplementation"(libs.cronet.embedded)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.animation)
    implementation(libs.compose.compiler)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material)
    implementation(libs.compose.material.icons.core)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.uiTooling)
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.accompanist.themeadapter.material)

    implementation(libs.iconics.core)
    implementation(libs.iconics.compose)
    implementation(libs.community.material.typeface)

    implementation(libs.bundles.paging)

    implementation(libs.reorderable)
    implementation(libs.changeLog)

    implementation(libs.car.core)
    implementation(libs.car.automotive)
}

// Disable to fix memory leak and be compatible with the configuration cache.
configure<GoogleServicesPluginConfig> {
    disableVersionCheck = true
}
