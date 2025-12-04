package com.example.itshere

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.itshere.ViewModel.SignUpViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SignUpScreen(
    viewModel: SignUpViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onSignUpSuccess: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var showVerificationSentDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val state by viewModel.state.collectAsState()
    val auth = FirebaseAuth.getInstance()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {
        // Loading overlay
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF9FA8DA))
            }
        }

        // Back button
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            // Header
            Text(
                text = "Hey, ur thing",
                fontSize = 20.sp,
                color = Color.Black
            )
            Text(
                text = "IT'sHere",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Sign Up.",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Name field
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Name",
                    fontSize = 14.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = state.name,
                    onValueChange = viewModel::onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color(0xFFFFCDD2),
                        unfocusedContainerColor = Color(0xFFFFCDD2),
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        errorBorderColor = Color.Red,
                        errorContainerColor = Color(0xFFFFCDD2)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true,
                    isError = state.nameError != null,
                    enabled = !state.isLoading
                )
                state.nameError?.let { error ->
                    Text(
                        text = error,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Phone field
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Phone",
                    fontSize = 14.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = state.phone,
                    onValueChange = { newValue ->
                        val filteredValue = newValue.filter { it.isDigit() }
                        viewModel.onPhoneChange(filteredValue)
                    },
                    placeholder = { Text("+60 123456789", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color(0xFFFFCDD2),
                        unfocusedContainerColor = Color(0xFFFFCDD2),
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        errorBorderColor = Color.Red,
                        errorContainerColor = Color(0xFFFFCDD2)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        autoCorrect = false
                    ),
                    isError = state.phoneError != null,
                    enabled = !state.isLoading
                )
                state.phoneError?.let { error ->
                    Text(
                        text = error,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Email field
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Email",
                    fontSize = 14.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = state.email,
                    onValueChange = viewModel::onEmailChange,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color(0xFFFFCDD2),
                        unfocusedContainerColor = Color(0xFFFFCDD2),
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        errorBorderColor = Color.Red,
                        errorContainerColor = Color(0xFFFFCDD2)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = state.emailError != null,
                    enabled = !state.isLoading
                )
                state.emailError?.let { error ->
                    Text(
                        text = error,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Password field
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Password",
                    fontSize = 14.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = state.password,
                    onValueChange = viewModel::onPasswordChange,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color(0xFFFFCDD2),
                        unfocusedContainerColor = Color(0xFFFFCDD2),
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        errorBorderColor = Color.Red,
                        errorContainerColor = Color(0xFFFFCDD2)
                    ),
                    shape = RoundedCornerShape(20.dp), // 改為20度
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = Color.Black
                            )
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = state.passwordError != null,
                    enabled = !state.isLoading
                )
                state.passwordError?.let { error ->
                    Text(
                        text = error,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Confirm Password",
                    fontSize = 14.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = state.confirmPassword,
                    onValueChange = viewModel::onConfirmPasswordChange,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color(0xFFFFCDD2),
                        unfocusedContainerColor = Color(0xFFFFCDD2),
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        errorBorderColor = Color.Red,
                        errorContainerColor = Color(0xFFFFCDD2)
                    ),
                    shape = RoundedCornerShape(20.dp), // 改為20度
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                                tint = Color.Black
                            )
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = state.confirmPasswordError != null,
                    enabled = !state.isLoading
                )
                state.confirmPasswordError?.let { error ->
                    Text(
                        text = error,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.signUp(
                        onSuccess = { showVerificationSentDialog = true },
                        onError = { error ->
                            errorMessage = error
                            showErrorDialog = true
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9FA8DA)
                ),
                shape = RoundedCornerShape(20.dp),
                enabled = !state.isLoading
            ) {
                Text(
                    text = if (state.isLoading) "Creating Account..." else "Sign Up",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    // Email Verification Sent Dialog
    if (showVerificationSentDialog) {
        AlertDialog(
            onDismissRequest = { /* Don't allow dismiss */ },
            title = {
                Text(
                    "Verify Your Email",
                    color = Color(0xFF9FA8DA),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "We've sent a verification email to:",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Text(
                        state.email,
                        color = Color(0xFF9FA8DA),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        "Please check your inbox and verify your email address before you can log in.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Important: You must verify your email to access your account.",
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showVerificationSentDialog = false
                        auth.signOut()
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9FA8DA)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Go to Login", color = Color.White)
                }
            }
        )
    }

    // Error Dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = {
                Text(
                    "Sign Up Failed",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(errorMessage, color = Color.Gray)
            },
            confirmButton = {
                Button(
                    onClick = { showErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9FA8DA)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("OK", color = Color.White)
                }
            }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
internal fun SignUpScreenPreview() {
    SignUpScreen(
        onNavigateBack = {},
        onSignUpSuccess = {}
    )
}