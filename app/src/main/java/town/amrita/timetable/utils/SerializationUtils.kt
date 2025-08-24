package town.amrita.timetable.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object LocalTimeSerializer : KSerializer<LocalTime> {
  override val descriptor: SerialDescriptor
    get() = PrimitiveSerialDescriptor("LocalTimeSerializer", PrimitiveKind.STRING)

  override fun serialize(
    encoder: Encoder,
    value: LocalTime
  ) {
    encoder.encodeString(DateTimeFormatter.ISO_TIME.format(value))
  }

  override fun deserialize(decoder: Decoder): LocalTime {
    return LocalTime.parse(decoder.decodeString(), DateTimeFormatter.ISO_TIME)
  }
}

object DayOfWeekSerializer : KSerializer<DayOfWeek> {
  override val descriptor: SerialDescriptor
    get() = PrimitiveSerialDescriptor("LocalTimeSerializer", PrimitiveKind.STRING)

  override fun serialize(
    encoder: Encoder,
    value: DayOfWeek
  ) {
    encoder.encodeString(value.longName())
  }

  override fun deserialize(decoder: Decoder): DayOfWeek {
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val day = decoder.decodeString()
    val dayIndex = days.indexOf(day)
    if (dayIndex == -1)
      throw NoSuchElementException("\"$day\" not a valid day of week")
    return DayOfWeek.of(dayIndex + 1)
  }
}