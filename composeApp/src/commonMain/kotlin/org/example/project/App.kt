package org.example.project

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.example.project.auth.AuthScreen
import org.example.project.auth.AuthViewModel
import io.github.jan.supabase.auth.status.SessionStatus

@Composable
fun AppUI(vm: AuthViewModel) {
    // ⬇️ Use nullable initial instead of SessionStatus.Loading
    val status: SessionStatus? by vm.sessionStatus.collectAsState(initial = null)

    val isLoggedIn = status is SessionStatus.Authenticated

    if (isLoggedIn) {
        MainTabs(onSignOut = { vm.logout() })
    } else {
        MaterialTheme { Surface { AuthScreen(vm) } }
    }
}

@Composable
private fun MainTabs(onSignOut: () -> Unit) {
    data class Tab(val label: String, val content: @Composable () -> Unit)

    val tabs = remember {
        listOf(
            Tab("Home")       { Text("Home") },
            Tab("Categories") { Text("Categories") },
            Tab("Bookings")   { Text("Bookings") },
            Tab("Profile")    {
                Column {
                    Text("Profile")
                    Spacer(Modifier)
                    Button(onClick = onSignOut) { Text("Sign out") }
                }
            }
        )
    }
    var current by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { i, t ->
                    NavigationBarItem(
                        selected = current == i,
                        onClick = { current = i },
                        icon = { /* no icon */ },
                        label = { Text(t.label) }
                    )
                }
            }
        }
    ) { _ ->
        Surface(Modifier.fillMaxSize()) { tabs[current].content() }
    }
}
