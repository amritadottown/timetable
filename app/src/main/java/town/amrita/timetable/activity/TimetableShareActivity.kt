package town.amrita.timetable.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import kotlinx.coroutines.launch
import town.amrita.timetable.models.Timetable
import town.amrita.timetable.models.validateSchedule
import town.amrita.timetable.ui.TimetableTheme
import town.amrita.timetable.ui.components.LocalSnackbarState
import town.amrita.timetable.ui.components.TimetablePreview
import town.amrita.timetable.ui.components.TimetableScaffold
import town.amrita.timetable.utils.getDisplayName
import town.amrita.timetable.utils.getFileContent
import town.amrita.timetable.utils.updateTimetableFromUri
import town.amrita.timetable.widget.Sizes
import town.amrita.timetable.widget.TimetableAppWidget
import town.amrita.timetable.widget.TimetableWidgetReceiver

class TimetableShareActivity : ComponentActivity() {
  @SuppressLint("NewApi")
  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      val scope = rememberCoroutineScope()

      val uri = intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)

      var name by remember { mutableStateOf("") }
      var timetable by remember { mutableStateOf<Timetable?>(null) }
      val validationResult =
        remember(timetable) { timetable?.let { validateSchedule(it) } ?: emptyList() }

      LaunchedEffect(true) {
        if (uri != null) {
          name = getDisplayName(uri)
          timetable = getFileContent(uri)
        }
      }

      TimetableTheme {
        TimetableScaffold("Import Timetable") {
          val snackbarHostState = LocalSnackbarState.current

          Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Text(name)
            if (validationResult.isEmpty()) {
              TimetablePreview(
                Modifier
                  .weight(1f)
                  .fillMaxSize(), timetable = timetable
              )
            } else {
              Column(Modifier.weight(1f).fillMaxSize()) {
                Text(text = "⚠️ Errors found", fontWeight = FontWeight.Medium)
                validationResult.map {
                  Text(it)
                }
              }
            }
            Button(
              modifier = Modifier.fillMaxWidth(),
              enabled = validationResult.isEmpty(),
              onClick = {
                scope.launch {
                  uri?.let { updateTimetableFromUri(uri) }

                  val appWidgetManager = GlanceAppWidgetManager(this@TimetableShareActivity)
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

              }) {
              Text("Use Timetable")
            }
          }
        }
      }
    }
  }
}