package me.heftymouse.timetable.registry

import kotlinx.serialization.Serializable

@Serializable
data class Registry(val version: Int, val timetables: Array<String>);