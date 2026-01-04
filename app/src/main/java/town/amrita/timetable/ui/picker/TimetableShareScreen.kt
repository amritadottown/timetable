package town.amrita.timetable.ui.picker

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import town.amrita.timetable.models.Timetable
import town.amrita.timetable.models.validate
import town.amrita.timetable.ui.TimetableTheme
import town.amrita.timetable.ui.components.TimetablePreview
import town.amrita.timetable.ui.components.TimetableScaffold
import town.amrita.timetable.utils.getDisplayName
import town.amrita.timetable.utils.getFileContent
import town.amrita.timetable.utils.updateTimetableFromUri

@Composable
fun TimetableShareScreen(uri: Uri?, goToConfig: (Timetable, Uri) -> Unit = { _, _ -> }) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  var name by remember { mutableStateOf("") }
  var timetable by remember { mutableStateOf<Timetable?>(null) }
  val validationResult =
    remember(timetable) { timetable?.validate() ?: emptyList() }

  LaunchedEffect(true) {
    if (uri != null) {
      scope.launch {
        name = context.getDisplayName(uri)
        timetable = withContext(Dispatchers.IO) { context.getFileContent(uri) }
      }
    }
  }

  TimetableTheme {
    TimetableScaffold("Import Timetable") {
      Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        if (uri == null) {
          Text("No URI provided")
          return@Column
        }

        timetable?.let { timetable ->
          Text(name)
          if (validationResult.isEmpty()) {
            TimetablePreview(
              Modifier
                .weight(1f)
                .fillMaxSize(), timetable = timetable
            )
          } else {
            Column(Modifier
              .weight(1f)
              .fillMaxSize()) {
              Text(text = "⚠️ Errors found", fontWeight = FontWeight.Medium)
              validationResult.map {
                Text(it)
              }
            }
          }

          val needsConfig = timetable.config.isNotEmpty()

          if (needsConfig) {
            Button(
              modifier = Modifier.fillMaxWidth(),
              enabled = validationResult.isEmpty(),
              onClick = {
                goToConfig(timetable, uri)
              }
            ) {
              Text("Continue")
            }
          } else {
            UseTimetableButton(
              modifier = Modifier.fillMaxWidth(),
              enabled = validationResult.isEmpty(),
              onApplyTimetable = {
                context.updateTimetableFromUri(uri, emptyMap())
              }
            )
          }
        }
      }
    }
  }
}