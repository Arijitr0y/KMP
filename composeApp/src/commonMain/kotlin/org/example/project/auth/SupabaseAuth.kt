package org.example.project.auth

import com.russhwolf.settings.Settings
import io.github.jan.supabase.SupabaseClient


import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.*
//import io.github.jan.supabase.auth.otp.OtpType

import kotlinx.coroutines.flow.Flow
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.SettingsSessionManager
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.providers.builtin.OTP
//import io.github.jan.supabase.auth.providers.builtin.OtpType


// Platform bridge implemented per-target
expect object SupabasePlatform {
    val supabaseUrl: String
    val supabaseAnonKey: String
    fun createClient(settings: Settings): SupabaseClient
}

// Single client instance (init once at app start)
object SupabaseHolder {
    lateinit var client: SupabaseClient
        private set

    fun init(settings: Settings) {
        if (::client.isInitialized) return
        client = SupabasePlatform.createClient(settings)
    }
}

// Repository wrapping Auth API
class AuthRepository(
    private val client: SupabaseClient = SupabaseHolder.client
) {
    private val auth: Auth get() = client.auth

    suspend fun requestEmailOtp(email: String) {
        auth.signInWith(OTP) { this.email = email }
    }

    suspend fun verifyEmailOtp(email: String, code: String) {
        auth.verifyEmailOtp(
            type = OtpType.Email.EMAIL,   // <- correct for 3.2.6
            email = email,
            token = code
        )
    }


    val sessionStatus: Flow<SessionStatus> get() = auth.sessionStatus

    suspend fun signOut() = auth.signOut()
}
