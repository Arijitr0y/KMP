package org.example.project.app

import android.app.Application
import android.util.Log
import org.example.project.auth.SupabaseHolder
import org.example.project.auth.defaultAndroidSettings
import org.example.project.auth.initSupabaseAndroid

class AndroidApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Provide app context for Settings + Ktor engine init
        initSupabaseAndroid(this)

        // Build the Supabase client once for the whole app
        SupabaseHolder.init(defaultAndroidSettings())

        Log.d("AndroidApp", "Supabase initialized")
    }
}
