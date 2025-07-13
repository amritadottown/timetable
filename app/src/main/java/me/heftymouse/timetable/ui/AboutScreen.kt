package me.heftymouse.timetable.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import me.heftymouse.timetable.ui.components.TimetableScaffold

@Composable
fun AboutScreen() {
  TimetableScaffold("About") {
    Column {
      Text("ASEB Timetable")
      Text(
        buildAnnotatedString {
          append("Built by ")
          withLink(LinkAnnotation.Url("https://amrita.town")) { append("amrita.town") }
        }
      )
      Text("More stuff here soon I guess")
    }
  }
}