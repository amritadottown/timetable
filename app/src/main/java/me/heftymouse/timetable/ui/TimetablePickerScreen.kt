package me.heftymouse.timetable.ui

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import me.heftymouse.timetable.registry.Registry
import me.heftymouse.timetable.registry.RegistryService
import me.heftymouse.timetable.ui.components.TimetableScaffold
import retrofit2.await

@Composable
fun RegistryScreen(goBack: () -> Unit) {
  val coroutineScope = rememberCoroutineScope()
  var data: Registry? by remember { mutableStateOf(null) }

  TimetableScaffold(title = "Download Timetables") {
    LaunchedEffect(true) {
      try {
        data = RegistryService.instance.getRegistry().await()
      } catch(e: Exception) {
        Log.d("Timetable", e.toString())
      }
    }
    Column {
      LazyColumn {
        data?.let {
          items(items = it.timetables) {
            Text(it)
          }
        }
      }
    }
  }
}