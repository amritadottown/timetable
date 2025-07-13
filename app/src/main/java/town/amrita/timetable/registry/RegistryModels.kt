package town.amrita.timetable.registry

import kotlinx.serialization.Serializable

@Serializable
data class Registry(val version: Int, val timetables: RegistryYears)

typealias RegistryYears = Map<String, RegistrySemesters>
typealias RegistrySemesters = Map<String, List<String>>