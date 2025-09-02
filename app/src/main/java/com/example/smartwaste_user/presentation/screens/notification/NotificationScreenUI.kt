package com.example.smartwaste_user.presentation.screens.notification

import android.content.Intent
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.smartwaste_user.data.models.HolidayModel
import com.example.smartwaste_user.presentation.navigation.Routes
import com.example.smartwaste_user.presentation.viewmodels.HolidayViewModel
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DarkTheme {
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
}

@Composable
fun NotificationScreenUI(
    navController: NavHostController,
    holidayViewModel: HolidayViewModel = hiltViewModel()
) {
    val holidayState by holidayViewModel.holidayState.collectAsState()

    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    val context = LocalContext.current

    val textToSpeech : TextToSpeech?=null
    DisposableEffect(Unit) {
        val textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale("gu", "IN"))
                Log.d("TTS", "TTS init status=$status, setLanguage(Gujarati) result=$result")

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Gujarati language is not supported or missing data.")
                    val installIntent = Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(installIntent)

                } else {
                    val gujaratiVoices = textToSpeech?.voices?.filter { it.locale == Locale("gu", "IN") }

                    if (gujaratiVoices != null) {
                        if (gujaratiVoices.isEmpty()) {
                            Log.w("TTS", "No Gujarati voices were found. Prompting user to install.")
                            val installIntent = Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(installIntent)
                        } else {
                            Log.d("TTS", "----------- AVAILABLE GUJARATI VOICES -----------")
                            gujaratiVoices.forEach { voice ->
                                Log.d("TTS", "Voice Name: ${voice.name}, Features: ${voice.features}")
                            }
                            Log.d("TTS", "--------------------------------------------------")

                            val maleVoice = gujaratiVoices?.firstOrNull { voice ->
                                voice.name.contains("male", ignoreCase = true) ||
                                        voice.features.contains("genderMale")
                            }

                            if (maleVoice != null) {
                                textToSpeech.voice = maleVoice
                                Log.d("TTS", "SUCCESS: Found and set a male Gujarati voice: ${maleVoice.name}")
                            } else {
                                Log.w("TTS", "Male Gujarati voice not found. Redirecting user to TTS settings to install it.")
                                val installIntent = Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                if (installIntent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(installIntent)
                                } else {
                                    Log.e("TTS", "Could not resolve activity for ACTION_INSTALL_TTS_DATA")
                                }
                            }
                        }
                    }
                }
            } else {
                Log.e("TTS", "TTS Initialization failed with status=$status")
            }
        }
        tts = textToSpeech
        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    LaunchedEffect(Unit) {
        holidayViewModel.getAllHolidays()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        DarkTheme.background,
                        DarkTheme.surface
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { HeaderSection() }
            item { ActionCardsSection(navController) }

            when {
                holidayState.isLoading -> item { LoadingSection() }
                holidayState.error.isNotEmpty() -> item { ErrorSection(error = holidayState.error) }
                holidayState.success != null && holidayState.success!!.isNotEmpty() -> {
                    val allHolidays = getAllHolidaysSorted(holidayState.success!!)
                    item {
                        SectionHeader(
                            title = "Upcoming Holidays",
                            subtitle = "${allHolidays.size} holidays scheduled"
                        )
                    }
                    if (allHolidays.isEmpty()) {
                        item { EmptyHolidaysSection() }
                    } else {
                        items(allHolidays) { holiday ->
                            EnhancedHolidayCard(holiday = holiday, tts = tts)
                        }
                    }
                }
                else -> item { EmptyHolidaysSection() }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun HeaderSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkTheme.cardBackground),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(DarkTheme.primary.copy(alpha = 0.1f), Color.Transparent),
                        radius = 300f
                    )
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    DarkTheme.primary.copy(alpha = 0.2f),
                                    DarkTheme.primary.copy(alpha = 0.05f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = DarkTheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Smart Waste Notifications",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkTheme.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Stay updated with waste collection schedules, holidays, and important announcements",
                    fontSize = 15.sp,
                    color = DarkTheme.textSecondary,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ActionCardsSection(navController: NavHostController) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ActionCard(
            title = "Editorial Content",
            subtitle = "View waste management guides and tips",
            icon = Icons.Default.OndemandVideo,
            iconColor = DarkTheme.accent,
            onClick = { navController.navigate(Routes.EditorialScreen) }
        )
        ActionCard(
            title = "Holiday Calendar",
            subtitle = "Check collection schedule during holidays",
            icon = Icons.Default.CalendarToday,
            iconColor = DarkTheme.success,
            onClick = { /* Handle holiday calendar navigation */ }
        )
    }
}

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = DarkTheme.cardBackground),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color = iconColor.copy(alpha = 0.15f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkTheme.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = subtitle, fontSize = 14.sp, color = DarkTheme.textSecondary)
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = DarkTheme.textSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = DarkTheme.textPrimary
            )
            Text(text = subtitle, fontSize = 14.sp, color = DarkTheme.textSecondary)
        }
    }
}

