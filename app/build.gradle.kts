import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.sleepsounds.storytelling"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.sleepsounds.storytelling"
        minSdk = 26
        targetSdk = 37
        versionCode = 60
        versionName = "3.1.1"
        ndk {
            debugSymbolLevel = "FULL"
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            if (keystorePropertiesFile.exists()) {
                val keystoreProperties = Properties()
                keystoreProperties.load(FileInputStream(keystorePropertiesFile))
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            // Google Official Test AdMob IDs for Debug builds
            manifestPlaceholders["admob_app_id"] = "ca-app-pub-3940256099942544~3347511713"
            buildConfigField("String", "BANNER_ID", "\"ca-app-pub-3940256099942544/9214589741\"")
            buildConfigField("String", "BANNER_ID_2", "\"ca-app-pub-3940256099942544/9214589741\"")
            buildConfigField("String", "BANNER_ID_3", "\"ca-app-pub-3940256099942544/9214589741\"")
            buildConfigField("String", "BANNER_ID_4", "\"ca-app-pub-3940256099942544/9214589741\"")
            buildConfigField("String", "BANNER_ID_5", "\"ca-app-pub-3940256099942544/9214589741\"")
            buildConfigField("String", "INTERSTITIAL_ID", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "APP_OPEN_ID", "\"ca-app-pub-3940256099942544/9257395921\"")
            buildConfigField("String", "REWARDED_ID", "\"ca-app-pub-3940256099942544/5224354917\"")
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            // Production AdMob IDs for Play Store release
            manifestPlaceholders["admob_app_id"] = "ca-app-pub-7072665289567101~2710213889"
            buildConfigField("String", "BANNER_ID", "\"ca-app-pub-7072665289567101/1577095611\"")
            buildConfigField("String", "BANNER_ID_2", "\"ca-app-pub-7072665289567101/1657410522\"")
            buildConfigField("String", "BANNER_ID_3", "\"ca-app-pub-7072665289567101/2671109294\"")
            buildConfigField("String", "BANNER_ID_4", "\"ca-app-pub-7072665289567101/6718165510\"")
            buildConfigField("String", "BANNER_ID_5", "\"ca-app-pub-7072665289567101/3373157732\"")
            buildConfigField("String", "INTERSTITIAL_ID", "\"ca-app-pub-7072665289567101/5298404535\"")
            buildConfigField("String", "APP_OPEN_ID", "\"ca-app-pub-7072665289567101/6018343853\"")
            buildConfigField("String", "REWARDED_ID", "\"ca-app-pub-7072665289567101/3985322861\"")
            ndk {
                debugSymbolLevel = "FULL"
            }
            optimization {
                enable = true
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    bundle {
        language {
            enableSplit = false
        }
    }

    packaging {
        dex {
            useLegacyPackaging = false
        }
        jniLibs {
            useLegacyPackaging = false
            excludes += "**/libandroidx.graphics.path.so"
        }
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/*.version"
            excludes += "META-INF/*.kotlin_module"
            excludes += "META-INF/kotlin-project-structure-metadata.json"
            excludes += "kotlin-tooling-metadata.json"
            excludes += "DebugProbesKt.bin"
        }
    }
}

configurations.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group == "androidx.fragment" && requested.name.startsWith("fragment")) {
            useVersion("1.9.1")
            because("Ensure 16 KB page size compliance for transitive androidx.fragment")
        }
    }
}

val appBuildDirProvider = layout.buildDirectory
tasks.matching {
    it.name == "extractReleaseNativeDebugMetadata" ||
        it.name == "extractReleaseNativeSymbolTables" ||
        it.name == "mergeReleaseNativeDebugMetadata" ||
        it.name == "mergeReleaseNativeSymbolTables"
}.configureEach {
    val isFullDebug = name.contains("DebugMetadata")
    val symbolExt = if (isFullDebug) ".dbg" else ".sym"
    val buildDirProp = appBuildDirProvider
    doLast {
        val buildDir = buildDirProp.get().asFile
        val mergedRoot = File(buildDir, "intermediates/merged_native_libs/release")
        if (!mergedRoot.exists()) return@doLast

        val outDirs = outputs.files.files.filter { it.isDirectory || !it.name.contains('.') }
        val abiDirs = mergedRoot.walkTopDown()
            .filter { it.isDirectory && it.parentFile?.name == "lib" }
            .toList()

        for (outDir in outDirs) {
            for (abiDir in abiDirs) {
                val abiName = abiDir.name
                val targetAbiDir = File(outDir, abiName).apply { mkdirs() }
                abiDir.listFiles { f -> f.isFile && f.name.endsWith(".so") }?.forEach { soFile ->
                    val symFile = File(targetAbiDir, "${soFile.name}$symbolExt")
                    if (!symFile.exists() || symFile.length() == 0L) {
                        soFile.copyTo(symFile, overwrite = true)
                    }
                }
            }
        }

        val primaryOut = outDirs.firstOrNull() ?: return@doLast
        val zipOutDir = File(buildDir, "outputs/native-debug-symbols/release").apply { mkdirs() }
        val zipFile = File(zipOutDir, "native-debug-symbols.zip")
        val symFiles = primaryOut.walkTopDown().filter { it.isFile }.toList()
        if (symFiles.isNotEmpty()) {
            ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                for (file in symFiles) {
                    val relPath = file.relativeTo(primaryOut).invariantSeparatorsPath
                    zos.putNextEntry(ZipEntry(relPath))
                    file.inputStream().use { it.copyTo(zos) }
                    zos.closeEntry()
                }
            }
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(platform(libs.firebase.bom))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    ksp(libs.kotlin.metadata.jvm)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.lifecycle.viewmodel.compose)

    implementation(libs.firebase.config)
    implementation(libs.firebase.database)
    implementation(libs.firebase.analytics)
    implementation(libs.billing)
    implementation(libs.play.services.ads)

    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.android.youtube.player)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
