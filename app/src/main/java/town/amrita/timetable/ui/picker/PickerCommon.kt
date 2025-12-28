package town.amrita.timetable.ui.picker

import android.appwidget.AppWidgetManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.glance.appwidget.GlanceAppWidgetManager
import kotlinx.coroutines.launch
import town.amrita.timetable.activity.LocalWidgetId
import town.amrita.timetable.ui.components.LocalSnackbarState
import town.amrita.timetable.widget.Sizes
import town.amrita.timetable.widget.TimetableAppWidget
import town.amrita.timetable.widget.TimetableWidgetReceiver

@Composable
fun UseTimetableButton(
  modifier: Modifier = Modifier,
  enabled: Boolean,
  onApplyTimetable: suspend () -> Unit
) {
  val scope = rememberCoroutineScope()
  val context = LocalContext.current

  val widgetId = LocalWidgetId.current
  val snackbarHostState = LocalSnackbarState.current

  Button(
    modifier = modifier,
    enabled = enabled,
    onClick = {
      scope.launch {
        try {
          onApplyTimetable()

          if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            val activity = context as ComponentActivity
            activity.finish()
          } else {
            val appWidgetManager = GlanceAppWidgetManager(context)
            if (appWidgetManager.getGlanceIds(TimetableAppWidget::class.java).isEmpty()) {
              if (!appWidgetManager.requestPinGlanceAppWidget(
                  TimetableWidgetReceiver::class.java,
                  TimetableAppWidget(), Sizes.BEEG
                )
              ) {
                snackbarHostState.showSnackbar(
                  message = "Timetable updated. Place the Timetable widget on your home screen to see it.",
                  withDismissAction = true
                )
              }
            } else {
              snackbarHostState.showSnackbar(
                message = "Timetable updated",
                withDismissAction = true
              )
            }
          }
        } catch (e: Exception) {
          Log.d("Timetable", "Error using timetable: $e")
          snackbarHostState.showSnackbar(
            message = "Error updating timetable",
            withDismissAction = true
          )
        }
      }
    }) {
    Text("Use Timetable")
  }
}