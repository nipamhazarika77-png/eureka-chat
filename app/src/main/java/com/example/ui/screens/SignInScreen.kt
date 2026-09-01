package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DeepBlue
import com.example.ui.theme.Purple
import com.example.viewmodels.AuthState
import com.example.viewmodels.AuthViewModel

enum class AuthMode { EMAIL, PHONE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    authViewModel: AuthViewModel,
    onNavigateToMain: () -> Unit
) {
    var authMode by remember { mutableStateOf(AuthMode.EMAIL) }
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }

    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }

    val authState by authViewModel.authState.collectAsState()
    val otpSent by authViewModel.otpSent.collectAsState()

    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onNavigateToMain()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "EUREKA",
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Connect. Communicate. Discover.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Auth Mode Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (authMode == AuthMode.EMAIL) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { authMode = AuthMode.EMAIL; authViewModel.resetOtpState() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Email", color = if (authMode == AuthMode.EMAIL) Color.White else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (authMode == AuthMode.PHONE) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { authMode = AuthMode.PHONE; isRegistering = false },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Phone", color = if (authMode == AuthMode.PHONE) Color.White else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (authMode == AuthMode.EMAIL) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp)
                )
            } else {
                if (!otpSent) {
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number (e.g. +1234567890)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = { otpCode = it },
                        label = { Text("Enter OTP Code") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (authState is AuthState.Loading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else {
                Button(
                    onClick = {
                        if (authMode == AuthMode.EMAIL) {
                            if (isRegistering) {
                                authViewModel.register(email, password)
                            } else {
                                authViewModel.signIn(email, password)
                            }
                        } else {
                            if (!otpSent) {
                                activity?.let {
                                    authViewModel.sendOtp(phoneNumber, it)
                                }
                            } else {
                                authViewModel.verifyOtp(otpCode)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.horizontalGradient(listOf(DeepBlue, Purple))),
                        contentAlignment = Alignment.Center
                    ) {
                        val buttonText = when {
                            authMode == AuthMode.EMAIL && isRegistering -> "Register"
                            authMode == AuthMode.EMAIL -> "Sign In"
                            authMode == AuthMode.PHONE && !otpSent -> "Send OTP"
                            else -> "Verify OTP"
                        }
                        Text(
                            text = buttonText,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (authMode == AuthMode.EMAIL) {
                TextButton(
                    onClick = { isRegistering = !isRegistering }
                ) {
                    Text(
                        text = if (isRegistering) "Already have an account? Sign In" else "Don't have an account? Register",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else if (otpSent) {
                TextButton(
                    onClick = { authViewModel.resetOtpState() }
                ) {
                    Text(
                        text = "Change Phone Number",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (authState is AuthState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (authState as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}
