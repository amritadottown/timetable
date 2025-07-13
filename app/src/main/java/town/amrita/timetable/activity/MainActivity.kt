package town.amrita.timetable.activity

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import town.amrita.timetable.models.fileKey
import town.amrita.timetable.models.widgetConfig
import town.amrita.timetable.ui.RootScreen

val LocalWidgetId = staticCompositionLocalOf { AppWidgetManager.INVALID_APPWIDGET_ID }

class MainActivity : ComponentActivity() {
  var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    appWidgetId = intent?.extras?.getInt(
      AppWidgetManager.EXTRA_APPWIDGET_ID,
      AppWidgetManager.INVALID_APPWIDGET_ID
    ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

    setContent {
      CompositionLocalProvider(LocalWidgetId provides appWidgetId) {
        RootScreen()
      }
    }
  }

  override fun finish() {
    val currentFileKey = runBlocking { widgetConfig.data.first() }[fileKey]
    if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID && currentFileKey != null) {
      val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
      setResult(RESULT_OK, resultValue)
    }
    super.finish()
  }
}