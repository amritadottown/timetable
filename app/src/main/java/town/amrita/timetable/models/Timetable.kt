package town.amrita.timetable.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import town.amrita.timetable.utils.DayOfWeekSerializer
import town.amrita.timetable.utils.longName
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class Timetable(
  val subjects: Map<String, Subject>,
  val schedule: Map<@Serializable(with = DayOfWeekSerializer::class) DayOfWeek, List<String>>
)

val SLOTS: List<TimetableSlot> = listOf(
  TimetableSlot(LocalTime.of(8, 10), LocalTime.of(9, 0)),
  TimetableSlot(LocalTime.of(9, 0), LocalTime.of(9, 50)),
  TimetableSlot(LocalTime.of(9, 50), LocalTime.of(10, 40)),
  TimetableSlot(LocalTime.of(11, 0), LocalTime.of(11, 50)),
  TimetableSlot(LocalTime.of(11, 50), LocalTime.of(12, 40)),
  TimetableSlot(LocalTime.of(14, 0), LocalTime.of(14, 50)),
  TimetableSlot(LocalTime.of(14, 50), LocalTime.of(15, 40)),
)

val LAB_SLOTS: List<TimetableSlot> = listOf(
  TimetableSlot(LocalTime.of(8, 10), LocalTime.of(10, 25)),
  TimetableSlot(LocalTime.of(10, 50), LocalTime.of(13, 5)),
  TimetableSlot(LocalTime.of(13, 25), LocalTime.of(15, 40))
)

fun validateSchedule(timetable: Timetable): List<String> {
  val errors = mutableListOf<String>()

  for ((day, daySchedule) in timetable.schedule) {
    var totalSlots = 0

    for (entry in daySchedule) {
      if (entry == "FREE") {
        totalSlots += 1
        continue
      }

      val name = entry.removeSuffix("_LAB")
      val isLab = entry.endsWith("_LAB")

      if (name !in timetable.subjects) {
        errors.add("${day.longName()}: Subject \"$name\" not found in subjects")
      }

      if (isLab && totalSlots !in listOf(0, 3, 5)) {
        errors.add("${day.longName()}: LAB entry \"$entry\" can only be in slots 0, 3, or 5 (found in slot $totalSlots)")
      }

      val offset = if (isLab)
        when (totalSlots) {
          0 -> 3
          else -> 2
        }
      else 1

      totalSlots += offset
    }

    if (totalSlots != 7) {
      errors.add("${day.longName()}: Total slots should be 7, got $totalSlots")
    }
  }

  return errors
}


val UPDATE_TIMES: List<LocalTime>
  get() =
    ((SLOTS + LAB_SLOTS).map { it.start } + listOf(
      LocalTime.of(10, 25),
      LocalTime.of(10, 40),
      LocalTime.of(12, 40),
      LocalTime.of(13, 5),
      LocalTime.of(15, 40)
    )).sorted()

data class TimetableSlot(val start: LocalTime, val end: LocalTime) {
  fun containsTime(time: LocalTime): Boolean {
    return start <= time && time < end
  }

  override fun toString(): String {
    val formatter = DateTimeFormatter.ofPattern("h:mm")
    return "${formatter.format(start)}-${formatter.format(end)}"
  }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class Subject(
  val name: String,
  val code: String,
  val faculty: String,
  val shortName: String = name.split(" ").map{ e -> e[0] }.filter({ e -> e.isUpperCase() })
    .joinToString(separator = "")
)

@Serializable
data class TimetableSpec(
  val year: String,
  val section: String,
  val semester: String
) {
  override fun toString(): String {
    return "${this.year}_${this.section}_${this.semester}"
  }

  companion object {
    fun fromString(string: String): TimetableSpec {
      val sp = string.split("_")
      if (sp.size != 3)
        throw IllegalArgumentException("Invalid spec: $string")

      return TimetableSpec(sp[0], sp[1], sp[2])
    }
  }
}

data class TimetableDisplayEntry(
  val name: String,
  val shortName: String,
  val slot: TimetableSlot,
  val start: Int,
  val end: Int,
  val lab: Boolean
)

val FREE_SUBJECT = Subject("Free", "", "", "FREE")
val UNKNOWN_SUBJECT = Subject("⚠️ Unknown", "", "", "⚠️ UNK")

fun buildTimetableDisplay(
  day: DayOfWeek,
  timetable: Timetable,
  showFreePeriods: Boolean = true,
  showCompletedPeriods: Boolean = true
): List<TimetableDisplayEntry> {
  if (!timetable.schedule.containsKey(day))
    return emptyList()

  val times: MutableList<TimetableDisplayEntry> = mutableListOf()
  var i = 0
  val now = LocalTime.now()

  for (x in timetable.schedule[day] ?: listOf()) {
    val name = x.removeSuffix("_LAB")
    val isLab = x.endsWith("_LAB")

    val subject = when (name) {
      "FREE" -> FREE_SUBJECT
      in timetable.subjects -> timetable.subjects[name] ?: UNKNOWN_SUBJECT
      else -> UNKNOWN_SUBJECT
    }

    val offset = if (isLab)
      when (i) {
        0 -> 3
        else -> 2
      }
    else 1

    val slot = if (isLab)
      when (i) {
        0 -> LAB_SLOTS[0]
        3 -> LAB_SLOTS[1]
        5 -> LAB_SLOTS[2]
        else -> throw IllegalStateException()
      }
    else SLOTS[i]

    if ((showFreePeriods || subject != FREE_SUBJECT) && (showCompletedPeriods || slot.end > now))
      times.add(
        TimetableDisplayEntry(
          subject.name,
          subject.shortName,
          slot,
          i,
          i + offset - 1,
          isLab
        )
      )

    i += offset
  }

  return times
}