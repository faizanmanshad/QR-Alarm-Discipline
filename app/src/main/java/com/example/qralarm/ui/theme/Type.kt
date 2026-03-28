package com.example.qralarm.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.qralarm.R

// 1. Your Local Font Family
val MontserratFontFamily = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_bold, FontWeight.Bold)
)

// 2. The Complete Typography Map
val AppTypography = Typography(
    // Large Headings
    displayLarge = TextStyle(fontFamily = MontserratFontFamily, fontWeight = FontWeight.Normal, fontSize = 57.sp),
    headlineLarge = TextStyle(fontFamily = MontserratFontFamily, fontWeight = FontWeight.Normal, fontSize = 32.sp),

    // Top Bar Titles
    titleLarge = TextStyle(fontFamily = MontserratFontFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = MontserratFontFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp),

    // Main Body Text
    bodyLarge = TextStyle(fontFamily = MontserratFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = MontserratFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),

    // 🚨 THIS FIXES THE BOTTOM BAR LABELS 🚨
    labelLarge = TextStyle(fontFamily = MontserratFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = MontserratFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp),
    labelSmall = TextStyle(fontFamily = MontserratFontFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp)
)