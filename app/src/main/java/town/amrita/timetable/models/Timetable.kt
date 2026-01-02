package town.amrita.timetable.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import town.amrita.timetable.utils.DayOfWeekSerializer
import town.amrita.timetable.utils.cartesianProduct
import town.amrita.timetable.utils.longName
import java.time.DayOfWeek

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class Timetable(
  val subjects: Map<String, Subject>,
  val config: Map<String, ConfigOption> = emptyMap(),
  val slots: Map<String, Slot> = emptyMap(),
  val schedule: Map<@Serializable(with = DayOfWeekSerializer::class) DayOfWeek, List<String>>
) {
  val allPossibleValues: Map<String, List<String>> by lazy {
    slots.mapValues { (name, slot) ->
      val explicitValues = slot.choices.map { it.value }.distinct()
      if ("FREE" in explicitValues)
        return@mapValues explicitValues

      for (i in slot.match.indices) {
        val mentionedOptions = slot.choices.map { it.pattern[i] }.toSet()
        val e = config[slot.match[i]]?.values?.map { it.id }?.toSet()
        if (mentionedOptions != e)
          return@mapValues explicitValues + "FREE"
        else if (slot.match.size == 1)
          return@mapValues explicitValues
      }

      // this is an expensive-looking bruteforce
      // i'm hoping that most of the time it won't reach this
      // and either way, the config space is likely small enough for it to not matter
      val listOfOptions = config.values.map { it.values.map { it.id } }
      for (thing in cartesianProduct(listOfOptions)) {
        if (slot.resolve(thing) == "FREE")
          return@mapValues explicitValues + "FREE"
      }

      return@mapValues explicitValues
    }
  }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class Subject(
  val name: String,
  val code: String,
  val faculty: List<String>,
  val shortName: String = name.split(" ").map{ e -> e[0] }.filter({ e -> e.isUpperCase() })
    .joinToString(separator = "")
)  {
  companion object {
    val FREE = Subject("Free", "", emptyList(), "FREE")
    val UNKNOWN = Subject("⚠️ Unknown", "", emptyList(), "⚠️ UNK")
  }
}

@Serializable
data class ConfigOption(
  val label: String,
  val values: List<ConfigValue>
)

@Serializable
data class ConfigValue(
  val id: String,
  val label: String
)

@Serializable(with = SlotSerializer::class)
data class Slot(
  val match: List<String>,
  val choices: List<SlotChoice>
) {
  fun resolve(config: Map<String, String>): String {
    val matchValues = match.map(config::getValue)
    return resolve(matchValues)
  }

  fun resolve(matchValues: List<String>): String {
    return choices.firstOrNull { choice ->
      choice.pattern.indices.all { i ->
        choice.pattern[i] == "*" || choice.pattern[i] == matchValues[i]
      }
    }?.value ?: "FREE"
  }
}

@Serializable
data class SlotChoice(
  val pattern: List<String>,
  val value: String
)

object SlotSerializer : KSerializer<Slot> {
  override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Slot")

  override fun deserialize(decoder: Decoder): Slot {
    require(decoder is JsonDecoder)
    val element = decoder.decodeJsonElement().jsonObject

    val matchElement = element["match"]!!
    val choicesElement = element["choices"]!!

    val isSimple = matchElement is JsonPrimitive

    return if (isSimple) {
      // Simple format: { match: "configKey", choices: { "id1": "subject1", ... } }
      val match = matchElement.jsonPrimitive.content
      val choices = choicesElement.jsonObject.map { (id, value) ->
        SlotChoice(
          pattern = listOf(id),
          value = value.jsonPrimitive.content
        )
      }
      Slot(match = listOf(match), choices = choices)
    } else {
      // Complex format: already in the right shape
      val match = matchElement.jsonArray.map { it.jsonPrimitive.content }
      val choices = choicesElement.jsonArray.map { choice ->
        val obj = choice.jsonObject
        SlotChoice(
          pattern = obj["pattern"]!!.jsonArray.map { it.jsonPrimitive.content },
          value = obj["value"]!!.jsonPrimitive.content
        )
      }
      Slot(match = match, choices = choices)
    }
  }

  override fun serialize(encoder: Encoder, value: Slot) {
    require(encoder is JsonEncoder)
    val obj = buildJsonObject {
      put("match", JsonArray(value.match.map { JsonPrimitive(it) }))
      put("choices", JsonArray(value.choices.map { choice ->
        buildJsonObject {
          put("pattern", JsonArray(choice.pattern.map { JsonPrimitive(it) }))
          put("value", JsonPrimitive(choice.value))
        }
      }))
    }
    encoder.encodeJsonElement(obj)
  }
}

fun Timetable.validateSubjectReference(ref: String): Boolean {
  return when {
    ref in subjects -> true
    ref.removeSuffix("_LAB") in subjects -> true
    else -> false
  }
}

fun Timetable.validate(): List<String> {
  val timetable = this
  val errors = mutableListOf<String>()

  // 1. Validate subject keys don't end with _LAB
  for (key in timetable.subjects.keys) {
    if (key.endsWith("_LAB")) {
      errors.add("Subject key \"$key\" should not end with _LAB (reserved for schedule)")
    }
  }

  // 2. Validate config value ID uniqueness
  for ((configKey, configOption) in timetable.config) {
    val ids = configOption.values.map { it.id }
    if (ids.size != ids.toSet().size) {
      errors.add("Config \"$configKey\" has duplicate value IDs")
    }
  }

  // 3. Validate slots
  for ((slotName, slot) in timetable.slots) {
    // Validate match references
    for (matchKey in slot.match) {
      if (matchKey !in timetable.config) {
        errors.add("Slot \"$slotName\" references non-existent config key \"$matchKey\"")
      }
    }

    // Validate patterns and choices
    for (choice in slot.choices) {
      // Pattern length validation
      if (choice.pattern.size != slot.match.size) {
        errors.add("Slot \"$slotName\" pattern length (${choice.pattern.size}) doesn't match match length (${slot.match.size})")
      }

      // Validate pattern values are valid config IDs or wildcards
      for (i in choice.pattern.indices) {
        val patternValue = choice.pattern[i]
        if (patternValue == "*") continue

        val configKey = slot.match.getOrNull(i) ?: continue
        val configOption = timetable.config[configKey]
        if (configOption != null) {
          val validIds = configOption.values.map { it.id }
          if (patternValue !in validIds) {
            errors.add("Slot \"$slotName\" pattern value \"$patternValue\" is not a valid ID for config \"$configKey\" (valid: ${validIds.joinToString(", ")})")
          }
        }
      }

      // Validate output value references valid subject
      if (choice.value != "FREE" && !timetable.validateSubjectReference(choice.value)) {
        errors.add("Slot \"$slotName\" pattern [${choice.pattern.joinToString(", ")}] references invalid subject \"${choice.value}\"")
      }
    }
  }

  // 4. Validate schedule references
  for ((day, periods) in timetable.schedule) {
    for (entry in periods) {
      if (entry == "FREE") continue
      if (entry in timetable.slots) continue
      if (timetable.validateSubjectReference(entry)) continue

      errors.add("${day.longName()}: Entry \"$entry\" is not a valid subject, slot, or FREE")
    }
  }

  return errors
}
