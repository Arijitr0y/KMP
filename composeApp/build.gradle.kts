import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

// ---------- Cache-safe codegen task ----------
@CacheableTask
abstract class GenerateEnv : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val localPropsFile: RegularFileProperty

    @get:OutputFile
    abstract val outFile: RegularFileProperty

    @TaskAction
    fun run() {
        val props = Properties().apply {
            val f = localPropsFile.get().asFile
            if (f.exists()) f.inputStream().use { load(it) }
        }
        val url = props.getProperty("SUPABASE_URL") ?: ""
        val key = props.getProperty("SUPABASE_ANON_KEY") ?: ""

        val pkg = "org.assidious.superlocal.config" // <- keep in sync with your code
        val content = """
            package $pkg

            // Auto-generated from local.properties during build (do not commit secrets)
            object Env {
                const val SUPABASE_URL = "$url"
                const val SUPABASE_ANON_KEY = "$key"
                val isConfigured: Boolean = SUPABASE_URL.isNotBlank() && SUPABASE_ANON_KEY.isNotBlank()
            }
        """.trimIndent()

        val out = outFile.get().asFile
        out.parentFile.mkdirs()
        out.writeText(content)
    }
}

// ---------- Register task & wire as a source of commonMain ----------
val envGenDir = layout.buildDirectory.dir("generated/env/commonMain/kotlin")
val envOutFile = envGenDir.map { it.file("org/assidious/superlocal/config/Env.kt") }

val generateEnv = tasks.register("generateEnv", GenerateEnv::class.java) {
    localPropsFile.set(layout.projectDirectory.file("../local.properties")) // project root
    outFile.set(envOutFile)
}

kotlin.sourceSets.named("commonMain") {
    kotlin.srcDir(envGenDir)
}

// Ensure codegen runs before all Kotlin compilations in this module
tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).configureEach {
    dependsOn(generateEnv)
}



plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    }
//kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
// ---- Versions (single source of truth) ----
val ktor = "3.3.1"
val coroutines = "1.9.0"
val serializationJson = "1.7.3"
val supabase = "3.2.6"

kotlin {
    // ✅ Register Android target (removes your warning)
    androidTarget {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
    }

    // iOS targets
    iosArm64()
    iosSimulatorArm64()
       sourceSets {
        // ---------- Common ----------
        val commonMain by getting {
            dependencies {
                // Compose (yours)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.lifecycle.runtimeCompose)
                implementation(compose.runtime)

                 // Supabase core + Auth (multiplatform) — use explicit version
                implementation("io.github.jan-tennert.supabase:supabase-kt:3.2.6")
                implementation("io.github.jan-tennert.supabase:auth-kt:3.2.6")

                // Session persistence (Multiplatform Settings)
                implementation("com.russhwolf:multiplatform-settings")
                implementation("com.russhwolf:multiplatform-settings-no-arg")

                // Kotlinx Serialization JSON (supabase-kt uses it by default)
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")



            }
        }

        // ---------- Android ----------
        val androidMain by getting {
            dependencies {
                implementation(compose.preview)
                implementation(libs.androidx.activity.compose)
                implementation(compose.uiTooling)

                // ✅ Use ONE Ktor version; OkHttp is the typical Android engine
                implementation("io.ktor:ktor-client-okhttp:$ktor")
            }
        }

        // ---------- iOS ----------
        val iosArm64Main by getting {
            dependencies {
                implementation("io.ktor:ktor-client-darwin:$ktor")
            }
        }
        val iosSimulatorArm64Main by getting {
            dependencies {
                implementation("io.ktor:ktor-client-darwin:$ktor")
            }
        }

        // ---------- Tests ----------
        val commonTest by getting {
            dependencies { implementation(libs.kotlin.test) }
        }
    }
}

android {
    namespace = "org.example.project.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.example.project.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        // Optional: read Supabase keys from local.properties


        val props = Properties().apply {
            val lp = rootProject.file("local.properties")
            if (lp.exists()) lp.inputStream().use { load(it) }

        }
        val supaUrl = (props.getProperty("SUPABASE_URL") ?: "").trim().removeSuffix("/")
        val supaKey = ((props.getProperty("SUPABASE_ANON_KEY")
            ?: props.getProperty("SUPABASE_KEY")) ?: "").trim()

        require(supaUrl.startsWith("https://")) {
            "SUPABASE_URL must start with https:// (no trailing slash). Current: '$supaUrl'"
        }
        require(supaKey.isNotBlank()) {
            "Missing Supabase key. Add SUPABASE_ANON_KEY=... in local.properties " +
                    "(or SUPABASE_KEY as fallback)."
        }

        buildConfigField("String", "SUPABASE_URL", "\"$supaUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supaKey\"")
    }


    buildFeatures { buildConfig = true }

    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }

    buildTypes {
        getByName("release") { isMinifyEnabled = false }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}