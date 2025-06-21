package me.heftymouse.timetable.ui

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import me.heftymouse.timetable.activity.TimetableShareActivity
import me.heftymouse.timetable.ui.components.TimetableScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(openRegistryPage: () -> Unit) {
  val context = LocalContext.current
  TimetableScaffold(title = "Timetable") {
    Column {
      Button(onClick = openRegistryPage) { Text("aa") }
      Button(onClick = {
        val intent = Intent(context, TimetableShareActivity::class.java)
        context.startActivity(intent)
      }) { Text("bb") }
    }
  }
}