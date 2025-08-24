package town.amrita.timetable.utils

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

val DAYS = DayOfWeek.entries.toTypedArray()

val TODAY: DayOfWeek
  get() = LocalDate.now().dayOfWeek

fun DayOfWeek.shortName(): String {
  return this.getDisplayName(TextStyle.SHORT, Locale.getDefault())
}

fun DayOfWeek.longName(): String {
  return this.getDisplayName(TextStyle.FULL, Locale.getDefault())
}