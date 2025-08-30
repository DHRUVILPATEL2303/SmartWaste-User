package com.example.smartwaste_user.presentation.screens.Auth

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.smartwaste_user.R
import com.example.smartwaste_user.data.models.UserModel
import com.example.smartwaste_user.presentation.navigation.Routes
import com.example.smartwaste_user.presentation.viewmodels.AuthViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreenUI(
    viewModel: AuthViewModel = hiltViewModel<AuthViewModel>(),
    navController: NavHostController
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val state by viewModel.signUpState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()


    val isEmailValid = email.contains("@") && email.contains(".")
    val isPasswordValid = password.length >= 6
    val doPasswordsMatch = password == confirmPassword && confirmPassword.isNotEmpty()
    val isFormValid = name.isNotBlank() && isEmailValid && isPasswordValid && doPasswordsMatch


    val postNotificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Notification permission denied. You might miss important updates.",
                        duration = SnackbarDuration.Long
                    )
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionStatus = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                postNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(state.success) {
        state.success?.let {
            navController.navigate(Routes.HomeScreen) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
    }

    LaunchedEffect(state.error) {
        if (state.error.isNotBlank()) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = state.error,
                    duration = SnackbarDuration.Long
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AuthDarkTheme.background,
                        AuthDarkTheme.surface
                    )
                )
            )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        AuthDarkTheme.primary.copy(alpha = 0.15f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = "Create Account",
                                    tint = AuthDarkTheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                "Create Account",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = AuthDarkTheme.textPrimary
                            )
                        }
                    },

                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent,

        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Welcome illustration
                item {
                    Card(
                        modifier = Modifier.size(160.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = AuthDarkTheme.primary.copy(alpha = 0.1f)
                        ),
                        shape = CircleShape,
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.signup),
                                contentDescription = "Create Account Illustration",
                                modifier = Modifier
                                    .size(100.dp)
                                    .let { if (state.isLoading) it.blur(2.dp) else it }
                            )
                        }
                    }
                }

                // Welcome text
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Join SmartWaste",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = AuthDarkTheme.textPrimary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Create your account to start managing waste efficiently",
                            fontSize = 16.sp,
                            color = AuthDarkTheme.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Signup form
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = AuthDarkTheme.cardBackground
                        ),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        border = BorderStroke(1.dp, AuthDarkTheme.borderColor)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Name field
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Full Name", color = AuthDarkTheme.textSecondary) },
                                singleLine = true,
                                enabled = !state.isLoading,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next,
                                    capitalization = KeyboardCapitalization.Words
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                ),
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = AuthDarkTheme.primary
                                    )
                                },
                                supportingText = if (name.isNotEmpty() && name.length < 2) {
                                    { Text("Name must be at least 2 characters", color = AuthDarkTheme.error) }
                                } else null,
                                isError = name.isNotEmpty() && name.length < 2,
                                modifier = Modifier.fillMaxWidth(),
                                colors = outlinedTextFieldColors(),
                                shape = RoundedCornerShape(16.dp)
                            )

                            // Email field
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email Address", color = AuthDarkTheme.textSecondary) },
                                singleLine = true,
                                enabled = !state.isLoading,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                ),
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Email,
                                        contentDescription = null,
                                        tint = AuthDarkTheme.primary
                                    )
                                },
                                trailingIcon = if (email.isNotEmpty()) {
                                    {
                                        Icon(
                                            if (isEmailValid) Icons.Default.CheckCircle else Icons.Default.Error,
                                            contentDescription = null,

                                        )
                                    }
                                } else null,
                                supportingText = if (email.isNotEmpty() && !isEmailValid) {
                                    { Text("Please enter a valid email address", color = AuthDarkTheme.error) }
                                } else null,
                                isError = email.isNotEmpty() && !isEmailValid,
                                modifier = Modifier.fillMaxWidth(),
                                colors = outlinedTextFieldColors(),
                                shape = RoundedCornerShape(16.dp)
                            )

                            // Password field
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password", color = AuthDarkTheme.textSecondary) },
                                singleLine = true,
                                enabled = !state.isLoading,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                ),
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = AuthDarkTheme.primary
                                    )
                                },
                                trailingIcon = {
                                    Row {
                                        if (password.isNotEmpty()) {
                                            Icon(
                                                if (isPasswordValid) Icons.Default.CheckCircle else Icons.Default.Error,
                                                contentDescription = null,
                                                tint = if (isPasswordValid) AuthDarkTheme.error else AuthDarkTheme.error,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        IconButton(
                                            onClick = { passwordVisible = !passwordVisible },
                                            enabled = !state.isLoading
                                        ) {
                                            Icon(
                                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                                tint = AuthDarkTheme.textSecondary
                                            )
                                        }
                                    }
                                },
                                supportingText = if (password.isNotEmpty() && !isPasswordValid) {
                                    { Text("Password must be at least 6 characters", color = AuthDarkTheme.error) }
                                } else null,
                                isError = password.isNotEmpty() && !isPasswordValid,
                                modifier = Modifier.fillMaxWidth(),
                                colors = outlinedTextFieldColors(),
                                shape = RoundedCornerShape(16.dp)
                            )

                            // Confirm password field
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Confirm Password", color = AuthDarkTheme.textSecondary) },
                                singleLine = true,
                                enabled = !state.isLoading,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        if (isFormValid) {
                                            val userModel = UserModel(
                                                userId = "",
                                                email = email.trim(),
                                                name = name.trim(),
                                                address = "",
                                                phoneNumber = "",
                                                profileImageUrl = "",
                                                areaName = "",
                                                routeName = "",

                                            )
                                            viewModel.signUpUser(password, userModel)
                                        }
                                    }
                                ),
                                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = AuthDarkTheme.primary
                                    )
                                },
                                trailingIcon = {
                                    Row {
                                        if (confirmPassword.isNotEmpty()) {
                                            Icon(
                                                if (doPasswordsMatch) Icons.Default.CheckCircle else Icons.Default.Error,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        IconButton(
                                            onClick = { confirmPasswordVisible = !confirmPasswordVisible },
                                            enabled = !state.isLoading
                                        ) {
                                            Icon(
                                                imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                                                tint = AuthDarkTheme.textSecondary
                                            )
                                        }
                                    }
                                },
                                supportingText = if (confirmPassword.isNotEmpty() && !doPasswordsMatch) {
                                    { Text("Passwords do not match", color = AuthDarkTheme.error) }
                                } else null,
                                isError = confirmPassword.isNotEmpty() && !doPasswordsMatch,
                                modifier = Modifier.fillMaxWidth(),
                                colors = outlinedTextFieldColors(),
                                shape = RoundedCornerShape(16.dp)
                            )

                            // Create account button
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    when {
                                        name.trim().length < 2 -> {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Please enter a valid name")
                                            }
                                        }
                                        !isEmailValid -> {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Please enter a valid email address")
                                            }
                                        }
                                        !isPasswordValid -> {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Password must be at least 6 characters")
                                            }
                                        }
                                        !doPasswordsMatch -> {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Passwords do not match")
                                            }
                                        }
                                        else -> {
                                            val userModel = UserModel(
                                                userId = "",
                                                email = email.trim(),
                                                name = name.trim(),
                                                address = "",
                                                phoneNumber = "",
                                                profileImageUrl = "",
                                                areaName = "",
                                                routeName = "",
                                            )
                                            viewModel.signUpUser(password, userModel)
                                        }
                                    }
                                },
                                enabled = !state.isLoading && isFormValid,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AuthDarkTheme.primary,
                                    contentColor = Color.White,
                                    disabledContainerColor = AuthDarkTheme.borderColor,
                                    disabledContentColor = AuthDarkTheme.textSecondary
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 4.dp
                                )
                            ) {
                                AnimatedVisibility(
                                    visible = state.isLoading,
                                    enter = fadeIn(tween(300)),
                                    exit = fadeOut(tween(300))
                                ) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                }

                                AnimatedVisibility(
                                    visible = !state.isLoading,
                                    enter = fadeIn(tween(300)),
                                    exit = fadeOut(tween(300))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.PersonAdd,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Create Account",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                if (state.isLoading) {
                                    Text(
                                        text = "Creating Account...",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }


                item {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Already have an account? ",
                            color = AuthDarkTheme.textSecondary,
                            fontSize = 16.sp
                        )
                        TextButton(
                            onClick = {
                                if (!state.isLoading) {
                                    navController.popBackStack()
                                }
                            },
                            enabled = !state.isLoading
                        ) {
                            Text(
                                text = "Sign In",
                                color = AuthDarkTheme.primary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }


        AnimatedVisibility(
            visible = state.isLoading,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = AuthDarkTheme.cardBackground
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                    border = BorderStroke(1.dp, AuthDarkTheme.borderColor)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        CircularProgressIndicator(
                            color = AuthDarkTheme.primary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Creating your account...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = AuthDarkTheme.textPrimary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Please wait while we set up your SmartWaste profile",
                            fontSize = 14.sp,
                            color = AuthDarkTheme.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun outlinedTextFieldColors(): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AuthDarkTheme.primary,
        unfocusedBorderColor = AuthDarkTheme.borderColor,
        focusedTextColor = AuthDarkTheme.textPrimary,
        unfocusedTextColor = AuthDarkTheme.textPrimary,
        cursorColor = AuthDarkTheme.primary,
        focusedLabelColor = AuthDarkTheme.primary,
        unfocusedLabelColor = AuthDarkTheme.textSecondary,
        disabledBorderColor = AuthDarkTheme.borderColor.copy(alpha = 0.5f),
        disabledTextColor = AuthDarkTheme.textSecondary.copy(alpha = 0.5f),
        errorBorderColor = AuthDarkTheme.error,
        errorTextColor = AuthDarkTheme.textPrimary,
        errorLabelColor = AuthDarkTheme.error
    )
}