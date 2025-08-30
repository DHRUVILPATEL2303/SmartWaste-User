package com.example.smartwaste_user.presentation.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.smartwaste_user.data.models.UserModel
import com.example.smartwaste_user.presentation.navigation.Routes
import com.example.smartwaste_user.presentation.viewmodels.UserViewModel
import com.google.firebase.auth.FirebaseAuth

object ProfileDarkTheme {
    val background = Color(0xFF0D1117)
    val surface = Color(0xFF161B22)
    val surfaceVariant = Color(0xFF21262D)
    val primary = Color(0xFF58A6FF)
    val secondary = Color(0xFF79C0FF)
    val accent = Color(0xFFFF7B72)
    val success = Color(0xFF56D364)
    val warning = Color(0xFFE3B341)
    val textPrimary = Color(0xFFE6EDF3)
    val textSecondary = Color(0xFF8B949E)
    val cardBackground = Color(0xFF1C2128)
    val cardHover = Color(0xFF262C36)
    val divider = Color(0xFF30363D)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenUI(
    viewModel: UserViewModel = hiltViewModel<UserViewModel>(),
    navController: NavHostController
) {
    val userState by viewModel.userState.collectAsState()
    val qrCodeBitmap by viewModel.qrCodeBitmap.collectAsState()
    var showQRDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showPhotoDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Profile",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = ProfileDarkTheme.textPrimary
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showQRDialog = true },
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                color = ProfileDarkTheme.primary.copy(alpha = 0.1f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.QrCode,
                            contentDescription = "Show QR Code",
                            tint = ProfileDarkTheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { /* Handle settings */ },
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                color = ProfileDarkTheme.secondary.copy(alpha = 0.1f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = ProfileDarkTheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ProfileDarkTheme.cardBackground
                )
            )
        },
        containerColor = ProfileDarkTheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            ProfileDarkTheme.background,
                            ProfileDarkTheme.surface
                        )
                    )
                )
        ) {
            when {
                userState.isLoading -> {
                    LoadingState()
                }

                userState.error.isNotEmpty() -> {
                    ErrorState(
                        error = userState.error,
                        onRetry = { viewModel.getUserData() }
                    )
                }

                userState.succcess != null -> {
                    val user = userState.succcess!!

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        item {
                            ProfileHeaderCard(
                                user = user,
                                onPhotoClick = { showPhotoDialog = true }
                            )
                        }

                        item {
                            InfoCard(
                                title = "Contact Information",
                                icon = Icons.Default.ContactPhone
                            ) {
                                ProfileInfoItem(
                                    icon = Icons.Default.Phone,
                                    title = "Phone Number",
                                    value = user.phoneNumber.ifEmpty { "Not provided" }
                                )
                                HorizontalDivider(
                                    color = ProfileDarkTheme.divider,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                                ProfileInfoItem(
                                    icon = Icons.Default.LocationOn,
                                    title = "Address",
                                    value = user.address.ifEmpty { "Not provided" }
                                )
                            }
                        }

                        item {
                            InfoCard(
                                title = "Service Area",
                                icon = Icons.Default.LocationCity
                            ) {
                                ProfileInfoItem(
                                    icon = Icons.Default.Place,
                                    title = "Area Name",
                                    value = user.areaName.ifEmpty { "Not assigned" }
                                )
                                HorizontalDivider(
                                    color = ProfileDarkTheme.divider,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                                ProfileInfoItem(
                                    icon = Icons.Default.Route,
                                    title = "Route Name",
                                    value = user.routeName.ifEmpty { "Not assigned" }
                                )
                            }
                        }

                        item {
                            ActionButtonsSection(
                                onEditProfile = { showEditDialog = true },
                                onLogout = {
                                    FirebaseAuth.getInstance().signOut()
                                    navController.navigate(Routes.LoginScreen){
                                        popUpTo(Routes.HomeScreen){
                                            inclusive = true
                                        }
                                    }
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }

        // Dialogs
        if (showQRDialog) {
            QRCodeDialog(
                qrCodeBitmap = qrCodeBitmap,
                onDismiss = { showQRDialog = false }
            )
        }

        if (showEditDialog && userState.succcess != null) {
            EditProfileDialog(
                user = userState.succcess!!,
                onDismiss = { showEditDialog = false },
                onSave = { updatedUser ->
                    viewModel.updateUserData(updatedUser)
                    showEditDialog = false
                }
            )
        }

        if (showPhotoDialog && userState.succcess != null) {
            PhotoViewDialog(
                user = userState.succcess!!,
                onDismiss = { showPhotoDialog = false }
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = ProfileDarkTheme.cardBackground
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = ProfileDarkTheme.primary,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Loading your profile...",
                    fontSize = 16.sp,
                    color = ProfileDarkTheme.textSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = ProfileDarkTheme.cardBackground
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    tint = ProfileDarkTheme.accent,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Something went wrong",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ProfileDarkTheme.textPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    fontSize = 14.sp,
                    color = ProfileDarkTheme.textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ProfileDarkTheme.primary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Try Again",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileHeaderCard(
    user: UserModel,
    onPhotoClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = ProfileDarkTheme.cardBackground
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ProfileDarkTheme.primary.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        radius = 400f
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clickable { onPhotoClick() }
                ) {
                    if (user.profileImageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = user.profileImageUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .border(4.dp, ProfileDarkTheme.primary, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            ProfileDarkTheme.primary.copy(alpha = 0.2f),
                                            ProfileDarkTheme.primary.copy(alpha = 0.1f)
                                        )
                                    )
                                )
                                .border(4.dp, ProfileDarkTheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.name.firstOrNull()?.uppercase() ?: "U",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = ProfileDarkTheme.primary
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(40.dp),
                        shape = CircleShape,
                        color = ProfileDarkTheme.primary,
                        shadowElevation = 4.dp
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "View Photo",
                            tint = Color.White,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = user.name,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = ProfileDarkTheme.textPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = user.email,
                    fontSize = 16.sp,
                    color = ProfileDarkTheme.textSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = ProfileDarkTheme.success.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = ProfileDarkTheme.success,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "${user.totalPoints} Points",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ProfileDarkTheme.success
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = ProfileDarkTheme.cardBackground
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = ProfileDarkTheme.primary.copy(alpha = 0.15f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = ProfileDarkTheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ProfileDarkTheme.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            content()
        }
    }
}

@Composable
private fun ProfileInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ProfileDarkTheme.textSecondary,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                color = ProfileDarkTheme.textSecondary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = ProfileDarkTheme.textPrimary
            )
        }
    }
}

@Composable
private fun ActionButtonsSection(
    onEditProfile: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onEditProfile,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ProfileDarkTheme.primary
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp
            )
        ) {
            Icon(
                Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Edit Profile",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, ProfileDarkTheme.accent),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = ProfileDarkTheme.accent
            )
        ) {
            Icon(
                Icons.Default.ExitToApp,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Logout",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PhotoViewDialog(
    user: UserModel,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(20.dp)
            ) {
                // Header
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ProfileDarkTheme.cardBackground.copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        ) {
                            if (user.profileImageUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = user.profileImageUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(ProfileDarkTheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = user.name.firstOrNull()?.uppercase() ?: "U",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user.name,
                                color = ProfileDarkTheme.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Profile Photo",
                                color = ProfileDarkTheme.textSecondary,
                                fontSize = 12.sp
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = ProfileDarkTheme.textSecondary
                            )
                        }
                    }
                }

                // Photo
                Card(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.size(320.dp)
                ) {
                    if (user.profileImageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = user.profileImageUrl,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            ProfileDarkTheme.primary.copy(alpha = 0.3f),
                                            ProfileDarkTheme.cardBackground
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.name.firstOrNull()?.uppercase() ?: "U",
                                fontSize = 120.sp,
                                fontWeight = FontWeight.Bold,
                                color = ProfileDarkTheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QRCodeDialog(
    qrCodeBitmap: android.graphics.Bitmap?,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = ProfileDarkTheme.cardBackground
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.QrCode,
                        contentDescription = null,
                        tint = ProfileDarkTheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Your QR Code",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = ProfileDarkTheme.textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                qrCodeBitmap?.let { bitmap ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "User QR Code",
                            modifier = Modifier
                                .size(280.dp)
                                .padding(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Scan this code to share your profile information",
                    fontSize = 14.sp,
                    color = ProfileDarkTheme.textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { /* Handle share */ },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ProfileDarkTheme.primary
                        ),
                        border = BorderStroke(1.dp, ProfileDarkTheme.primary),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share")
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ProfileDarkTheme.primary
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileDialog(
    user: UserModel,
    onDismiss: () -> Unit,
    onSave: (UserModel) -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var email by remember { mutableStateOf(user.email) }
    var phoneNumber by remember { mutableStateOf(user.phoneNumber) }
    var address by remember { mutableStateOf(user.address) }
    var areaName by remember { mutableStateOf(user.areaName) }
    var routeName by remember { mutableStateOf(user.routeName) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = ProfileDarkTheme.cardBackground
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Profile",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = ProfileDarkTheme.textPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = ProfileDarkTheme.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name", color = ProfileDarkTheme.textSecondary) },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = ProfileDarkTheme.primary)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ProfileDarkTheme.primary,
                        unfocusedBorderColor = ProfileDarkTheme.divider,
                        focusedTextColor = ProfileDarkTheme.textPrimary,
                        unfocusedTextColor = ProfileDarkTheme.textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { },
                    label = { Text("Email", color = ProfileDarkTheme.textSecondary) },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null, tint = ProfileDarkTheme.textSecondary)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = ProfileDarkTheme.divider,
                        disabledTextColor = ProfileDarkTheme.textSecondary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number", color = ProfileDarkTheme.textSecondary) },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = ProfileDarkTheme.primary)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ProfileDarkTheme.primary,
                        unfocusedBorderColor = ProfileDarkTheme.divider,
                        focusedTextColor = ProfileDarkTheme.textPrimary,
                        unfocusedTextColor = ProfileDarkTheme.textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address", color = ProfileDarkTheme.textSecondary) },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = ProfileDarkTheme.primary)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ProfileDarkTheme.primary,
                        unfocusedBorderColor = ProfileDarkTheme.divider,
                        focusedTextColor = ProfileDarkTheme.textPrimary,
                        unfocusedTextColor = ProfileDarkTheme.textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = areaName,
                    onValueChange = { areaName = it },
                    label = { Text("Area Name", color = ProfileDarkTheme.textSecondary) },
                    leadingIcon = {
                        Icon(Icons.Default.Place, contentDescription = null, tint = ProfileDarkTheme.primary)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ProfileDarkTheme.primary,
                        unfocusedBorderColor = ProfileDarkTheme.divider,
                        focusedTextColor = ProfileDarkTheme.textPrimary,
                        unfocusedTextColor = ProfileDarkTheme.textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = routeName,
                    onValueChange = { routeName = it },
                    label = { Text("Route Name", color = ProfileDarkTheme.textSecondary) },
                    leadingIcon = {
                        Icon(Icons.Default.Route, contentDescription = null, tint = ProfileDarkTheme.primary)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ProfileDarkTheme.primary,
                        unfocusedBorderColor = ProfileDarkTheme.divider,
                        focusedTextColor = ProfileDarkTheme.textPrimary,
                        unfocusedTextColor = ProfileDarkTheme.textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ProfileDarkTheme.textSecondary
                        ),
                        border = BorderStroke(1.dp, ProfileDarkTheme.divider),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Cancel", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = {
                            val updatedUser = user.copy(
                                name = name,
                                phoneNumber = phoneNumber,
                                address = address,
                                areaName = areaName,
                                routeName = routeName
                            )
                            onSave(updatedUser)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ProfileDarkTheme.primary
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}