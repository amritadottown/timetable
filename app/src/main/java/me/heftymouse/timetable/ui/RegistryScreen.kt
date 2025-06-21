package me.heftymouse.timetable.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import me.heftymouse.timetable.ui.components.TimetableScaffold

@Composable
fun RegistryScreen(goBack: () -> Unit) {
 TimetableScaffold(title = "Download Timetables") {
  Column {
   Button(onClick = goBack) { Text("bb") }
  }
 }
}