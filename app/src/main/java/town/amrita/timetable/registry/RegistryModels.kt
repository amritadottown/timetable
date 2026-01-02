package town.amrita.timetable.registry

import kotlinx.serialization.Serializable

@Serializable
data class Registry(val version: Int, val timetables: RegistryYears)

typealias RegistryYears = Map<String, RegistrySemesters>
typealias RegistrySemesters = Map<String, List<String>>

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