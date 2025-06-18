package me.heftymouse.timetable.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

val DAYS = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

val TODAY
  get() = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE"))


