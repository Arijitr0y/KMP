package org.example.project.app

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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppRoot() }
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
