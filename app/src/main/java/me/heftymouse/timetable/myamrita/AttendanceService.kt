package me.heftymouse.timetable.myamrita

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface MyAmritaService {

  @Headers(
    "accesstoken: ul",
    "authkey: qD66qY15l/3UBZF3FWQf6NiQpZFXco66RQcPJrMNt+hCbpXbVZlc+p+NIqdczk1a",
    "authorization: bearer guest",
    "token: ul",
    "version: 1",
    "x-api-key: f8c77cb7-d3f8-45fd-a4f7-292e44d34634"
  )
  @POST("student/getstudentotp")
  fun getStudentOtp(
    @Header("emailId") emailId: String,
    @Body body: GetStudentOtpRequest
  ): Call<GetStudentOtpResponse>

  @Headers(
    "accesstoken: ul",
    "authkey: qD6QY15l/3UBZf3FW0j6NiQpZFxco66RQcPJrMNt+HCbpXbVZlcP++N1Qdczk1a",
    "authorization: bearer guest",
    "token: ul",
    "version: 1",
    "x-api-key: f8c77cb7-d3f8-45fd-a4f7-292e44d34634"
  )
  @POST("student/getstudentaccess")
  fun getStudentAccess(
    @Header("emailId") emailId: String,
    @Body body: GetStudentAccessRequest
  ): Call<GetStudentAccessResponse>

  @Headers(
    "authkey: qD6QY15l/3UBZf3FW0j6NiQpZFxco66RQcPJrMNt+HCbpXbVZlcP++N1Qdczk1a",
    "authorization: bearer guest",
    "version: 1",
    "x-api-key: f8c77cb7-d3f8-45fd-a4f7-292e44d34634"
  )
  @POST("student/student-academic-term-list")
  fun getAcademicTerms(
    @Header("accesstoken") accessToken: String,
    @Header("token") otherToken: String,
    @Header("emailId") emailId: String,
    @Body body: GetAcademicTermsRequest
  ): Call<StandardResponse<Array<StudentTerm>>>
}
