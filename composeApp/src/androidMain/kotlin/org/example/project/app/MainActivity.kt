package org.example.project.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.example.project.AppUI
import org.example.project.auth.AuthViewModel
import org.example.project.auth.handleOAuthDeepLinkIfAny

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppUI(vm = AuthViewModel())
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // intent is non-null here
        handleOAuthDeepLinkIfAny(intent.data)
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
