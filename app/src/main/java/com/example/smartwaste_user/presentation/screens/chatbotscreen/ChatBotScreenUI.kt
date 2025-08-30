package com.example.smartwaste_user.presentation.screens.chatbotscreen

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.smartwaste_user.R
import com.example.smartwaste_user.presentation.viewmodels.geminiviewmodel.GeminiViewModel
import com.example.smartwaste_user.presentation.viewmodels.geminiviewmodel.Message
import kotlinx.coroutines.launch

object ChatDarkTheme {
    val background = Color(0xFF0D1117)
    val surface = Color(0xFF161B22)
    val surfaceVariant = Color(0xFF21262D)
    val primary = Color(0xFF2ECC71)
    val primaryVariant = Color(0xFF27AE60)
    val secondary = Color(0xFF58A6FF)
    val accent = Color(0xFFE3B341)
    val textPrimary = Color(0xFFE6EDF3)
    val textSecondary = Color(0xFF8B949E)
    val userMessageBg = Color(0xFF1C2D24)
    val botMessageBg = Color(0xFF1C2128)
    val inputBackground = Color(0xFF21262D)
    val borderColor = Color(0xFF30363D)
    val successGreen = Color(0xFF56D364)
    val warningAmber = Color(0xFFFFA726)
    val goldStart = Color(0xFFFFD700)
    val goldEnd = Color(0xFFFFA500)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBotScreenUI(
    navController: NavHostController,
    viewModel: GeminiViewModel = hiltViewModel<GeminiViewModel>()
) {
    val chatState by viewModel.chatState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedUri = uri
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedUri = uri
    }

    LaunchedEffect(chatState.messages.size) {
        if (chatState.messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(chatState.messages.size - 1)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image with Blur
        Image(
            painter = painterResource(id = R.drawable.chatbackground),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = 15.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.7f),
                            Color.Black.copy(alpha = 0.8f)
                        )
                    )
                )
        )

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
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                ChatDarkTheme.primary.copy(alpha = 0.3f),
                                                ChatDarkTheme.primary.copy(alpha = 0.1f)
                                            )
                                        ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Eco,
                                    contentDescription = "AI Assistant",
                                    tint = ChatDarkTheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "SmartWaste AI",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = ChatDarkTheme.textPrimary
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(ChatDarkTheme.successGreen, CircleShape)
                                    )
                                    Text(
                                        text = "Online",
                                        fontSize = 12.sp,
                                        color = ChatDarkTheme.textSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        // Recycling Icon Badge
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    ChatDarkTheme.accent.copy(alpha = 0.15f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Recycling,
                                contentDescription = "Recycling",
                                tint = ChatDarkTheme.accent,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(24, 54, 18),
                        titleContentColor = ChatDarkTheme.textPrimary,
                        actionIconContentColor = ChatDarkTheme.textPrimary
                    ),
                   modifier = Modifier.clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                )
            },
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(bottom = 80.dp) // Increased bottom padding for bottom nav
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
                ) {
                    if (chatState.messages.isEmpty()) {
                        item {
                            WelcomeMessage()
                        }
                    }
                    items(chatState.messages) { message ->
                        ChatMessageItem(message = message)
                    }
                }

                AnimatedVisibility(
                    visible = chatState.isLoading,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    TypingIndicator()
                }

                MessageInput(
                    onSendMessage = { prompt, uri ->
                        viewModel.sendMessage(prompt, uri)
                        selectedUri = null
                    },
                    onImageSelected = { imagePickerLauncher.launch("image/*") },
                    onVideoSelected = { videoPickerLauncher.launch("video/*") },
                    selectedUri = selectedUri,
                    onClearMedia = { selectedUri = null }
                )
            }
        }
    }
}

