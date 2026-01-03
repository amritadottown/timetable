package town.amrita.timetable.models

import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class TimetableDisplayEntry(
  val name: String,
  val shortName: String,
  val slot: TimetableSlot,
  val start: Int,
  val end: Int,
  val lab: Boolean
)

data class TimetableSlot(val start: LocalTime, val end: LocalTime) {
  fun containsTime(time: LocalTime): Boolean {
    return time in start..<end
  }

  override fun toString(): String {
    val formatter = DateTimeFormatter.ofPattern("h:mm")
    return "${formatter.format(start)}-${formatter.format(end)}"
  }
}

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

val UPDATE_TIMES: List<LocalTime>
  get() =
    ((SLOTS + LAB_SLOTS).map { it.start } + listOf(
      LocalTime.of(10, 25),
      LocalTime.of(10, 40),
      LocalTime.of(12, 40),
      LocalTime.of(13, 5),
      LocalTime.of(15, 40)
    )).sorted()

fun buildTimetableDisplay(
  day: DayOfWeek,
  timetable: Timetable,
  showFreePeriods: Boolean = true,
  config: Map<String, String>?
): List<TimetableDisplayEntry> {
  val rawSchedule = timetable.schedule[day] ?: return emptyList()

  val times: MutableList<TimetableDisplayEntry> = mutableListOf()

  val resolveCache = when (config) {
    null -> timetable.allPossibleValues
    else -> timetable.slots.mapValues { (_, slot) -> listOf(slot.resolve(config)) }
  }

  val daySchedule = rawSchedule.map { x ->
    resolveCache.getOrElse(x) { listOf(x) }
  }

  var i = 0
  while (i < 7) {
    val resolved = daySchedule[i]
    val isLab =
      resolved.any { it != "FREE" }
      && resolved.all { it.endsWith("_LAB") || it == "FREE" }
      && when (i) {
        0 -> resolved == daySchedule[1] && daySchedule[1] == daySchedule[2]
        3, 5 -> resolved == daySchedule[i + 1]
        else -> false
      }

    fun resolveSubject(name: String): Subject {
      return when (val trimmed = name.removeSuffix("_LAB")) {
        "FREE" -> Subject.FREE
        in timetable.subjects -> timetable.subjects[trimmed] ?: Subject.UNKNOWN
        else -> Subject.UNKNOWN
      }
    }

    val subject = when {
      resolved.size > 1 -> {
        val subjects = resolved.map { resolveSubject(it) }
        val displayName = subjects.joinToString(" / ") { it.shortName }
        Subject(displayName, subjects.joinToString(" / ") { it.code }, emptyList(), displayName)
      }
      else -> resolveSubject(resolved[0])
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

    if (showFreePeriods || subject != Subject.FREE)
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