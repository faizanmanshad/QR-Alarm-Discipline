package com.example.qralarm.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelPicker(
    modifier: Modifier = Modifier,
    items: List<String>,
    initialIndex: Int = 0,
    onValueChange: (Int) -> Unit,
    itemHeight: Dp = 50.dp, // Height of a single item
    numberOfVisibleItems: Int = 3 // We will show prev, center, next
) {
    // 1. Setup Infinite Loop logic (Large item count)
    val listSize = Int.MAX_VALUE
    val startListIndex = remember {
        (listSize / 2) - ((listSize / 2) % items.size) + initialIndex
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startListIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // 2. React to center item changes
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .map { it % items.size }
            .distinctUntilChanged()
            .collect { index ->
                onValueChange(index)
            }
    }

    // 3. UI Layout
    Box(
        modifier = modifier
            .height(itemHeight * numberOfVisibleItems) // Total height based on visible items
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // --- 🚨 FIXED SOLUTION: HIGHLIGHTED LINES IN THE CENTER ---
        // We draw these behind the list so the active item sits between them
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .offset(y = -(itemHeight / 2)),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            thickness = 1.dp
        )
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .offset(y = (itemHeight / 2)),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            thickness = 1.dp
        )

        // The Scrolling List
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(vertical = itemHeight), // Padding allows center-snapping
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(listSize) { index ->
                val actualIndex = index % items.size
                val item = items[actualIndex]

                // --- 🚨 FIXED SOLUTION: CENTER HIGHLIGHT LOGIC ---
                // We compare current item index with the center-most item index
                val isCenter = remember(listState.firstVisibleItemIndex) {
                    // Center item is firstVisible + numberVisible/2 - contentPaddingOffset
                    listState.firstVisibleItemIndex == index
                }

                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item,
                        fontSize = if (isCenter) 38.sp else 24.sp,
                        fontWeight = if (isCenter) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCenter) Color.White else Color.White.copy(alpha = 0.2f),
                        style = MaterialTheme.typography.displayLarge // Use Montserrat if applied globally
                    )
                }
            }
        }
    }
}