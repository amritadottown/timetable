package me.heftymouse.timetable.registry

import kotlinx.serialization.Serializable

@Serializable
data class Registry(val version: Int, val timetables: RegistryCourses);

typealias RegistryCourses = Map<String, RegistryYears>
typealias RegistryYears = Map<String, RegistrySemesters>
typealias RegistrySemesters = Map<String, Array<String>>