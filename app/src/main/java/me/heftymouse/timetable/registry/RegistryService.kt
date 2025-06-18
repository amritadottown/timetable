package me.heftymouse.timetable.registry

import me.heftymouse.timetable.models.Timetable
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET


interface RegistryService {
  @GET("/index.json")
  fun getRegistry(): Call<Registry>

  @GET("/files/{id}")
  fun getTimetable(): Call<Timetable>

  companion object {
    lateinit var theService: RegistryService

    fun getInstance(): RegistryService {
      if (this::theService.isInitialized) return theService

      theService = Retrofit.Builder()
        .baseUrl("https://timetable-meta.amrita.town")
        .build()
        .create(RegistryService::class.java)
      return theService
    }
  }
}