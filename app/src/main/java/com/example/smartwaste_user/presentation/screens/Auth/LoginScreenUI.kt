package com.example.smartwaste_user.presentation.screens.Auth

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Eco
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.smartwaste_user.R
import com.example.smartwaste_user.presentation.navigation.Routes
import com.example.smartwaste_user.presentation.viewmodels.AuthViewModel
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch

// Dark theme colors for Auth screens
object AuthDarkTheme {
    val background = Color(0xFF0D1117)
    val surface = Color(0xFF161B22)
    val surfaceVariant = Color(0xFF21262D)
    val primary = Color(0xFF2ECC71)
    val primaryVariant = Color(0xFF27AE60)
    val secondary = Color(0xFF58A6FF)
    val accent = Color(0xFFE3B341)
    val error = Color(0xFFFF6B6B)
    val textPrimary = Color(0xFFE6EDF3)
    val textSecondary = Color(0xFF8B949E)
    val cardBackground = Color(0xFF1C2128)
    val borderColor = Color(0xFF30363D)
    val inputBackground = Color(0xFF0D1117)
    val googleBlue = Color(0xFF4285F4)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreenUI(
    viewModel: AuthViewModel = hiltViewModel(),
    navController: NavController
) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val loginState by viewModel.loginState.collectAsState()
    val googleSignInState by viewModel.googleSignInState.collectAsState()

    val isLoading = loginState.isLoading || googleSignInState.isLoading

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

