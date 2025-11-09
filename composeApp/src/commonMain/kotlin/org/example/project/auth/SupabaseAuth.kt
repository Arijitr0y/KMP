package org.example.project.auth

import com.russhwolf.settings.Settings
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.SettingsSessionManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.OAuthProvider
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.auth.providers.builtin.Email


import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow



// ⬇️ Put this once, near the top, and delete any duplicate "expect object SupabasePlatform"
expect object SupabasePlatform {
    val supabaseUrl: String
    val supabaseAnonKey: String
    val oauthRedirectUri: String          // <-- add this line
    fun createClient(settings: Settings): SupabaseClient
}


object SupabaseHolder {
    lateinit var client: SupabaseClient
        private set
    fun init(settings: Settings) {
        if (!::client.isInitialized) client = SupabasePlatform.createClient(settings)
    }
}
// 👇 makes oauthRedirectUri available in common code


// … keep your existing SupabaseHolder, Settings, etc …

class AuthRepository(
    private val client: SupabaseClient = SupabaseHolder.client
) {
    private val auth: Auth get() = client.auth

    // --- Google OAuth (version-compatible) ---

    suspend fun loginWithGoogle() {
        auth.signInWith(
            provider = Google,
            redirectUrl = "org.example.project://auth-callback"
        )
    }


    // --- Email/password ---
    suspend fun loginEmailPassword(email: String, password: String) {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun requestEmailOtp(email: String) {
        auth.signInWith(OTP) { this.email = email }
    }


    // Verify the OTP that came via email
    suspend fun verifyEmailOtp(email: String, code: String) {
        auth.verifyEmailOtp(
            type = OtpType.Email.EMAIL,   // <-- NOT a function; just the enum constant
            email = email,
            token = code
        )
        // After this call the user is signed in (passwordless) for that email.
    }

    // Attach a password to the now-signed-in account
    suspend fun setPasswordForCurrentUser(password: String) {
        auth.updateUser {                 // <-- no UserUpdate class needed
            this.password = password
        }
    }

    suspend fun sendPasswordReset(email: String) = auth.resetPasswordForEmail(email)

    val sessionStatus: Flow<SessionStatus> get() = auth.sessionStatus
    suspend fun signOut() = auth.signOut()

    suspend fun userExists(email: String): Boolean = false // (stub until you add an Edge Function)
}
