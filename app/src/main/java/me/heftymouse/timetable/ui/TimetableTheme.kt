package me.heftymouse.timetable.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun TimetableTheme(content: @Composable () -> Unit) {
  val colorScheme =
    if (isSystemInDarkTheme())
      dynamicDarkColorScheme(LocalContext.current)
    else
      dynamicLightColorScheme(LocalContext.current)

  MaterialTheme(
    colorScheme = colorScheme,
    content = content
  )
}