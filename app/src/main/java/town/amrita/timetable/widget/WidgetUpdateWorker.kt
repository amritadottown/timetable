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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import town.amrita.timetable.models.TimetableSpec
import town.amrita.timetable.models.UPDATE_TIMES
import town.amrita.timetable.models.updateDay
import town.amrita.timetable.models.widgetConfig
import town.amrita.timetable.utils.updateTimetableFromRegistry
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class AlarmReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent?) {
    if (context == null)
      return

    val pending = goAsync()
    CoroutineScope(Dispatchers.Default).launch {
      try {
        context.ensureWorkAndAlarms()
        val work =
          OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        WorkManager.getInstance(context).enqueue(work)
      } finally {
        pending.finish()
      }
    }
  }
}

class WidgetUpdateWorker(context: Context, workParams: WorkerParameters) :
  CoroutineWorker(context, workParams) {
  override suspend fun doWork(): Result {
    val config = this.applicationContext.widgetConfig.data.first()
    val timestamp = config.lockedUntil

    if (tags.contains("REGISTRY_UPDATE") && config.isLocal == false) {
      applicationContext.ensureWorkAndAlarms()
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
      this.applicationContext.updateDay(null)

    return Result.success()
  }
}

class AlarmRestoreReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent?) {
    if (intent?.action != Intent.ACTION_BOOT_COMPLETED && intent?.action != Intent.ACTION_MY_PACKAGE_REPLACED)
      return

    val pending = goAsync()
    CoroutineScope(Dispatchers.Default).launch {
      try {
        context?.ensureWorkAndAlarms()
      } finally {
        pending.finish()
      }
    }
  }
}

suspend fun Context.ensureWorkAndAlarms() {
  val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager

  val now = LocalTime.now()
  val showNextDayAt = this@ensureWorkAndAlarms.widgetConfig.data.first().showNextDayAt
  val updateTimes = (UPDATE_TIMES + showNextDayAt).sorted()
  val nextAlarmTime = when {
    showNextDayAt <= now -> LocalDate.now().plusDays(1).atTime(updateTimes.first())
    else -> LocalDate.now().atTime(updateTimes.fastFirst { now <= it })
  }.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

  val intent = Intent(this, AlarmReceiver::class.java)
  val pendingIntent = PendingIntent.getBroadcast(
    this,
    0,
    intent,
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
  )
  alarmManager.cancel(pendingIntent)
  alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextAlarmTime, pendingIntent)

  val work = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(1, TimeUnit.HOURS)
    .addTag("REGISTRY_UPDATE")
    .build()

  WorkManager.getInstance(this)
    .enqueueUniquePeriodicWork("TIMETABLE_UPDATE_WORKER", ExistingPeriodicWorkPolicy.KEEP, work)
}