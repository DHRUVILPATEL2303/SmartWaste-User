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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Person
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

// Color Palette (kept from original)
private val GrayBackground = Color(0xFF121212)
private val GraySurface = Color(0x991E1E1E)
private val GraySurfaceVariant = Color(0xFF2C2C2C)
private val PrimaryGreen = Color(0xFF2ECC71)
private val PrimaryGreenVariant = Color(0xFF27AE60)
private val OnSurfaceText = Color(0xFFE0E0E0)
private val OnSurfaceTextVariant = Color(0xFFBDBDBD)
private val UserMessageBg = Color(0xFF25332C)
private val BotMessageBg = Color(0xAA2A2A2A)
private val GoldStart = Color(0xFFF7D060)
private val GoldEnd = Color(0xFFEAB543)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBotScreenUI(
    navController: NavHostController,
    viewModel: GeminiViewModel = hiltViewModel<GeminiViewModel>()
) {
    val chatState by viewModel.chatState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // State to hold the selected media URI before sending
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
    Scaffold(
        topBar = {
            Box(

            ) {
                TopAppBar(

                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Eco,
                                contentDescription = "Eco Icon",
                                tint = PrimaryGreen,
                                modifier = Modifier.size(32.dp)
                            )
                            Column {
                                Text(
                                    "SmartWaste Assistant",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = OnSurfaceText
                                )
                                Text(
                                    "Your AI Helper",
                                    fontSize = 12.sp,
                                    color = OnSurfaceTextVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }


                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(10, 59, 18, 200) // Slightly transparent
                    ),
                    modifier = Modifier.clip(RoundedCornerShape(32.dp)),
                )


            }
        },
        containerColor = Color.Transparent // Make scaffold background transparent
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.chatbackground), // Your background image
                contentDescription = "Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(radius = 20.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(bottom = 80.dp)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(top = 20.dp, bottom = 20.dp)
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = GraySurface
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Eco,
                contentDescription = "Eco Icon",
                modifier = Modifier.size(48.dp),
                tint = PrimaryGreen
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Welcome to SmartWaste! 🌱",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = OnSurfaceText
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Your AI companion for a greener planet. Share an image of waste or ask me anything about recycling!",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = OnSurfaceTextVariant,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun ChatMessageItem(message: Message) {
    val horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    val goldenBorder = if (!message.isFromUser) {
        BorderStroke(
            2.dp,
            Brush.linearGradient(colors = listOf(GoldStart, GoldEnd))
        )
    } else {
        null
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!message.isFromUser) {
            Icon(
                imageVector = Icons.Default.Eco,
                contentDescription = "Bot Avatar",
                tint = PrimaryGreen,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
        }

        Card(
            modifier = Modifier.weight(1f, fill = false),
            shape = RoundedCornerShape(
                topStart = if (message.isFromUser) 20.dp else 4.dp,
                topEnd = if (message.isFromUser) 4.dp else 20.dp,
                bottomStart = 20.dp,
                bottomEnd = 20.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isFromUser) UserMessageBg else BotMessageBg
            ),
            border = goldenBorder, // Apply golden border here
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = if (message.isFromUser) Alignment.End else Alignment.Start
            ) {
                if (message.imageUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(message.imageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Analyzed Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
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
                                setOnPreparedListener { it.isLooping = true; start() }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Text(
                    text = message.text,
                    color = OnSurfaceText,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )
            }
        }

        if (message.isFromUser) {
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "User Avatar",
                tint = OnSurfaceTextVariant,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
            )
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
        Icon(
            imageVector = Icons.Default.Eco,
            contentDescription = "Bot Avatar",
            tint = PrimaryGreen,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))

        Card(
            shape = RoundedCornerShape(
                topStart = 4.dp,
                topEnd = 20.dp,
                bottomStart = 20.dp,
                bottomEnd = 20.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = BotMessageBg
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(PrimaryGreen.copy(alpha = alpha))
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
    val canSend = text.trim().isNotEmpty() || selectedUri != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        AnimatedVisibility(
            visible = selectedUri != null,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            MediaPreview(uri = selectedUri, onClearMedia = onClearMedia)
        }

        Surface(
            color = GraySurface,
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onImageSelected) {
                    Icon(
                        Icons.Default.AddPhotoAlternate,
                        contentDescription = "Add Image",
                        tint = OnSurfaceTextVariant
                    )
                }

                IconButton(onClick = onVideoSelected) {
                    Icon(
                        Icons.Default.Videocam,
                        contentDescription = "Add Video",
                        tint = OnSurfaceTextVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                ) {
                    BasicTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(color = OnSurfaceText, fontSize = 16.sp),
                        cursorBrush = SolidColor(PrimaryGreen),
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
                            text = "Ask about waste...",
                            color = OnSurfaceTextVariant.copy(alpha = 0.7f),
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
                        .background(if (canSend) PrimaryGreen else GraySurfaceVariant)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Message",
                        tint = if (canSend) Color.White else OnSurfaceTextVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MediaPreview(uri: Uri?, onClearMedia: () -> Unit) {
    if (uri == null) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp, start = 48.dp, end = 48.dp)
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(containerColor = GraySurfaceVariant)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(uri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Selected Media Preview",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            IconButton(
                onClick = onClearMedia,
                modifier = Modifier
                    .padding(4.dp)
                    .clip(CircleShape)
                    .size(24.dp)
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear Media",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

fun getMimeType(context: Context, uri: Uri): String? {
    return context.contentResolver.getType(uri)
}