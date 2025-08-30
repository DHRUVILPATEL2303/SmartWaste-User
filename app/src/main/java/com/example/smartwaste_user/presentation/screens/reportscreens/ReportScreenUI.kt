package com.example.smartwaste_user.presentation.screens.reportscreens

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.smartwaste_user.data.models.ReportModel
import com.example.smartwaste_user.presentation.navigation.Routes
import com.example.smartwaste_user.presentation.viewmodels.ReportViewModel

import java.text.SimpleDateFormat
import java.util.Locale

// Dark theme colors
object ReportDarkTheme {
    val background = Color(0xFF0D1117)
    val surface = Color(0xFF161B22)
    val surfaceVariant = Color(0xFF21262D)
    val primary = Color(0xFF2ECC71)
    val primaryVariant = Color(0xFF27AE60)
    val secondary = Color(0xFF58A6FF)
    val accent = Color(0xFFE3B341)
    val error = Color(0xFFFF6B6B)
    val success = Color(0xFF56D364)
    val warning = Color(0xFFFFA726)
    val textPrimary = Color(0xFFE6EDF3)
    val textSecondary = Color(0xFF8B949E)
    val cardBackground = Color(0xFF1C2128)
    val cardHover = Color(0xFF262C36)
    val borderColor = Color(0xFF30363D)
    val gradientStart = Color(0xFF1E40AF)
    val gradientEnd = Color(0xFF3B82F6)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreenUI(
    viewModel: ReportViewModel = hiltViewModel<ReportViewModel>(),
    navController: NavController
) {
    val reportState by viewModel.reportState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.getAllReports()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        ReportDarkTheme.background,
                        ReportDarkTheme.surface
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Dark TopAppBar
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
                                    ReportDarkTheme.primary.copy(alpha = 0.15f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Assignment,
                                contentDescription = "Reports",
                                tint = ReportDarkTheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Reports & Services",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = ReportDarkTheme.textPrimary
                            )
                            Text(
                                text = "Manage your reports",
                                fontSize = 12.sp,
                                color = ReportDarkTheme.textSecondary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ReportDarkTheme.cardBackground.copy(alpha = 0.95f)
                ),
                actions = {
                    IconButton(
                        onClick = { viewModel.getAllReports() },
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                ReportDarkTheme.secondary.copy(alpha = 0.15f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = ReportDarkTheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    QuickActionButtons(
                        onMakeReportClick = {
                            navController.navigate(Routes.MakeReportScreen)
                        },
                        onMakeComplaintClick = { /* Navigate to make complaint screen */ },
                        onExtraServiceClick = {
                            navController.navigate(Routes.RequestExtraServiceScreen)
                        }
                    )
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            tint = ReportDarkTheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Recent Reports",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = ReportDarkTheme.textPrimary
                        )
                    }
                }

                when {
                    reportState.isLoading -> {
                        item { LoadingContent() }
                    }
                    reportState.error.isNotEmpty() -> {
                        item { ErrorContent(error = reportState.error, onRetry = { viewModel.getAllReports() }) }
                    }
                    reportState.success?.isEmpty() == true -> {
                        item { EmptyContent() }
                    }
                    else -> {
                        items(reportState.success ?: emptyList(), key = { it.reportId }) { report ->
                            ReportItem(
                                report = report,
                                onClick = { /* Handle click if needed */ },
                                onDeleteClick = { viewModel.deleteReport(report.reportId) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionButtons(
    onMakeReportClick: () -> Unit,
    onMakeComplaintClick: () -> Unit,
    onExtraServiceClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = ReportDarkTheme.cardBackground
        ),
        border = BorderStroke(1.dp, ReportDarkTheme.borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = ReportDarkTheme.textPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionButton(
                    icon = Icons.Default.Add,
                    label = "Make Report",
                    color = ReportDarkTheme.primary,
                    onClick = onMakeReportClick
                )
                QuickActionButton(
                    icon = Icons.Default.Warning,
                    label = "Complaint",
                    color = ReportDarkTheme.warning,
                    onClick = onMakeComplaintClick
                )
                QuickActionButton(
                    icon = Icons.Default.Build,
                    label = "Extra Service",
                    color = ReportDarkTheme.success,
                    onClick = onExtraServiceClick
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = 0.2f),
                            color.copy(alpha = 0.05f)
                        )
                    ),
                    shape = CircleShape
                )
                .border(1.dp, color.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = ReportDarkTheme.textPrimary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ReportItem(
    report: ReportModel,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ReportDarkTheme.cardBackground
        ),
        border = BorderStroke(1.dp, ReportDarkTheme.borderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ReportDarkTheme.primary.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        radius = 300f
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with title and delete button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = ReportDarkTheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Report in ${report.areaName}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ReportDarkTheme.textPrimary,
                            modifier = Modifier.weight(1f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                ReportDarkTheme.error.copy(alpha = 0.1f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Report",
                            tint = ReportDarkTheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Description
                if (report.description.isNotBlank()) {
                    Text(
                        text = report.description,
                        fontSize = 14.sp,
                        color = ReportDarkTheme.textSecondary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp
                    )
                }

                // Status and Date row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatusChip(status = report.status)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Date",
                            modifier = Modifier.size(14.dp),
                            tint = ReportDarkTheme.textSecondary
                        )
                        Text(
                            text = formatReportDate(report.reportDate),
                            fontSize = 12.sp,
                            color = ReportDarkTheme.textSecondary
                        )
                    }
                }

                HorizontalDivider(color = ReportDarkTheme.borderColor.copy(alpha = 0.5f))

                // Info section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoRow(icon = Icons.Default.Route, label = "Route ID", value = report.routeId)

                    val against = buildList {
                        if (report.againstDriverId?.isNotBlank() == true) add("Driver")
                        if (report.againstCollectorId?.isNotBlank() == true) add("Collector")
                    }.joinToString(", ")

                    if (against.isNotEmpty()) {
                        InfoRow(icon = Icons.Default.Group, label = "Against", value = against)
                    }
                }

                // Attachments
                if (report.attachments.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Attachment,
                                contentDescription = null,
                                tint = ReportDarkTheme.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Attachments (${report.attachments.size})",
                                style = MaterialTheme.typography.labelMedium,
                                color = ReportDarkTheme.textSecondary
                            )
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(report.attachments) { attachmentUriString ->
                                AttachmentThumbnail(attachmentUriString = attachmentUriString)
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(16.dp),
            tint = ReportDarkTheme.secondary
        )
        Text(
            text = "$label:",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = ReportDarkTheme.textSecondary,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = ReportDarkTheme.textPrimary
        )
    }
}

@Composable
fun AttachmentThumbnail(attachmentUriString: String) {
    Card(
        modifier = Modifier.size(72.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = ReportDarkTheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, ReportDarkTheme.borderColor)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = Uri.parse(attachmentUriString),
                contentDescription = "Attachment Thumbnail",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Overlay for better visibility
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.3f)
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = ReportDarkTheme.cardBackground
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            border = BorderStroke(1.dp, ReportDarkTheme.borderColor)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    color = ReportDarkTheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "Loading reports...",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = ReportDarkTheme.textPrimary
                )
                Text(
                    text = "Please wait while we fetch your reports",
                    fontSize = 14.sp,
                    color = ReportDarkTheme.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ErrorContent(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = ReportDarkTheme.cardBackground
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            border = BorderStroke(1.dp, ReportDarkTheme.error.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(ReportDarkTheme.error.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        tint = ReportDarkTheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "Something went wrong",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ReportDarkTheme.textPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = error,
                    fontSize = 14.sp,
                    color = ReportDarkTheme.textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Button(
                    onClick = onRetry,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ReportDarkTheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Try Again",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = ReportDarkTheme.cardBackground
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            border = BorderStroke(1.dp, ReportDarkTheme.borderColor)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(ReportDarkTheme.surfaceVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AssignmentTurnedIn,
                        contentDescription = "No Reports",
                        tint = ReportDarkTheme.textSecondary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Text(
                    text = "No reports yet",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ReportDarkTheme.textPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Create your first report using the quick actions above to get started",
                    fontSize = 14.sp,
                    color = ReportDarkTheme.textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

private fun formatReportDate(dateString: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        formatter.format(parser.parse(dateString)!!)
    } catch (e: Exception) {
        dateString
    }
}