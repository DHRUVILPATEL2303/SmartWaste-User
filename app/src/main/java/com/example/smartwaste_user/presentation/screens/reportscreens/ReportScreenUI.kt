package com.example.smartwaste_user.presentation.screens.reportscreens

import android.net.Uri
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.smartwaste_user.data.models.ReportModel
import com.example.smartwaste_user.presentation.navigation.Routes
import com.example.smartwaste_user.presentation.viewmodels.ReportViewModel
import java.text.SimpleDateFormat
import java.util.Locale


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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Reports & Services",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            actions = {
                IconButton(onClick = { viewModel.getAllReports() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
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
                    onExtraServiceClick = { /* Navigate to extra service screen */ }
                )
            }

            item {
                Text(
                    text = "Recent Reports",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                )
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            QuickActionButton(icon = Icons.Default.Add, label = "Make Report", color = MaterialTheme.colorScheme.primary, onClick = onMakeReportClick)
            QuickActionButton(icon = Icons.Default.Warning, label = "Complaint", color = Color(0xFFFF6B35), onClick = onMakeComplaintClick)
            QuickActionButton(icon = Icons.Default.Build, label = "Extra Service", color = Color(0xFF4CAF50), onClick = onExtraServiceClick)
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
                .size(56.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun ReportItem(
    report: ReportModel,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF7FBC81))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "Report in ${report.areaName}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Report",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (report.description.isNotBlank()) {
                Text(
                    text = report.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusChip(status = report.status)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Date",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatReportDate(report.reportDate),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

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

            if (report.attachments.isNotEmpty()) {
                Column {
                    Text(
                        text = "Attachments",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(report.attachments) { attachmentUriString ->
                            AttachmentThumbnail(attachmentUriString = attachmentUriString)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label:",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(70.dp)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun AttachmentThumbnail(attachmentUriString: String) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = Uri.parse(attachmentUriString),
            contentDescription = "Attachment Thumbnail",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}


@Composable
private fun StatusChip(status: String) {
    val (backgroundColor, textColor) = when (status.lowercase()) {
        "pending" -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        "approved", "completed" -> Color(0xFFD1E7DD) to Color(0xFF0F5132)
        "rejected" -> Color(0xFFF8D7DA) to Color(0xFF721C24)
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        Text(
            text = "Loading reports...",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun ErrorContent(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(imageVector = Icons.Default.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
        Text(text = "Error: $error", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Button(onClick = onRetry) { Text("Retry") }
    }
}

@Composable
private fun EmptyContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.AssignmentTurnedIn,
            contentDescription = "No Reports",
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.size(64.dp)
        )
        Text(
            text = "No reports found",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = "Create your first report using the actions above",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
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