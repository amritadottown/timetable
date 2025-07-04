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
import me.heftymouse.timetable.widget.TimetableAppWidget
import me.heftymouse.timetable.models.fileKey
import me.heftymouse.timetable.models.updateFile
import me.heftymouse.timetable.models.widgetConfig
import java.io.FileInputStream

@SuppressLint("Range")
suspend fun Context.updateTimetableFromUri(uri: Uri) {
    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val displayName =
                cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            Log.d("Timetable", displayName)
            contentResolver.openFileDescriptor(uri, "r").use { fd ->
                val inputChannel = FileInputStream(fd?.fileDescriptor).channel
                val outputChannel = openFileOutput(displayName, MODE_PRIVATE).channel

                inputChannel.use { input ->
                    outputChannel.use { output ->
                        output.transferFrom(input, 0, input.size())
                    }
                }
                this.updateFile(displayName)
            }
        }
    }
}
