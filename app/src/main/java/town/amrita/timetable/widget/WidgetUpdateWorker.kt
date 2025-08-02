package town.amrita.timetable.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.compose.ui.util.fastFirst
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import town.amrita.timetable.models.TimetableSpec
import town.amrita.timetable.models.UPDATE_TIMES
import town.amrita.timetable.models.updateDay
import town.amrita.timetable.models.widgetConfig
import town.amrita.timetable.utils.TODAY
import town.amrita.timetable.utils.updateTimetableFromRegistry
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class AlarmReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent?) {
    if (intent != null && context != null) {
      context.ensureWorkAndAlarms()
      val work =
        OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
          .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
          .build()
      WorkManager.getInstance(context).enqueue(work)
    }
  }
}

class WidgetUpdateWorker(context: Context, workParams: WorkerParameters) :
  CoroutineWorker(context, workParams) {
  override suspend fun doWork(): Result {
    val config = this.applicationContext.widgetConfig.data.first()
    val timestamp = config.lockedUntil

    if (tags.contains("REGISTRY_UPDATE") && config.isLocal == false) {
      try {
        val file = config.file
        if (file == null)
          return Result.failure()

        val spec = TimetableSpec.fromString(file)
        applicationContext.updateTimetableFromRegistry(spec)
      } catch (_: Exception) {
      }
    }

    if (timestamp == null || Instant.ofEpochSecond(timestamp).isBefore(Instant.now()))
      this.applicationContext.updateDay(TODAY)

    return Result.success()
  }
}

class AlarmRestoreReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent?) {
    intent?.let {
      if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
        context?.ensureWorkAndAlarms()
      }
    }
  }
}

fun Context.ensureWorkAndAlarms() {
  val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager

  val now = LocalTime.now()
  val nextAlarmTime = when {
    now.isBefore(UPDATE_TIMES.first()) -> LocalDate.now().atTime(UPDATE_TIMES.first())
    now.isAfter(UPDATE_TIMES.last()) -> LocalDate.now().plusDays(1).atTime(UPDATE_TIMES.first())
    else -> LocalDate.now().atTime(UPDATE_TIMES.fastFirst { now.isBefore(it) })
  }.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

  val intent = Intent(this, AlarmReceiver::class.java)
  val pendingIntent = PendingIntent.getBroadcast(
    this,
    0,
    intent,
    PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
  )
  alarmManager.cancel(pendingIntent)
  alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextAlarmTime, pendingIntent)

  val work = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(1, TimeUnit.HOURS)
    .addTag("REGISTRY_UPDATE")
    .build()

  WorkManager.getInstance(this)
    .enqueueUniquePeriodicWork("TIMETABLE_UPDATE_WORKER", ExistingPeriodicWorkPolicy.KEEP, work)
}