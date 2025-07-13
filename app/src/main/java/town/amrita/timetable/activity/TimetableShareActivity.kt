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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import town.amrita.timetable.models.Timetable
import town.amrita.timetable.ui.TimetableTheme
import town.amrita.timetable.ui.components.TimetablePreview
import town.amrita.timetable.ui.components.TimetableScaffold
import town.amrita.timetable.utils.getDisplayName
import town.amrita.timetable.utils.getFileContent
import town.amrita.timetable.utils.updateTimetableFromUri

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
      LaunchedEffect(true) {
        if (uri != null) {
          name = getDisplayName(uri)
          timetable = getFileContent(uri)
        }
      }

      TimetableTheme {
        TimetableScaffold("Import Timetable") {
          Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Text(name)
            TimetablePreview(modifier = Modifier.weight(1f), timetable = timetable)
            Button(
              modifier = Modifier.fillMaxWidth(),
              onClick = {
                scope.launch {
                  if (uri != null)
                    updateTimetableFromUri(uri)
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