package town.amrita.timetable.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource

data class TimetableColors(
  val behindTimetableItem: Color
)

val LocalTimetableColors = staticCompositionLocalOf<TimetableColors> { error("how?") }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TimetableTheme(content: @Composable () -> Unit) {
  val darkTheme = isSystemInDarkTheme()
  val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
  val colorScheme = when {
    dynamicColor && darkTheme -> dynamicDarkColorScheme(LocalContext.current)
    dynamicColor && !darkTheme -> dynamicLightColorScheme(LocalContext.current)
    darkTheme -> darkColorScheme()
    else -> expressiveLightColorScheme()
  }

  val localTimetableColors = TimetableColors(
    behindTimetableItem = when {
      dynamicColor && darkTheme -> colorResource(android.R.color.system_accent2_800)
      dynamicColor && !darkTheme -> colorResource(android.R.color.system_accent2_100)
      darkTheme -> Color(0xff20333d)
      else -> Color(0xffe0f3ff)
    }
  )

  CompositionLocalProvider(LocalTimetableColors provides localTimetableColors) {
    MaterialExpressiveTheme(
      colorScheme = colorScheme,
      content = content
    )
  }

}