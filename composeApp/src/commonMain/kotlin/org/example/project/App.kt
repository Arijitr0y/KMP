package org.example.project

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import org.example.project.auth.AuthScreen
import org.example.project.auth.AuthViewModel

// Shared UI. Keep this name AppUI (NOT AppRoot) to avoid conflicts.
@Composable
fun AppUI(vm: AuthViewModel) {
    MaterialTheme {
        Surface {
            AuthScreen(vm)
        }
    }
}
