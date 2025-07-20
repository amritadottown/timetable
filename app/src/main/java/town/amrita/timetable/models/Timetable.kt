package town.amrita.timetable.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@Serializable
data class Timetable(
  val subjects: HashMap<String, Subject>,
  val schedule: HashMap<String, Array<String>>
) {
  val slots: Array<String> = arrayOf(
    "8:10-9:00",
    "9:00-9:50",
    "9:50-10:40",
    "11:00-11:50",
    "11:50-12:40",
    "2:00-2:50",
    "2:50-3:40"
  )
  val labSlots: Array<String> = arrayOf(
    "8:10-10:25",
    "10:50-1:05",
    "1:25-3:40"
  )
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class Subject(
  val name: String,
  val code: String,
  val faculty: String,
  val shortName: String = name.split(" ").map({ e -> e[0] }).filter({ e -> e.isUpperCase() })
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
  val slot: String,
  val start: Int,
  val end: Int,
  val lab: Boolean
)

val FREE_SUBJECT = Subject("Free", "", "")
val UNKNOWN_SUBJECT = Subject("⚠️ Unknown", "", "")

fun buildTimetableDisplay(day: String, timetable: Timetable, showFreePeriods: Boolean = true): List<TimetableDisplayEntry> {
  if (!timetable.schedule.containsKey(day))
    return emptyList()

  val times: MutableList<TimetableDisplayEntry> = mutableListOf()
  var i = 0

  for (x in timetable.schedule[day] ?: arrayOf()) {
    val name = x.removeSuffix("_LAB")
    val isLab = x.endsWith("_LAB")

    val subject = when (name) {
      "FREE" -> if (showFreePeriods) FREE_SUBJECT else break
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
        0 -> timetable.labSlots[0]
        3 -> timetable.labSlots[1]
        5 -> timetable.labSlots[2]
        else -> "⚠️ UNKNOWN"
      }
    else timetable.slots[i]

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