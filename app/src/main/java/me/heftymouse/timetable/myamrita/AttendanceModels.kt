package me.heftymouse.timetable.myamrita

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class GetStudentOtpRequest(
  val auth_token: String
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class GetStudentOtpResponse(
  val time: String,
  val success: Boolean,
  val data: OtpData
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class OtpData(
  val key: String
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class GetStudentAccessRequest(
  val auth_token: String,
  val dev_key: String,
  val dev_otp: String
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class GetStudentAccessResponse(
  val time: String,
  val success: Boolean,
  val Token: String,
  val access_token: String,
  val access_token_expiry: String,
  val user: User
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class User(
  val std_reg_id: String,
  val roll_no: String,
  val amrita_mail: String,
  val std_nm: String,
  val gen_std_nm: String,
  val cmp_id: String,
  val hostel_std_reg_id: String
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class StandardResponse<T>(val time: String, val success: Boolean, val data: T)

@Serializable
data class GetAcademicTermsRequest(val roll_no: String)

@Serializable
enum class TermType {
  @SerialName("ODD")
  ODD,

  @SerialName("EVEN")
  EVEN
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class StudentTerm(
  @SerialName("academic_term_id") val termId: Int,
  @SerialName("term_year") val termYear: Int,
  @SerialName("term_type") val termType: TermType
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class StudentClass(
  @SerialName("course_code") val courseCode: String,
  val total: Int,
  val present: Int,
  val absent: Int,
  val duty: Int
)