@Composable
private fun LoadingSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkTheme.cardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp), contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    color = DarkTheme.primary,
                    modifier = Modifier.size(40.dp),
                    strokeWidth = 3.dp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Loading holidays...", color = DarkTheme.textSecondary, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun ErrorSection(error: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkTheme.accent.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "⚠️", fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Error Loading Holidays",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkTheme.accent
                )
                Text(text = error, fontSize = 14.sp, color = DarkTheme.textSecondary)
            }
        }
    }
}

@Composable
private fun EmptyHolidaysSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkTheme.cardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🗓️", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Upcoming Holidays",
                color = DarkTheme.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "All holidays are in the past or no holidays are currently scheduled",
                color = DarkTheme.textSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun EnhancedHolidayCard(
    holiday: HolidayModel,
    modifier: Modifier = Modifier,
    tts: TextToSpeech? = null
) {
    var translatedName by remember { mutableStateOf(holiday.holidayName) }
    var translatedDate by remember { mutableStateOf(formatHolidayDate(holiday.holidayDate)) }

    LaunchedEffect(key1 = holiday) {
        translateText(
            text = holiday.holidayName,
            targetLang = TranslateLanguage.GUJARATI,
            onResult = { translated -> translatedName = translated },
            onError = { e -> Log.e("Translate", "Failed to translate name: ${e.message}") }
        )
        translateText(
            text = formatHolidayDate(holiday.holidayDate),
            targetLang = TranslateLanguage.GUJARATI,
            onResult = { translated -> translatedDate = translated },
            onError = { e -> Log.e("Translate", "Failed to translate date: ${e.message}") }
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkTheme.cardBackground),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                DarkTheme.primary.copy(alpha = 0.2f),
                                DarkTheme.primary.copy(alpha = 0.05f)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Holiday",
                    tint = DarkTheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Display the translated name
                Text(
                    text = translatedName,
                    color = DarkTheme.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Date",
                        tint = DarkTheme.textSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    // Display the translated date
                    Text(
                        text = translatedDate,
                        color = DarkTheme.textSecondary,
                        fontSize = 14.sp
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.Speaker,
                contentDescription = "Speak holiday",
                tint = DarkTheme.accent,
                modifier = Modifier
                    .size(28.dp)
                    .clickable {
                        val speechText = "$translatedName ની રજા $translatedDate ના રોજ છે."
                        Log.d("TTS", "Speaking (Gujarati): $speechText")
                        tts?.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, null)
                    }
            )
        }
    }
}

fun getAllHolidaysSorted(holidays: List<HolidayModel>): List<HolidayModel> {
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    val currentDate = Date()
    return holidays
        .filter { holiday ->
            try {
                val holidayDate = dateFormat.parse(holiday.holidayDate)
                if (holidayDate != null) {
                    val isToday = dateFormat.format(holidayDate) == dateFormat.format(currentDate)
                    val isFuture = holidayDate.after(currentDate)
                    isToday || isFuture
                } else {
                    false
                }
            } catch (e: Exception) {
                Log.e("holidays_debug", "Error parsing date for ${holiday.holidayName}: ${e.message}")
                false
            }
        }
        .sortedBy { holiday ->
            try {
                dateFormat.parse(holiday.holidayDate) ?: Date(0)
            } catch (e: Exception) {
                Date(0)
            }
        }
}

fun formatHolidayDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        Log.e("date_format", "Error formatting date: $dateString", e)
        dateString
    }
}

fun translateText(
    text: String,
    targetLang: String,
    onResult: (String) -> Unit,
    onError: (Exception) -> Unit
) {
    val options = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.ENGLISH)
        .setTargetLanguage(targetLang)
        .build()

    val translator = Translation.getClient(options)

    val conditions = DownloadConditions.Builder()
        .requireWifi()
        .build()

    translator.downloadModelIfNeeded(conditions)
        .addOnSuccessListener {
            translator.translate(text)
                .addOnSuccessListener(onResult)
                .addOnFailureListener(onError)
        }
        .addOnFailureListener(onError)
}


fun translateTextUsingGoogleTranslateApi(
    apiKey: String,
    text: String,
    targetLang: String,
    onSuccess: (String) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val url = "https://translation.googleapis.com/language/translate/v2"
    val requestBody = FormBody.Builder()
        .add("q", text)
        .add("target", targetLang)
        .add("format", "text")
        .add("key", apiKey)
        .build()

    val request = Request.Builder()
        .url(url)
        .post(requestBody)
        .build()

    OkHttpClient().newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            onFailure(e)
        }

        override fun onResponse(call: Call, response: Response) {
            response.use {
                if (!it.isSuccessful) return onFailure(Exception("HTTP error ${it.code}"))
                val json = JSONObject(it.body!!.string())
                val translated = json
                    .getJSONObject("data")
                    .getJSONArray("translations")
                    .getJSONObject(0)
                    .getString("translatedText")
                onSuccess(translated)
            }
        }
    })
}