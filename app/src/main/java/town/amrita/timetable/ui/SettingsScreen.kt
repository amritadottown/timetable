package town.amrita.timetable.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import town.amrita.timetable.BuildConfig
import town.amrita.timetable.models.WidgetConfig
import town.amrita.timetable.models.updateShowFreePeriods
import town.amrita.timetable.models.widgetConfig
import town.amrita.timetable.ui.components.TimetableScaffold

@Composable
fun SettingsScreen() {
  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  val settings = context.widgetConfig.data.collectAsState(WidgetConfig())

  TimetableScaffold("Settings") {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
      Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Show Free Periods")
        Switch(
          settings.value.showFreePeriods,
          onCheckedChange = {
            scope.launch {
              context.updateShowFreePeriods(it)
            }
          }
        )
      }
      HorizontalDivider()
      Column {
        Text("ASEB Timetable")
        Text("Version ${BuildConfig.VERSION_NAME} ${BuildConfig.BUILD_TYPE}")
        Text(
          buildAnnotatedString {
            append("Built by ")
            withLink(LinkAnnotation.Url("https://amrita.town")) { append("amrita.town") }
          }
        )
        Text(
          buildAnnotatedString {
            withLink(LinkAnnotation.Url("https://github.com/amritadottown/timetable-registry")) {
              append(
                "Timetable Registry on GitHub"
              )
            }
          }
        )
        Text("More stuff here soon I guess")
      }
    }
  }
}