package town.amrita.timetable.registry

import kotlinx.serialization.json.Json
import town.amrita.timetable.models.Timetable
import town.amrita.timetable.models.TimetableSpec
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path


class RegistryService {
  interface RegistryServiceImpl {
    @GET("index.json")
    fun getRegistry(): Call<Registry>

    @GET("files/{year}/{section}/{sem}.json")
    fun getTimetableImpl(
      @Path("year") year: String,
      @Path("section") section: String,
      @Path("sem") semester: String
    ): Call<Timetable>
  }

  val impl: RegistryServiceImpl by lazy {
    Retrofit.Builder()
      .baseUrl("https://timetable-registry.amrita.town/")
      .addConverterFactory(Json.asConverterFactory("application/json; charset=UTF8".toMediaType()))
      .build()
      .create(RegistryServiceImpl::class.java)
  }

  fun getRegistry(): Call<Registry> = impl.getRegistry()
  fun getTimetable(spec: TimetableSpec) =
    impl.getTimetableImpl(spec.year, spec.section, spec.semester)

  companion object {
    val instance = RegistryService()
  }
}