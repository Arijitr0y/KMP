package org.example.project.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.example.project.AppUI
import org.example.project.auth.AuthViewModel
import org.example.project.auth.SupabaseHolder
import org.example.project.auth.handleOAuthDeepLinkIfAny

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true) // <-- key line
        handleOAuthDeepLink(intent?.data)   // ⬅️ when activity is (re)launched by redirect
        setContent {
            val ctx = LocalContext.current
            val mainHandler = remember { Handler(Looper.getMainLooper()) }
            val vm = remember {
                AuthViewModel(toast = { msg ->
                    mainHandler.post { Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show() }
                })
            }
            AppUI(vm = vm)
        }

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleOAuthDeepLink(intent.data)    // ⬅️ when activity was already running
    }

    // MainActivity.kt
    private fun handleOAuthDeepLink(uri: Uri?) {
        if (uri == null) return

        // Only require our scheme; accept any host/path/query that Supabase sends back
        if (uri.scheme != "org.example.project") return

        lifecycleScope.launch(Dispatchers.IO) {
            val auth = SupabaseHolder.client.auth
            val url = uri.toString()
            val ok = try {
                auth.exchangeCodeForSession(url)   // PKCE → creates the session if 'code' is present
                true
            } catch (e: Throwable) {
                // TEMP: show why it failed
                launch(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "OAuth failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
                false
            }

            if (ok) {
                // Optional but helpful: force a state tick so your UI re-checks
                // Not strictly needed, sessionStatus should emit by itself.
                // You can also do: auth.refreshCurrentSession()
                launch(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Signed in with Google ✅", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}
// This is the ONLY AppRoot in the project
@Composable
fun AppRoot() {
    val ctx = LocalContext.current
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    val vm = remember {
        AuthViewModel(
            toast = { msg ->
                // Always post to main thread to avoid "Looper.prepare()" crash
                mainHandler.post {
                    Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
    AppUI(vm)
}
