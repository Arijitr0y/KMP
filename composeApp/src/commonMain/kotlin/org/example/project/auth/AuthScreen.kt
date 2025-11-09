package org.example.project.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(vm: AuthViewModel) {
    val ui by vm.ui.collectAsState()

    Column(Modifier.padding(24.dp)) {
        // Tabs
        TabRow(selectedTabIndex = when (ui.mode) {
            AuthMode.Login -> 0; AuthMode.Signup, AuthMode.VerifyOtp -> 1; AuthMode.Forgot -> 2
        }) {
            Tab(selected = ui.mode == AuthMode.Login,  onClick = { vm.switchMode(AuthMode.Login) },  text = { Text("Login") })
            Tab(selected = ui.mode == AuthMode.Signup || ui.mode == AuthMode.VerifyOtp,
                onClick = { vm.switchMode(AuthMode.Signup) }, text = { Text("Sign up") })
            Tab(selected = ui.mode == AuthMode.Forgot, onClick = { vm.switchMode(AuthMode.Forgot) }, text = { Text("Forgot") })
        }

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = ui.email, onValueChange = vm::setEmail,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), singleLine = true
        )

        when (ui.mode) {
            AuthMode.Login -> {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = ui.password, onValueChange = vm::setPassword,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true
                )
                Spacer(Modifier.height(16.dp))

                // Example: show under the action button in Login block
                Button(onClick = vm::login, enabled = !ui.loading) {
                    Text(if (ui.loading) "Signing in..." else "Login")
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = vm::loginWithGoogle,
                    enabled = !ui.loading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continue with Google")
                }

            }

            AuthMode.Signup -> {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = ui.password, onValueChange = vm::setPassword,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = ui.confirm, onValueChange = vm::setConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Confirm password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = vm::startSignup, enabled = !ui.loading) {
                    Text(if (ui.loading) "Sending code..." else "Continue")
                }
                // Example: show under the action button in Login block
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = vm::loginWithGoogle,
                    enabled = !ui.loading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continue with Google")
                }

            }

            AuthMode.VerifyOtp -> {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = ui.code, onValueChange = vm::setCode,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Code from email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = vm::verifyOtpThenSetPassword, enabled = !ui.loading) {
                    Text(if (ui.loading) "Verifying..." else "Verify & create account")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { vm.switchMode(AuthMode.Signup) }) { Text("Back") }
            }

            AuthMode.Forgot -> {
                Spacer(Modifier.height(16.dp))
                Button(onClick = vm::sendReset, enabled = !ui.loading) {
                    Text(if (ui.loading) "Sending..." else "Send reset link")
                }
            }
        }
    }
}
