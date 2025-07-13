package me.heftymouse.timetable.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.heftymouse.timetable.ui.components.TimetableScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(openRegistryPage: () -> Unit) {
  TimetableScaffold(title = "Timetable") {
    Column {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        onClick = openRegistryPage,
        colors = CardDefaults.cardColors()
          .copy(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
      ) {
        Text(
          "Select Timetable",
          modifier = Modifier.padding(24.dp),
          style = MaterialTheme.typography.bodyLarge
        )
      }
    }
  }
}

@Preview
@Composable
fun HomeScreenPreview() {
  TimetableTheme {
    HomeScreen { }
  }
}