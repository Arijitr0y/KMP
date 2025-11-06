package org.example.project.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(
    vm: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val state = vm.uiState
    Surface(modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (state) {
                is AuthUiState.EmailEntry -> EmailStep(
                    onSubmit = { email -> vm.requestOtp(email) }
                )
                is AuthUiState.CodeEntry -> CodeStep(
                    email = state.email,
                    onConfirm = { code -> vm.verifyOtp(state.email, code) },
                    onBack = { vm.requestOtp(state.email) } // simple resend/back
                )
                is AuthUiState.Authed -> LoggedInStep(onSignOut = vm::signOut)
                is AuthUiState.Loading -> CircularProgressIndicator()
                is AuthUiState.Error -> ErrorStep(
                    message = state.message,
                    onRetry = { /* go back to email */ }
                )
            }
        }
    }
}

@Composable
private fun EmailStep(onSubmit: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    Column(
        Modifier.padding(24.dp).widthIn(max = 420.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Sign in with Email OTP", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { onSubmit(email.trim()) },
            enabled = email.contains("@"),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Send Code") }
    }
}

@Composable
private fun CodeStep(email: String, onConfirm: (String) -> Unit, onBack: () -> Unit) {
    var code by remember { mutableStateOf("") }
    Column(
        Modifier.padding(24.dp).widthIn(max = 420.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Enter the code sent to", style = MaterialTheme.typography.titleLarge)
        Text(email, style = MaterialTheme.typography.bodyLarge) // <- fixed 'typography'
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text("6-digit code") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { onConfirm(code.trim()) },
            enabled = code.length >= 6,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Verify") }

        TextButton(onClick = onBack, modifier = Modifier.align(Alignment.End)) {
            Text("Resend / Change email")
        }
    }
}

@Composable
private fun LoggedInStep(onSignOut: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("You’re signed in ✅", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onSignOut) { Text("Sign out") }
    }
}

@Composable
private fun ErrorStep(message: String, onRetry: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Error", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRetry) { Text("Try again") }
    }
}
