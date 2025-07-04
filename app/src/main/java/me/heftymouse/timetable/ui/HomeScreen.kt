package me.heftymouse.timetable.ui

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.heftymouse.timetable.activity.TimetableShareActivity
import me.heftymouse.timetable.ui.components.TimetableScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(openRegistryPage: () -> Unit) {
  val context = LocalContext.current
  TimetableScaffold(title = "Timetable") {
    Column {
      Card(modifier = Modifier.fillMaxWidth(), onClick = openRegistryPage) {
        Text("Select Timetable", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge)
      }
    }
  }
}

@Preview
@Composable
fun HomeScreenPreview() {
  TimetableTheme {
    HomeScreen {  }
  }
}