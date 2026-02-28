package com.chatzz.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chatzz.ui.viewmodels.AuthViewModel

@Composable
fun LoginScreen(viewModel: AuthViewModel) {
    val email by viewModel.email.collectAsState()
    val isSignUp by viewModel.isSignUp.collectAsState()
    val isOtpSent by viewModel.isOtpSent.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var otp by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    LaunchedEffect(isOtpSent, isSignUp) {
        if (isOtpSent) {
            otp = if (isSignUp) "123456" else "123456demo!"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = if (isSignUp) "Create Account" else "Welcome Back", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(32.dp))

        error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (!isOtpSent) {
            OutlinedTextField(
                value = email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.sendOtp() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                else Text(if (isSignUp) "Send OTP" else "Continue to Login")
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { viewModel.toggleSignUpMode() }) {
                Text(if (isSignUp) "Already have an account? Login" else "Don't have an account? Sign Up")
            }
        } else {
            OutlinedTextField(
                value = otp,
                onValueChange = { otp = it },
                label = { Text(if (isSignUp) "Enter OTP (123456)" else "Enter Password") },
                modifier = Modifier.fillMaxWidth()
            )
            if (isSignUp) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Create Password") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.verifyOtp(otp, password) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                else Text(if (isSignUp) "Verify OTP & Sign Up" else "Login")
            }
        }
    }
}
