package org.example.project.auth

import android.content.Context
import android.util.Log
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.SettingsSessionManager
import io.github.jan.supabase.createSupabaseClient
import io.ktor.client.engine.okhttp.OkHttp
import org.example.project.app.BuildConfig

private const val TAG = "SupabaseInit"

private lateinit var appCtx: Context
fun initSupabaseAndroid(context: Context) { appCtx = context.applicationContext }

fun defaultAndroidSettings(): Settings =
    SharedPreferencesSettings(
        appCtx.getSharedPreferences("supabase_prefs", Context.MODE_PRIVATE)
    )

actual object SupabasePlatform {
    actual val supabaseUrl: String get() = BuildConfig.SUPABASE_URL
    actual val supabaseAnonKey: String get() = BuildConfig.SUPABASE_ANON_KEY

    actual fun createClient(settings: Settings): SupabaseClient {
        // quick sanity logs (first 24 chars only)
        Log.d(TAG, "URL=${BuildConfig.SUPABASE_URL}")
        Log.d(TAG, "KEY=${BuildConfig.SUPABASE_ANON_KEY.take(24)}...")

        require(BuildConfig.SUPABASE_URL.startsWith("https://") && !BuildConfig.SUPABASE_URL.endsWith("/")) {
            "Invalid SUPABASE_URL: '${BuildConfig.SUPABASE_URL}'. It must start with https:// and have no trailing slash."
        }
        require(BuildConfig.SUPABASE_ANON_KEY.isNotBlank()) { "Missing SUPABASE_ANON_KEY" }

        return createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseAnonKey
        ) {
            httpEngine = OkHttp.create()
            install(Auth) { sessionManager = SettingsSessionManager(settings) }
        }
    }
}
