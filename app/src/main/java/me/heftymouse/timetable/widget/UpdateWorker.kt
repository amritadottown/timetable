package me.heftymouse.timetable.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import me.heftymouse.timetable.models.TimetableSpec
import me.heftymouse.timetable.models.fileKey
import me.heftymouse.timetable.models.isLocalKey
import me.heftymouse.timetable.models.lockedUntilKey
import me.heftymouse.timetable.models.updateDay
import me.heftymouse.timetable.models.widgetConfig
import me.heftymouse.timetable.utils.TODAY
import me.heftymouse.timetable.utils.updateTimetableFromRegistry
import java.time.Instant


class UpdateWorker(context: Context, workParams: WorkerParameters) :
  CoroutineWorker(context, workParams) {
  override suspend fun doWork(): Result {
    val config = this.applicationContext.widgetConfig.data.first()
    val timestamp = config[lockedUntilKey]
    if (timestamp != null && Instant.ofEpochSecond(timestamp).isAfter(Instant.now()))
      return Result.success()

    if (config[isLocalKey] == false) {
      try {
        val file = config[fileKey]
        if (file == null)
          return Result.failure()

        val spec = TimetableSpec.fromString(file)
        applicationContext.updateTimetableFromRegistry(spec)
      } catch (_: Exception) {
        return Result.failure()
      }
    }

    this.applicationContext.updateDay(TODAY)

    return Result.success()
  }
}