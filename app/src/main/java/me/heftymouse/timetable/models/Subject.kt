package me.heftymouse.timetable.models

import kotlinx.serialization.Serializable

@Serializable
data class Subject(val name: String, val code: String, val faculty: String)