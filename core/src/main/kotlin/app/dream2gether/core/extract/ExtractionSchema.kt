package app.dream2gether.core.extract

import app.dream2gether.core.model.Category
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/** What the model returns for one chunk of transcript. Defaults keep half-filled answers usable. */
@Serializable
data class ExtractionOutput(
    val moments: List<ExtractedMoment> = emptyList(),
    val dreams: List<ExtractedDream> = emptyList(),
)

@Serializable
data class ExtractedMoment(
    val title: String = "",
    val emoji: String = "",
    @SerialName("day_ref") val dayRef: String = "unknown",
    @SerialName("day_ref_end") val dayRefEnd: String = "",
    val approx: Boolean = false,
    val who: String = "both",
    val categories: List<String> = emptyList(),
    val places: List<String> = emptyList(),
    val people: List<String> = emptyList(),
    @SerialName("first_time") val firstTime: Boolean = false,
    val story: String = "",
    val quote: String = "",
    @SerialName("photo_hint") val photoHint: String = "",
    val segments: List<Int> = emptyList(),
)

@Serializable
data class ExtractedDream(val text: String = "", val segments: List<Int> = emptyList())

/** JSON schema the model's output is forced into (constrained decoding). */
object ExtractionSchema {
    const val MAX_MOMENTS = 8
    private const val MAX_DREAMS = 5

    fun build(memberIds: List<String>): JsonObject = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("moments") {
                put("type", "array")
                put("maxItems", MAX_MOMENTS)
                put("items", momentSchema(memberIds))
            }
            putJsonObject("dreams") {
                put("type", "array")
                put("maxItems", MAX_DREAMS)
                putJsonObject("items") {
                    put("type", "object")
                    putJsonObject("properties") {
                        putJsonObject("text") { put("type", "string") }
                        put("segments", intArray())
                    }
                    required("text", "segments")
                    put("additionalProperties", false)
                }
            }
        }
        required("moments", "dreams")
        put("additionalProperties", false)
    }

    private fun momentSchema(memberIds: List<String>) = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("title") { put("type", "string") }
            putJsonObject("emoji") { put("type", "string") }
            putJsonObject("day_ref") { put("type", "string"); put("pattern", DateRef.PATTERN) }
            putJsonObject("day_ref_end") { put("type", "string") }
            putJsonObject("approx") { put("type", "boolean") }
            putJsonObject("who") { put("enum", enumOf(memberIds + "both")) }
            putJsonObject("categories") {
                put("type", "array")
                put("maxItems", 3)
                putJsonObject("items") { put("enum", enumOf(Category.entries.map { it.key })) }
            }
            put("places", stringArray(5))
            put("people", stringArray(5))
            putJsonObject("first_time") { put("type", "boolean") }
            putJsonObject("story") { put("type", "string") }
            putJsonObject("quote") { put("type", "string") }
            putJsonObject("photo_hint") { put("type", "string") }
            put("segments", intArray())
        }
        required(
            "title", "emoji", "day_ref", "day_ref_end", "approx", "who", "categories", "places", "people",
            "first_time", "story", "quote", "photo_hint", "segments",
        )
        put("additionalProperties", false)
    }

    private fun enumOf(values: List<String>) = buildJsonArray { values.forEach { add(it) } }

    private fun stringArray(max: Int) = buildJsonObject {
        put("type", "array")
        put("maxItems", max)
        putJsonObject("items") { put("type", "string") }
    }

    private fun intArray() = buildJsonObject {
        put("type", "array")
        putJsonObject("items") { put("type", "integer"); put("minimum", 0) }
    }

    private fun kotlinx.serialization.json.JsonObjectBuilder.required(vararg keys: String) =
        put("required", JsonArray(keys.map(::JsonPrimitive)))
}
