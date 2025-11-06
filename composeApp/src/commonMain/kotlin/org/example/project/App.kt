package org.example.project.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import org.example.project.auth.AuthScreen
import org.example.project.auth.AuthViewModel

/** Small root Composable to host the Auth flow. */
@Composable
fun AppRoot() {
    MaterialTheme {
        Surface {
            AuthScreen(vm = AuthViewModel())
        }
    }
}
