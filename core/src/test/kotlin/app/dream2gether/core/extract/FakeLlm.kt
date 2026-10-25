package app.dream2gether.core.extract

import app.dream2gether.core.llm.LanguageModel
import app.dream2gether.core.llm.LlmRequest

/** Answers with canned replies in order (the last one repeats) and remembers what it was asked. */
class FakeLlm(private vararg val answers: String) : LanguageModel {
    override val name = "fake-llm"
    val requests = mutableListOf<LlmRequest>()

    override fun complete(request: LlmRequest): String {
        requests += request
        return answers[minOf(requests.size - 1, answers.size - 1)]
    }
}
