package app.dream2gether.core.llm

import kotlinx.serialization.json.JsonObject

/** A local language model. Implementations: llama.cpp on the Mac and Pi, LiteRT-LM on Android. */
interface LanguageModel {
    /** Recorded in night notes, so we know what produced a result. */
    val name: String

    /** Returns the model's text answer; with [LlmRequest.jsonSchema] set, that's JSON matching it. */
    fun complete(request: LlmRequest): String
}

data class LlmRequest(
    val system: String,
    val user: String,
    val jsonSchema: JsonObject? = null,
    val maxTokens: Int = 2048,
    val temperature: Double = 0.2,
)
