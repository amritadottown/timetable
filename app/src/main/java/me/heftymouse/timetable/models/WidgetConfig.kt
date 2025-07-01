package me.heftymouse.timetable.models

import android.content.Context
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.glance.appwidget.updateAll
import me.heftymouse.timetable.widget.TimetableAppWidget
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

val Context.widgetConfig by preferencesDataStore("TimetableWidget")
val dayKey = stringPreferencesKey("TIMETABLE_DAY")
val fileKey = stringPreferencesKey("TIMETABLE_FILE")
val lockedUntilKey = longPreferencesKey("TIMETABLE_LOCKED_UNTIL")

suspend fun Context.updateDay(day: String) {
    this.widgetConfig.updateData {
        it.toMutablePreferences().apply { this[dayKey] = day }
    }

    TimetableAppWidget().updateAll(this)
}

suspend fun Context.updateFile(file: String) {
    this.widgetConfig.updateData {
        it.toMutablePreferences().apply { this[fileKey] = file }
    }

    TimetableAppWidget().updateAll(this)
}

suspend fun Context.updateLock(isLocked: Boolean) {
    this.widgetConfig.updateData {
        it.toMutablePreferences().apply {
            this[lockedUntilKey] =
                if(isLocked)
                    LocalDateTime.now()
                        .plusDays(1)
                        .truncatedTo(ChronoUnit.DAYS)
                        .toEpochSecond(ZonedDateTime.now().offset)
                else Instant.MIN.epochSecond
        }
    }

    TimetableAppWidget().updateAll(this)
}