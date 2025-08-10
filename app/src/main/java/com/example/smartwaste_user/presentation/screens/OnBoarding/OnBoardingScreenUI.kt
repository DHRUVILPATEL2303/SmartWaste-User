package com.example.smartwaste_user.presentation.screens.OnBoarding

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.pager.*
import com.example.smartwaste_user.R
import com.example.smartwaste_user.data.models.OnBoardingPage
import com.example.smartwaste_user.presentation.navigation.Routes
import com.example.smartwaste_user.presentation.viewmodels.OnboardingViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnBoardingScreenUI(navController: NavController,viewModel: OnboardingViewModel= hiltViewModel<OnboardingViewModel>()) {
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()

    val pages = listOf(
        OnBoardingPage(R.drawable.onboarding_1, "Smart Waste Collection", "Effortlessly manage waste with our intelligent collection system."),
        OnBoardingPage(R.drawable.onboarding_2, "Track and Recycle", "Monitor your recycling progress and contribute to a greener planet."),
        OnBoardingPage(R.drawable.onboarding_3, "Join Our Mission", "Be part of our community to keep the city clean and sustainable.")
    )

    val gradientColors = listOf(
        Color(0xFF0C180D),
        Color(0xFF1E3A1F),
        Color(0xFF498349)
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)

                ,
        containerColor = Color.Transparent,
        bottomBar = {
            BottomBarContent(pagerState, pages.size) {
                if (pagerState.currentPage == pages.lastIndex) {
                    viewModel.setOnboardingShown()
                    navController.navigate(Routes.LoginScreen) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                } else {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            count = pages.size,
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) { page ->
            OnBoardingPageContent(page = pages[page], pageIndex = page, currentPage = pagerState.currentPage)
        }
    }
}

@Composable
fun BottomBarContent(pagerState: PagerState, pageCount: Int, onNextClick: () -> Unit) {
    val buttonScale by animateFloatAsState(
        targetValue = if (pagerState.currentPage == pageCount - 1) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 200f)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp, vertical = 24.dp),

        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        HorizontalPagerIndicator(
            pagerState = pagerState,
            activeColor = Color(0xFF4CAF50),
            inactiveColor = Color.White.copy(alpha = 0.3f),
            indicatorWidth = 12.dp,
            indicatorHeight = 12.dp,
            spacing = 10.dp,
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Button(
            onClick = onNextClick,
            shape = RoundedCornerShape(50),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp, pressedElevation = 12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White
            ),
            modifier = Modifier
                .weight(2f)
                .height(56.dp)
                .scale(buttonScale)
                .padding(end = 16.dp)
        ) {
            Text(
                text = if (pagerState.currentPage == pageCount - 1) "Get Started" else "Next",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun OnBoardingPageContent(
    page: OnBoardingPage,
    pageIndex: Int,
    currentPage: Int
) {
    val isCurrentPage = pageIndex == currentPage

    val scale by animateFloatAsState(
        targetValue = if (isCurrentPage) 1f else 0.92f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isCurrentPage) 1f else 0.5f,
        animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing),
        label = "alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp)
            .scale(scale)
            .alpha(alpha)
            .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = page.imageRes),
            contentDescription = page.title,
            modifier = Modifier
                .size(if (isCurrentPage) 340.dp else 300.dp)
                .clip(RoundedCornerShape(70.dp))
                .padding(16.dp)
                .graphicsLayer {
                    shadowElevation = if (isCurrentPage) 12f else 2f
                    shape = RoundedCornerShape(70.dp)
                    clip = true
                },
            contentScale = ContentScale.FillBounds
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .animateContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = page.title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 34.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = page.description ?: "",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}