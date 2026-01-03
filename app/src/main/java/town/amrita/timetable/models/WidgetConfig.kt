package town.amrita.timetable.models

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import androidx.glance.appwidget.updateAll
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToStream
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import town.amrita.timetable.utils.LocalTimeSerializer
import town.amrita.timetable.widget.TimetableAppWidget
import town.amrita.timetable.widget.ensureWorkAndAlarms
import java.io.InputStream
import java.io.OutputStream
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

const val CONFIG_VERSION = 2

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class WidgetConfig(
  val version: Int = -1,

  val day: DayOfWeek? = null,
  val isLocal: Boolean = true,
  val file: String? = null,
  val electiveChoices: Map<String, String> = emptyMap(),
  val lockedUntil: Long? = null,
  val showFreePeriods: Boolean = true,
  val showCompletedPeriods: Boolean = true,
  @Serializable(with = LocalTimeSerializer::class)
  val showNextDayAt: LocalTime = LocalTime.of(23, 59)
)

val DEFAULT_CONFIG = WidgetConfig(version = CONFIG_VERSION)

private object WidgetConfigSerializer : Serializer<WidgetConfig> {
  override val defaultValue: WidgetConfig = DEFAULT_CONFIG

  @OptIn(ExperimentalSerializationApi::class)
  override suspend fun readFrom(input: InputStream): WidgetConfig {
    val text = input.bufferedReader().use { it.readText() }
    try {
      val jsonElement = Json.parseToJsonElement(text)
      val json = jsonElement.jsonObject
      var version = json["version"]?.jsonPrimitive?.int ?: 0

      return when (version) {
        CONFIG_VERSION -> Json.decodeFromJsonElement<WidgetConfig>(jsonElement)
        else -> {
          val currentJson = json.toMutableMap()
          if (version == 0) {
            // every version before migrations were added
            // the last one of those (1.0.6) incorrectly deserializes day from int to DayOfWeek
            currentJson["day"] = JsonNull
            version = 1
          }
          if (version == 1) {
            // v2 schema migration: clear timetable data to force reconfiguration
            // we can preserve cosmetic options though
            currentJson["day"] = JsonNull
            currentJson["isLocal"] = JsonPrimitive(true)
            currentJson["file"] = JsonNull
            currentJson["electiveChoices"] = JsonObject(emptyMap())
            currentJson["lockedUntil"] = JsonNull

            version = 2
          }

          Json.decodeFromJsonElement<WidgetConfig>(JsonObject(currentJson)).copy(version = CONFIG_VERSION)
        }
      }
    } catch (e: Exception) {
      throw CorruptionException("Could not parse config", e)
    }
  }

  @OptIn(ExperimentalSerializationApi::class)
  override suspend fun writeTo(t: WidgetConfig, output: OutputStream) {
    return Json.encodeToStream(t, output)
  }
}

val Context.widgetConfig by dataStore(
  fileName = "widget_config.json",
  serializer = WidgetConfigSerializer,
  corruptionHandler = ReplaceFileCorruptionHandler<WidgetConfig> { _ ->
    WidgetConfigSerializer.defaultValue
  }
)

suspend fun Context.updateDay(day: DayOfWeek?) {
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

suspend fun Context.updateElectiveChoices(choices: Map<String, String>) {
  this.widgetConfig.updateData {
    it.copy(electiveChoices = choices)
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

suspend fun Context.updateShowCompletedPeriods(showCompletedPeriods: Boolean) {
  this.widgetConfig.updateData {
    it.copy(showCompletedPeriods = showCompletedPeriods)
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
