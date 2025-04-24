package me.heftymouse.timetable.models

import android.content.Context
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.glance.appwidget.updateAll
import me.heftymouse.timetable.widget.TimetableWidget

val Context.widgetConfig by preferencesDataStore("TimetableWidget")
val dayKey = stringPreferencesKey("TIMETABLE_DAY")
val fileKey = stringPreferencesKey("TIMETABLE_FILE")
val lockedKey = longPreferencesKey("TIMETABLE_LOCKED_UNTIL")

suspend fun updateWidget(context: Context, day: String) {
    context.widgetConfig.updateData {
        it.toMutablePreferences().apply { this[dayKey] = day }
    }

    TimetableWidget().updateAll(context)
}
