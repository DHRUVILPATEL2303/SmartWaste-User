package com.example.smartwaste_user.presentation.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun HomeScreenUI(navController: NavHostController) {
    var showVerifyDialog by remember { mutableStateOf(false) }
    var isVerified by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        val user = Firebase.auth.currentUser
        if (user != null && !user.isEmailVerified) {
            showVerifyDialog = true
        }
    }

    // UI
    Box(modifier = Modifier.fillMaxSize()) {
        // Your HomeScreen content
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Welcome to Home!", style = MaterialTheme.typography.headlineSmall)
        }

        // Show dialog if not verified
        if (showVerifyDialog && !isVerified) {
            EmailVerificationDialog(
                onDismiss = { /* Optional */ },
                onRefresh = {
                    val user = Firebase.auth.currentUser
                    user?.reload()?.addOnCompleteListener {
                        if (user.isEmailVerified) {
                            isVerified = true
                            showVerifyDialog = false
                        }
                    }
                },
                onResend = {
                    Firebase.auth.currentUser?.sendEmailVerification()
                }
            )
        }
    }
}


@Composable
fun EmailVerificationDialog(
    onDismiss: () -> Unit,
    onRefresh: () -> Unit,
    onResend: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onRefresh) {
                Text("I've Verified")
            }
        },
        dismissButton = {
            Button(onClick = onResend) {
                Text("Resend Email")
            }
        },
        title = { Text("Email Not Verified") },
        text = {
            Text("Please verify your email address to unlock all features.")
        }
    )
}