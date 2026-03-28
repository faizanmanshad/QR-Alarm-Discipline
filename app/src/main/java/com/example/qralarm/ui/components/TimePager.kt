package com.example.qralarm.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HorizontalNumberPicker(
    range: IntRange,
    initialValue: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val list = range.toList()
    val initialPage = list.indexOf(initialValue).coerceAtLeast(0)

    // This state controls the "Sliding"
    val pagerState = rememberPagerState(initialPage = initialPage) { list.size }

    // Sync the sliding position back to our ViewModel
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            onValueChange(list[page])
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 60.dp), // Shows side numbers
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) { page ->
            val number = list[page]

            // Calculate how far this number is from the center to "Fade" it out
            val pageOffset = (
                    (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                    ).absoluteValue

            Text(
                text = String.format("%02d", number),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 50.sp,
                    fontWeight = if (pageOffset < 0.5f) FontWeight.ExtraBold else FontWeight.Normal,
                    fontFamily = MaterialTheme.typography.displayLarge.fontFamily // Montserrat
                ),
                color = if (pageOffset < 0.5f)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier
                    .graphicsLayer {
                        // This creates the "Zoom" effect for the center number
                        val scale = lerp(
                            start = 0.7f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                        scaleX = scale
                        scaleY = scale
                        alpha = lerp(
                            start = 0.3f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                    }
            )
        }
    }
}