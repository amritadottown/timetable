package town.amrita.timetable.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import town.amrita.timetable.ui.components.TimetableScaffold
import town.amrita.timetable.BuildConfig

@Composable
fun AboutScreen() {
  TimetableScaffold("About") {
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
          withLink(LinkAnnotation.Url("https://github.com/amritadottown/timetable-registry")) { append("Timetable Registry on GitHub") }
        }
      )
      Text("More stuff here soon I guess")
    }
  }
}