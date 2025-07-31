package town.amrita.timetable.widget

import android.app.AlarmManager
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import town.amrita.timetable.models.TimetableSpec
import town.amrita.timetable.models.updateDay
import town.amrita.timetable.models.widgetConfig
import town.amrita.timetable.utils.TODAY
import town.amrita.timetable.utils.updateTimetableFromRegistry
import java.time.Instant

class UpdateWorker(context: Context, workParams: WorkerParameters) :
  CoroutineWorker(context, workParams) {
  override suspend fun doWork(): Result {
    val config = this.applicationContext.widgetConfig.data.first()
    val timestamp = config.lockedUntil
    if (timestamp != null && Instant.ofEpochSecond(timestamp).isAfter(Instant.now()))
      return Result.success()

    if (config.isLocal == false) {
      try {
        val file = config.file
        if (file == null)
          return Result.failure()

        val spec = TimetableSpec.fromString(file)
        applicationContext.updateTimetableFromRegistry(spec)
      } catch (_: Exception) {
//        return Result.failure()
      }
    }

    this.applicationContext.updateDay(TODAY)

    return Result.success()
  }
}