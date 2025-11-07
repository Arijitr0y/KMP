package org.example.project.app

import android.app.Application
import org.example.project.auth.SupabaseHolder
import org.example.project.auth.defaultAndroidSettings
import org.example.project.auth.initSupabaseAndroid

class AndroidApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initSupabaseAndroid(this)
        SupabaseHolder.init(defaultAndroidSettings())
    }
}
