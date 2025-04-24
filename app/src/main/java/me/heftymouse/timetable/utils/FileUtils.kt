package me.heftymouse.timetable.utils

import android.annotation.SuppressLint
import android.app.Activity.MODE_PRIVATE
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.flow.first
import me.heftymouse.timetable.widget.TimetableWidget
import me.heftymouse.timetable.models.fileKey
import me.heftymouse.timetable.models.widgetConfig
import java.io.FileInputStream

@SuppressLint("Range")
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
suspend fun Context.updateTimetableFromUri(uri: Uri) {
    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val displayName =
                cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            Log.d("Timetable", displayName)
            contentResolver.openFileDescriptor(uri, "r").use { fd ->
                val data = FileInputStream(fd?.fileDescriptor).readAllBytes()
                openFileOutput(displayName, MODE_PRIVATE).use { outputStream ->
                    outputStream.write(data)
                }
                widgetConfig.updateData {
                    it.toMutablePreferences().apply {
                        this[fileKey] = displayName
                    }
                }
                Log.d("Timetable", widgetConfig.data.first()[fileKey] ?: "not found")
                TimetableWidget().updateAll(this)
            }
        }
    }
}
