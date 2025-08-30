package com.example.smartwaste_user.presentation.screens.editorialscreen

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.smartwaste_user.data.models.EdiorialModel
import com.example.smartwaste_user.presentation.viewmodels.editorialviewmodel.EditorialViewModel
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.delay

object EditorialDarkTheme {
    val background = Color(0xFF0D1117)
    val surface = Color(0xFF161B22)
    val surfaceVariant = Color(0xFF21262D)
    val primary = Color(0xFFFF4444)
    val secondary = Color(0xFF79C0FF)
    val accent = Color(0xFFE3B341)
    val success = Color(0xFF56D364)
    val textPrimary = Color(0xFFE6EDF3)
    val textSecondary = Color(0xFF8B949E)
    val cardBackground = Color(0xFF1C2128)
    val cardHover = Color(0xFF262C36)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorialScreenUI(
    navController: NavController,
    viewModel: EditorialViewModel = hiltViewModel()
) {
    val editorialState by viewModel.getAllEditorialState.collectAsState()
    var selectedEditorial by remember { mutableStateOf<EdiorialModel?>(null) }

    val context = LocalContext.current
    val activity = context as? Activity

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                }
                Lifecycle.Event.ON_RESUME -> {
                }
                Lifecycle.Event.ON_DESTROY -> {
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.getAllEditorials()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    DisposableEffect(Unit) {
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            activity?.window?.clearFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }

    if (selectedEditorial != null) {
        NormalVideoPlayer(
            editorial = selectedEditorial!!,
            onDismiss = {
                selectedEditorial = null
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        )
    } else {
        Scaffold(
            topBar = {
                EditorialTopAppBar(
                    onBackClick = {
                        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        navController.navigateUp()
                    },
                    editorialCount = editorialState.success?.size ?: 0
                )
            },
            containerColor = EditorialDarkTheme.background,
            contentColor = EditorialDarkTheme.textPrimary
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                EditorialDarkTheme.background,
                                EditorialDarkTheme.surface
                            )
                        )
                    )
            ) {
                when {
                    editorialState.isLoading -> {
                        LoadingSection()
                    }
                    editorialState.error.isNotEmpty() -> {
                        ErrorSection(
                            error = editorialState.error,
                            onRetry = { viewModel.getAllEditorials() }
                        )
                    }
                    editorialState.success.isNullOrEmpty() -> {
                        EmptyEditorialsSection()
                    }
                    else -> {
                        val editorials = editorialState.success
                        EditorialsContent(
                            editorials = editorials.orEmpty(),
                            onWatchVideo = { editorial ->
                                selectedEditorial = editorial
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorialTopAppBar(
    onBackClick: () -> Unit,
    editorialCount: Int
) {
    TopAppBar(
        title = {
            Column(
                modifier = Modifier.padding(start = 30.dp)
            ) {
                Text(
                    text = "Editorial Videos",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = EditorialDarkTheme.textPrimary
                )
                if (editorialCount > 0) {
                    Text(
                        text = "$editorialCount videos available",
                        fontSize = 12.sp,
                        color = EditorialDarkTheme.textSecondary
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .padding(start = 20.dp)
                    .size(30.dp)
                    .background(
                        color = EditorialDarkTheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = EditorialDarkTheme.primary
                )
            }
        },
        actions = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = EditorialDarkTheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VideoLibrary,
                    contentDescription = "Videos",
                    tint = EditorialDarkTheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = EditorialDarkTheme.cardBackground,
            titleContentColor = EditorialDarkTheme.textPrimary,
            navigationIconContentColor = EditorialDarkTheme.primary,
            actionIconContentColor = EditorialDarkTheme.primary
        )
    )
}

@Composable
private fun EditorialsContent(
    editorials: List<EdiorialModel>,
    onWatchVideo: (EdiorialModel) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HeaderCard(totalVideos = editorials.size)
        }

        items(
            items = editorials,
            key = { editorial -> editorial.hashCode() }
        ) { editorial ->
            EditorialCard(
                editorial = editorial,
                onWatchVideo = { onWatchVideo(editorial) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HeaderCard(totalVideos: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = EditorialDarkTheme.cardBackground
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            EditorialDarkTheme.primary.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        radius = 400f
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    EditorialDarkTheme.primary.copy(alpha = 0.2f),
                                    EditorialDarkTheme.primary.copy(alpha = 0.05f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VideoLibrary,
                        contentDescription = "Videos",
                        tint = EditorialDarkTheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Watch & Learn",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = EditorialDarkTheme.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Discover $totalVideos educational videos covering various topics",
                        fontSize = 14.sp,
                        color = EditorialDarkTheme.textSecondary,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingSection() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = EditorialDarkTheme.cardBackground
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = EditorialDarkTheme.primary,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Loading Editorial Videos",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = EditorialDarkTheme.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Please wait while we fetch the latest content...",
                    fontSize = 14.sp,
                    color = EditorialDarkTheme.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ErrorSection(
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
                containerColor = EditorialDarkTheme.cardBackground
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "⚠️",
                    fontSize = 64.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Unable to Load Videos",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = EditorialDarkTheme.textPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = error,
                    fontSize = 14.sp,
                    color = EditorialDarkTheme.textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EditorialDarkTheme.primary
                    ),
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Try Again",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyEditorialsSection() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = EditorialDarkTheme.cardBackground
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎬",
                    fontSize = 80.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "No Editorial Videos",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = EditorialDarkTheme.textPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "We're working on adding new educational content. Check back soon for exciting videos!",
                    fontSize = 15.sp,
                    color = EditorialDarkTheme.textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
private fun EditorialCard(
    editorial: EdiorialModel,
    onWatchVideo: () -> Unit
) {
    var imageLoadError by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onWatchVideo() },
        colors = CardDefaults.cardColors(
            containerColor = EditorialDarkTheme.cardBackground
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                if (!imageLoadError && editorial.thumbnail.isNotBlank()) {
                    AsyncImage(
                        model = editorial.thumbnail,
                        contentDescription = "Video Thumbnail for ${editorial.title}",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                        contentScale = ContentScale.Crop,
                        onError = { imageLoadError = true }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        EditorialDarkTheme.primary.copy(alpha = 0.3f),
                                        EditorialDarkTheme.surfaceVariant
                                    )
                                )
                            )
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = "Video",
                            tint = EditorialDarkTheme.textSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.6f)
                                ),
                                startY = 100f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .align(Alignment.Center)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    EditorialDarkTheme.primary,
                                    EditorialDarkTheme.primary.copy(alpha = 0.8f)
                                )
                            ),
                            shape = CircleShape
                        )
                        .clickable { onWatchVideo() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Video",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = EditorialDarkTheme.primary.copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "EDITORIAL",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Card(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.8f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Video",
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = editorial.title.takeIf { it.isNotBlank() } ?: "Untitled Video",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = EditorialDarkTheme.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tap anywhere to watch",
                        fontSize = 13.sp,
                        color = EditorialDarkTheme.textSecondary
                    )

                    Button(
                        onClick = onWatchVideo,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EditorialDarkTheme.primary
                        ),
                        shape = RoundedCornerShape(25.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Watch",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NormalVideoPlayer(
    editorial: EdiorialModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var youTubePlayer by remember { mutableStateOf<YouTubePlayer?>(null) }
    var youtubePlayerView by remember { mutableStateOf<YouTubePlayerView?>(null) }
    var isDoubleSpeed by remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current

    val originalOrientation = remember {
        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    BackHandler {
        activity?.requestedOrientation = originalOrientation
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDismiss()
    }

    DisposableEffect(Unit) {
        onDispose {
            youtubePlayerView?.release()
            activity?.requestedOrientation = originalOrientation
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(top = 50.dp)
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    onLongClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        isDoubleSpeed = !isDoubleSpeed

                    },
                    onClick = {
                    }
                ),
            factory = { ctx ->
                YouTubePlayerView(ctx).apply {
                    youtubePlayerView = this
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    val options = IFramePlayerOptions.Builder()
                        .controls(1) // Always show controls
                        .fullscreen(0) // Disable fullscreen
                        .rel(0)
                        .autoplay(1)
                        .build()

                    enableAutomaticInitialization = false
                    initialize(object : AbstractYouTubePlayerListener() {
                        override fun onReady(player: YouTubePlayer) {
                            youTubePlayer = player
                            val videoId = getYoutubeVideoId(editorial.vidoeUrl)
                            if (videoId.isNotEmpty()) {
                                player.loadVideo(videoId, 0f)
                            }


                        }
                    }, options)

                    addFullscreenListener(object : FullscreenListener {
                        override fun onEnterFullscreen(
                            fullscreenView: View,
                            exitFullscreen: () -> Unit
                        ) {
                            exitFullscreen.invoke()
                        }

                        override fun onExitFullscreen() {
                        }
                    })
                }
            }
        )

        IconButton(
            onClick = {
                activity?.requestedOrientation = originalOrientation
                activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                onDismiss()
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(48.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        AnimatedVisibility(
            visible = isDoubleSpeed,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = EditorialDarkTheme.accent.copy(alpha = 0.9f)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Speed",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "2x",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.8f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = editorial.title.ifBlank { "Editorial Video" },
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Long press anywhere on video for 2x speed",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

fun getYoutubeVideoId(url: String): String {
    if (url.isBlank()) return ""

    val patterns = listOf(
        // Standard YouTube URLs
        Regex("(?:youtube\\.com/watch\\?v=|youtu\\.be/)([\\w-]{11})"),
        // Embedded URLs
        Regex("youtube\\.com/embed/([\\w-]{11})"),
        // YouTube shorts
        Regex("youtube\\.com/shorts/([\\w-]{11})"),
        // Mobile URLs
        Regex("m\\.youtube\\.com/watch\\?v=([\\w-]{11})"),
        // Other variations
        Regex("[&?]v=([\\w-]{11})")
    )

    for (pattern in patterns) {
        val match = pattern.find(url)
        if (match != null) {
            val videoId = match.groups[1]?.value
            if (videoId != null && videoId.length == 11) {
                return videoId
            }
        }
    }
    return ""
}