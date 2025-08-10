package town.amrita.timetable.models

import android.content.Context
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import androidx.glance.appwidget.updateAll
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import town.amrita.timetable.utils.LocalTimeSerializer
import town.amrita.timetable.widget.TimetableAppWidget
import town.amrita.timetable.widget.ensureWorkAndAlarms
import java.io.InputStream
import java.io.OutputStream
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Serializable
data class WidgetConfig(
  val day: String? = null,
  val isLocal: Boolean = true,
  val file: String? = null,
  val electiveChoices: Map<String, String> = emptyMap(),
  val lockedUntil: Long? = null,
  val showFreePeriods: Boolean = true,

  @Serializable(with = LocalTimeSerializer::class)
  val showNextDayAt: LocalTime = LocalTime.of(23, 59)
)

private object WidgetConfigSerializer : Serializer<WidgetConfig> {
  override val defaultValue: WidgetConfig = WidgetConfig()

  @OptIn(ExperimentalSerializationApi::class)
  override suspend fun readFrom(input: InputStream): WidgetConfig {
    return Json.decodeFromStream(input)
  }

  @OptIn(ExperimentalSerializationApi::class)
  override suspend fun writeTo(t: WidgetConfig, output: OutputStream) {
    return Json.encodeToStream(t, output)
  }
}

val Context.widgetConfig by dataStore(
  fileName = "widget_config.json",
  serializer = WidgetConfigSerializer
)

suspend fun Context.updateDay(day: String?) {
  this.widgetConfig.updateData {
    it.copy(day = day)
  }

  TimetableAppWidget().updateAll(this)
}

suspend fun Context.updateFile(file: String) {
  this.widgetConfig.updateData {
    it.copy(file = file)
  }

  TimetableAppWidget().updateAll(this)
}

suspend fun Context.updateLock(isLocked: Boolean) {
  this.widgetConfig.updateData {
    it.copy(
      lockedUntil =
        if (isLocked)
          LocalDateTime.now()
            .plusDays(1)
            .truncatedTo(ChronoUnit.DAYS)
            .toEpochSecond(ZonedDateTime.now().offset)
        else Instant.MIN.epochSecond
    )
  }

  TimetableAppWidget().updateAll(this)
}

suspend fun Context.updateIsLocal(isLocal: Boolean) {
  this.widgetConfig.updateData {
    it.copy(isLocal = isLocal)
  }

  TimetableAppWidget().updateAll(this)
}

suspend fun Context.updateShowFreePeriods(showFreePeriods: Boolean) {
  this.widgetConfig.updateData {
    it.copy(showFreePeriods = showFreePeriods)
  }

  TimetableAppWidget().updateAll(this)
}

suspend fun Context.updateShowNextDayAt(showNextDayAt: LocalTime) {
  this.widgetConfig.updateData {
    it.copy(showNextDayAt = showNextDayAt)
  }

  this.ensureWorkAndAlarms()
  TimetableAppWidget().updateAll(this)
}
