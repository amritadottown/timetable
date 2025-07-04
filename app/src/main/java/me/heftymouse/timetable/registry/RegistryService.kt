package me.heftymouse.timetable.registry

import kotlinx.serialization.json.Json
import me.heftymouse.timetable.models.Timetable
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path


interface RegistryService {
  @GET("index.json")
  fun getRegistry(): Call<Registry>

  @GET("files/{program}/{year}/{sem}/{class}.json")
  fun getTimetable(
    @Path("program") program: String,
    @Path("year") year: String,
    @Path("sem") sem: String,
    @Path("class") classroom: String
  ): Call<Timetable>

  companion object {
    val instance: RegistryService by lazy {
      Retrofit.Builder()
        .baseUrl("https://timetable-registry.heftymausnik.workers.dev/")
        .addConverterFactory(Json.asConverterFactory("application/json; charset=UTF8".toMediaType()))
        .build()
        .create(RegistryService::class.java)
    }
  }
}