@Composable
fun WelcomeMessage() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = ChatDarkTheme.surface.copy(alpha = 0.9f)
        ),
        border = BorderStroke(
            2.dp,
            Brush.linearGradient(
                colors = listOf(ChatDarkTheme.goldStart, ChatDarkTheme.goldEnd)
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ChatDarkTheme.primary.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        radius = 400f
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    ChatDarkTheme.primary.copy(alpha = 0.2f),
                                    ChatDarkTheme.primary.copy(alpha = 0.05f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Eco,
                        contentDescription = "SmartWaste AI",
                        modifier = Modifier.size(40.dp),
                        tint = ChatDarkTheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "🌱 Welcome to SmartWaste AI!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = ChatDarkTheme.textPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Your intelligent companion for sustainable waste management. I can help you:",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = ChatDarkTheme.textSecondary,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Feature Cards
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureCard(
                        icon = "📸",
                        title = "Analyze Waste",
                        description = "Upload photos to identify waste types"
                    )
                    FeatureCard(
                        icon = "♻️",
                        title = "Recycling Tips",
                        description = "Get personalized recycling guidance"
                    )
                    FeatureCard(
                        icon = "🌍",
                        title = "Eco Advice",
                        description = "Learn sustainable practices"
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureCard(
    icon: String,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = ChatDarkTheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = icon,
                fontSize = 24.sp
            )
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ChatDarkTheme.textPrimary
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = ChatDarkTheme.textSecondary
                )
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: Message) {
    val isFromUser = message.isFromUser
    val horizontalArrangement = if (isFromUser) Arrangement.End else Arrangement.Start

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isFromUser) {
            // AI Avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        ChatDarkTheme.primary.copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = "AI Assistant",
                    tint = ChatDarkTheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        Card(
            modifier = Modifier.weight(1f, fill = false),
            shape = RoundedCornerShape(
                topStart = if (isFromUser) 20.dp else 6.dp,
                topEnd = if (isFromUser) 6.dp else 20.dp,
                bottomStart = 20.dp,
                bottomEnd = 20.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isFromUser) ChatDarkTheme.userMessageBg else ChatDarkTheme.botMessageBg
            ),
            border = if (!isFromUser) {
                BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            ChatDarkTheme.goldStart.copy(alpha = 0.3f),
                            ChatDarkTheme.goldEnd.copy(alpha = 0.3f)
                        )
                    )
                )
            } else null,
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = if (isFromUser) Alignment.End else Alignment.Start
            ) {
                if (message.imageUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(message.imageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Shared Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (message.videoUri != null) {
                    AndroidView(
                        factory = { context ->
                            VideoView(context).apply {
                                setVideoURI(message.videoUri)
                                setOnPreparedListener {
                                    it.isLooping = true
                                    start()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (message.text.isNotEmpty()) {
                    Text(
                        text = message.text,
                        color = ChatDarkTheme.textPrimary,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        if (isFromUser) {
            Spacer(modifier = Modifier.width(12.dp))
            // User Avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        ChatDarkTheme.secondary.copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "You",
                    tint = ChatDarkTheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    ChatDarkTheme.primary.copy(alpha = 0.15f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Eco,
                contentDescription = "AI Typing",
                tint = ChatDarkTheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))

        Card(
            shape = RoundedCornerShape(
                topStart = 6.dp,
                topEnd = 20.dp,
                bottomStart = 20.dp,
                bottomEnd = 20.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = ChatDarkTheme.botMessageBg
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI is thinking",
                    color = ChatDarkTheme.textSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                repeat(3) { index ->
                    val infiniteTransition = rememberInfiniteTransition(label = "typing-dot")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 800, delayMillis = index * 200),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha"
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(ChatDarkTheme.primary.copy(alpha = alpha))
                    )
                }
            }
        }
    }
}

@Composable
fun MessageInput(
    onSendMessage: (String, Uri?) -> Unit,
    onImageSelected: () -> Unit,
    onVideoSelected: () -> Unit,
    selectedUri: Uri?,
    onClearMedia: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    var showUploadMenu by remember { mutableStateOf(false) }
    val canSend = text.trim().isNotEmpty() || selectedUri != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(16.dp)
    ) {
        AnimatedVisibility(
            visible = selectedUri != null,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            MediaPreview(uri = selectedUri, onClearMedia = onClearMedia)
        }

        AnimatedVisibility(
            visible = showUploadMenu,
            enter = fadeIn() + slideInVertically(
                initialOffsetY = { it / 4 }
            ),
            exit = fadeOut() + slideOutVertically(
                targetOffsetY = { it / 4 }
            )
        ) {
            UploadOptionsDropUp(
                onImageSelected = {
                    onImageSelected()
                    showUploadMenu = false
                },
                onVideoSelected = {
                    onVideoSelected()
                    showUploadMenu = false
                },
                onDismiss = { showUploadMenu = false }
            )
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = ChatDarkTheme.inputBackground
            ),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, ChatDarkTheme.borderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = { showUploadMenu = !showUploadMenu },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            ChatDarkTheme.primary.copy(alpha = 0.1f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (showUploadMenu) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Upload Media",
                        tint = ChatDarkTheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    BasicTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            color = ChatDarkTheme.textPrimary,
                            fontSize = 16.sp
                        ),
                        cursorBrush = SolidColor(ChatDarkTheme.primary),
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (canSend) {
                                onSendMessage(text.trim(), selectedUri)
                                text = ""
                            }
                        })
                    )
                    if (text.isEmpty()) {
                        Text(
                            text = "Ask about waste management...",
                            color = ChatDarkTheme.textSecondary.copy(alpha = 0.7f),
                            fontSize = 16.sp
                        )
                    }
                }

                IconButton(
                    onClick = {
                        if (canSend) {
                            onSendMessage(text.trim(), selectedUri)
                            text = ""
                        }
                    },
                    enabled = canSend,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (canSend) {
                                Brush.linearGradient(
                                    colors = listOf(ChatDarkTheme.primary, ChatDarkTheme.primaryVariant)
                                )
                            } else {
                                SolidColor(ChatDarkTheme.surfaceVariant)
                            }
                        )
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Message",
                        tint = if (canSend) Color.White else ChatDarkTheme.textSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun UploadOptionsDropUp(
    onImageSelected: () -> Unit,
    onVideoSelected: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .clickable { onDismiss() }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            UploadOption(
                icon = Icons.Default.AddPhotoAlternate,
                label = "Image",
                backgroundColor = ChatDarkTheme.accent.copy(alpha = 0.2f),
                iconTint = ChatDarkTheme.accent,
                onClick = onImageSelected
            )

            UploadOption(
                icon = Icons.Default.Videocam,
                label = "Video",
                backgroundColor = ChatDarkTheme.warningAmber.copy(alpha = 0.2f),
                iconTint = ChatDarkTheme.warningAmber,
                onClick = onVideoSelected
            )
        }
    }
}

@Composable
private fun UploadOption(
    icon: ImageVector,
    label: String,
    backgroundColor: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(backgroundColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            color = ChatDarkTheme.textPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun MediaPreview(uri: Uri?, onClearMedia: () -> Unit) {
    if (uri == null) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ChatDarkTheme.borderColor),
        colors = CardDefaults.cardColors(
            containerColor = ChatDarkTheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopEnd
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(uri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Media Preview",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            IconButton(
                onClick = onClearMedia,
                modifier = Modifier
                    .padding(8.dp)
                    .size(32.dp)
                    .background(
                        Color.Black.copy(alpha = 0.7f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove Media",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

fun getMimeType(context: Context, uri: Uri): String? {
    return context.contentResolver.getType(uri)
}