    LaunchedEffect(loginState.error) {
        loginState.error?.let { error ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = error,
                    duration = SnackbarDuration.Long
                )
            }
        }
    }

    LaunchedEffect(googleSignInState.error) {
        googleSignInState.error?.let { error ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = error,
                    duration = SnackbarDuration.Long
                )
            }
        }
    }

    LaunchedEffect(loginState.success) {
        loginState.success?.let {
            navController.navigate(Routes.HomeScreen) {
                popUpTo(Routes.LoginScreen) { inclusive = true }
            }
        }
    }

    LaunchedEffect(googleSignInState.success) {
        googleSignInState.success?.let {
            navController.navigate(Routes.HomeScreen) {
                popUpTo(Routes.LoginScreen) { inclusive = true }
            }
        }
    }

    val oneTapClient = remember { Identity.getSignInClient(context) }
    val signInRequest = remember {
        BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            ).build()
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        try {
            val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
            val idToken = credential.googleIdToken
            if (idToken != null) {
                viewModel.signInWithGoogle(idToken)
            } else {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Google Sign-in failed: No ID token received")
                }
            }
        } catch (e: Exception) {
            Log.e("LoginScreen", "Google Sign-in failed", e)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Google Sign-in failed: ${e.localizedMessage}")
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
                                    imageVector = Icons.Default.Eco,
                                    contentDescription = "SmartWaste",
                                    tint = AuthDarkTheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                "Welcome Back",
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier.size(180.dp),
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
                            contentDescription = "Welcome Illustration",
                            modifier = Modifier
                                .size(120.dp)
                                .let { if (isLoading) it.blur(2.dp) else it }
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "SmartWaste Login",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuthDarkTheme.textPrimary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Sign in to manage your waste efficiently",
                        fontSize = 16.sp,
                        color = AuthDarkTheme.textSecondary,
                        textAlign = TextAlign.Center
                    )
                }

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
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address", color = AuthDarkTheme.textSecondary) },
                            singleLine = true,
                            enabled = !isLoading,
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
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AuthDarkTheme.primary,
                                unfocusedBorderColor = AuthDarkTheme.borderColor,
                                focusedTextColor = AuthDarkTheme.textPrimary,
                                unfocusedTextColor = AuthDarkTheme.textPrimary,
                                cursorColor = AuthDarkTheme.primary,
                                focusedLabelColor = AuthDarkTheme.primary,
                                unfocusedLabelColor = AuthDarkTheme.textSecondary,
                                disabledBorderColor = AuthDarkTheme.borderColor.copy(alpha = 0.5f),
                                disabledTextColor = AuthDarkTheme.textSecondary.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password", color = AuthDarkTheme.textSecondary) },
                            singleLine = true,
                            enabled = !isLoading,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    if (email.isNotBlank() && password.isNotBlank()) {
                                        viewModel.loginUserWithEmailAndPassword(email.trim(), password.trim())
                                    }
                                }
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
                                IconButton(
                                    onClick = { passwordVisible = !passwordVisible },
                                    enabled = !isLoading
                                ) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                        tint = AuthDarkTheme.textSecondary
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AuthDarkTheme.primary,
                                unfocusedBorderColor = AuthDarkTheme.borderColor,
                                focusedTextColor = AuthDarkTheme.textPrimary,
                                unfocusedTextColor = AuthDarkTheme.textPrimary,
                                cursorColor = AuthDarkTheme.primary,
                                focusedLabelColor = AuthDarkTheme.primary,
                                unfocusedLabelColor = AuthDarkTheme.textSecondary,
                                disabledBorderColor = AuthDarkTheme.borderColor.copy(alpha = 0.5f),
                                disabledTextColor = AuthDarkTheme.textSecondary.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                when {
                                    email.isBlank() -> {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Please enter your email address")
                                        }
                                    }
                                    password.isBlank() -> {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Please enter your password")
                                        }
                                    }
                                    else -> {
                                        viewModel.loginUserWithEmailAndPassword(email.trim(), password.trim())
                                    }
                                }
                            },
                            enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
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
                                visible = loginState.isLoading,
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
                                visible = !loginState.isLoading,
                                enter = fadeIn(tween(300)),
                                exit = fadeOut(tween(300))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Login,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Sign In",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            if (loginState.isLoading) {
                                Text(
                                    text = "Signing In...",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = AuthDarkTheme.borderColor
                            )
                            Text(
                                text = "  OR  ",
                                color = AuthDarkTheme.textSecondary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = AuthDarkTheme.borderColor
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                oneTapClient.beginSignIn(signInRequest)
                                    .addOnSuccessListener { result ->
                                        launcher.launch(
                                            IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                                        )
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("LoginScreen", "Google Sign-in failed", e)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Google Sign-in failed: ${e.localizedMessage}")
                                        }
                                    }
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.5.dp, AuthDarkTheme.googleBlue),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = AuthDarkTheme.googleBlue,
                                containerColor = Color.Transparent,
                                disabledContentColor = AuthDarkTheme.textSecondary
                            )
                        ) {
                            AnimatedVisibility(
                                visible = googleSignInState.isLoading,
                                enter = fadeIn(tween(300)),
                                exit = fadeOut(tween(300))
                            ) {
                                CircularProgressIndicator(
                                    color = AuthDarkTheme.googleBlue,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }

                            AnimatedVisibility(
                                visible = !googleSignInState.isLoading,
                                enter = fadeIn(tween(300)),
                                exit = fadeOut(tween(300))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_google),
                                        contentDescription = null,
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Continue with Google",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            if (googleSignInState.isLoading) {
                                Text(
                                    text = "Authenticating...",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AuthDarkTheme.googleBlue
                                )
                            }
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Don't have an account? ",
                        color = AuthDarkTheme.textSecondary,
                        fontSize = 16.sp
                    )
                    TextButton(
                        onClick = {
                            if (!isLoading) {
                                navController.navigate(Routes.SignUpScreen)
                            }
                        },
                        enabled = !isLoading
                    ) {
                        Text(
                            text = "Sign Up",
                            color = AuthDarkTheme.primary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }


        AnimatedVisibility(
            visible = isLoading,
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
                            text = when {
                                loginState.isLoading -> "Signing you in..."
                                googleSignInState.isLoading -> "Authenticating with Google..."
                                else -> "Please wait..."
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = AuthDarkTheme.textPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}