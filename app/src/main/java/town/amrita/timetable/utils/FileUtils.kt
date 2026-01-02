package town.amrita.timetable.utils

import android.annotation.SuppressLint
import android.app.Activity.MODE_PRIVATE
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.glance.appwidget.updateAll
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import town.amrita.timetable.models.Timetable
import town.amrita.timetable.registry.TimetableSpec
import town.amrita.timetable.registry.RegistryService
import retrofit2.await
import town.amrita.timetable.models.widgetConfig
import town.amrita.timetable.widget.TimetableAppWidget
import java.io.FileInputStream

@SuppressLint("Range")
suspend fun Context.updateTimetableFromUri(uri: Uri, config: Map<String, String> = emptyMap()) {
  val displayName = getDisplayName(uri)

  contentResolver.openFileDescriptor(uri, "r").use { fd ->
    val inputChannel = FileInputStream(fd?.fileDescriptor).channel
    val outputChannel = openFileOutput(displayName, MODE_PRIVATE).channel

    inputChannel.use { input ->
      outputChannel.use { output ->
        output.transferFrom(input, 0, input.size())
      }
    }

    this.widgetConfig.updateData {
      it.copy(
        file = displayName,
        isLocal = true,
        electiveChoices = config
      )
    }

    TimetableAppWidget().updateAll(this)
  }
}

@SuppressLint("Range")
fun Context.getDisplayName(uri: Uri): String {
  try {
    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
      if (cursor.moveToFirst()) {
        val displayName =
          cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
        return displayName
      }
    }
  } catch (e: Exception) {
    Log.e("Timetable", e.stackTraceToString())
  }

  return "⚠️ Unknown File Name"
}

@OptIn(ExperimentalSerializationApi::class)
fun Context.getFileContent(uri: Uri): Timetable {
  contentResolver.openFileDescriptor(uri, "r").use { fd ->
    return Json.decodeFromStream<Timetable>(FileInputStream(fd?.fileDescriptor))
  }
}

@OptIn(ExperimentalSerializationApi::class)
suspend fun Context.updateTimetableFromRegistry(spec: TimetableSpec, config: Map<String, String> = emptyMap()) {
  val newTT = RegistryService.instance.getTimetable(spec).await()
  this.openFileOutput("$spec.json", MODE_PRIVATE).use { out ->
    Json.encodeToStream(newTT, out)
  }

  this.widgetConfig.updateData {
    it.copy(
      file = spec.toString(),
      isLocal = false,
      electiveChoices = config
    )
  }

  TimetableAppWidget().updateAll(this)
}