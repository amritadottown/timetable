package me.heftymouse.timetable.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import me.heftymouse.timetable.models.lockedKey
import me.heftymouse.timetable.models.updateWidget
import me.heftymouse.timetable.models.widgetConfig
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class UpdateWorker(context: Context, workParams: WorkerParameters) : CoroutineWorker(context, workParams) {
    override suspend fun doWork(): Result {
        val timestamp = this.applicationContext.widgetConfig.data.first()[lockedKey]
        if(timestamp != null && Instant.ofEpochSecond(timestamp).isAfter(Instant.now()))
            return Result.success()

        val day = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE"))
        updateWidget(this.applicationContext, day)

        return Result.success()
    }
}