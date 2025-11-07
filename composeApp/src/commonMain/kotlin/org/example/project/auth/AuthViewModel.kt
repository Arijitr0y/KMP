package org.example.project.auth

import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AuthMode { Login, Signup, Forgot, VerifyOtp }

data class AuthUiState(
    val mode: AuthMode = AuthMode.Login,
    val email: String = "",
    val password: String = "",
    val confirm: String = "",      // only for sign-up
    val code: String = "",         // OTP
    val loading: Boolean = false
)

class AuthViewModel(
    private val repo: AuthRepository = AuthRepository(),
    private val appScope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val toast: (String) -> Unit = {}
) {
    private val _ui = MutableStateFlow(AuthUiState())
    val ui: StateFlow<AuthUiState> = _ui.asStateFlow()

    val sessionStatus: Flow<SessionStatus> = repo.sessionStatus

    fun setEmail(v: String) = _ui.update { it.copy(email = v.trim()) }
    fun setPassword(v: String) = _ui.update { it.copy(password = v) }
    fun setConfirm(v: String) = _ui.update { it.copy(confirm = v) }
    fun setCode(v: String) = _ui.update { it.copy(code = v.trim()) }
    fun switchMode(mode: AuthMode) = _ui.update { it.copy(mode = mode) }

    // ---------- Login ----------
    fun login() {
        val (email, password) = ui.value.let { it.email to it.password }
        if (email.isBlank() || password.isBlank()) { toast("Enter email & password"); return }
        appScope.launch {
            _ui.update { it.copy(loading = true) }
            try {
                repo.loginEmailPassword(email, password)
                toast("Welcome back")
            } catch (e: Throwable) {
                toast(e.message ?: "Login failed")
            } finally { _ui.update { it.copy(loading = false) } }
        }
    }

    // ---------- Sign-up (OTP verify first, then set password) ----------
    fun startSignup() {
        val s = ui.value
        if (s.email.isBlank()) { toast("Enter email"); return }
        if (s.password.length < 6) { toast("Password must be at least 6 chars"); return }
        if (s.password != s.confirm) { toast("Passwords don’t match"); return }

        appScope.launch {
            _ui.update { it.copy(loading = true) }
            try {
                // Optional: check existence (Edge Function when you add it)
                val exists = repo.userExists(s.email)
                if (exists) {
                    toast("Already a user, please login")
                    _ui.update { it.copy(mode = AuthMode.Login, loading = false) }
                    return@launch
                }
                repo.requestEmailOtp(s.email)
                _ui.update { it.copy(mode = AuthMode.VerifyOtp, loading = false) }
                toast("We sent a code to ${s.email}")
            } catch (e: Throwable) {
                toast(e.message ?: "Couldn’t send code"); _ui.update { it.copy(loading = false) }
            }
        }
    }

    fun verifyOtpThenSetPassword() {
        val s = ui.value
        if (s.code.length < 4) { toast("Enter the code"); return }
        appScope.launch {
            _ui.update { it.copy(loading = true) }
            try {
                repo.verifyEmailOtp(s.email, s.code)
                // Now user is signed-in via OTP; attach password to this account:
                repo.setPasswordForCurrentUser(s.password)
                toast("Account created")
                _ui.update { it.copy(mode = AuthMode.Login, code = "", password = "", confirm = "", loading = false) }
            } catch (e: Throwable) {
                toast(e.message ?: "Code verification failed")
                _ui.update { it.copy(loading = false) }
            }
        }
    }

    // ---------- Forgot ----------
    fun sendReset() {
        val email = ui.value.email
        if (email.isBlank()) { toast("Enter email"); return }
        appScope.launch {
            _ui.update { it.copy(loading = true) }
            try {
                repo.sendPasswordReset(email)
                toast("Reset link sent to $email")
            } catch (e: Throwable) {
                toast(e.message ?: "Couldn’t send reset link")
            } finally { _ui.update { it.copy(loading = false) } }
        }
    }
}
