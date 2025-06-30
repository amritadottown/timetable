package me.heftymouse.timetable.models

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
  val shortName: String = name.split(" ").map({ e -> e[0] }).joinToString(separator = "")
)