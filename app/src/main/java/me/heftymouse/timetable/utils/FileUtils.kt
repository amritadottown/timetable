package me.heftymouse.timetable.utils

import android.annotation.SuppressLint
import android.app.Activity.MODE_PRIVATE
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import me.heftymouse.timetable.models.Timetable
import me.heftymouse.timetable.models.TimetableSpec
import me.heftymouse.timetable.models.updateFile
import me.heftymouse.timetable.models.updateIsLocal
import me.heftymouse.timetable.registry.RegistryService
import retrofit2.await
import java.io.FileInputStream

@SuppressLint("Range")
suspend fun Context.updateTimetableFromUri(uri: Uri) {
  val displayName = getDisplayName(uri)

  contentResolver.openFileDescriptor(uri, "r").use { fd ->
    val inputChannel = FileInputStream(fd?.fileDescriptor).channel
    val outputChannel = openFileOutput(displayName, MODE_PRIVATE).channel

    inputChannel.use { input ->
      outputChannel.use { output ->
        output.transferFrom(input, 0, input.size())
      }
    }
    this.updateFile(displayName)
    this.updateIsLocal(true)
  }
}

@SuppressLint("Range")
fun Context.getDisplayName(uri: Uri): String {
  contentResolver.query(uri, null, null, null, null)?.use { cursor ->
    if (cursor.moveToFirst()) {
      val displayName =
        cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
      return displayName
    }
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
suspend fun Context.updateTimetableFromRegistry(spec: TimetableSpec) {
  val newTT = RegistryService.instance.getTimetable(spec).await()
  this.openFileOutput("$spec.json", MODE_PRIVATE).use { out ->
    Json.encodeToStream(newTT, out)
  }
  this.updateFile(spec.toString())
  this.updateIsLocal(false)
}