package app.dream2gether.cli

import app.dream2gether.core.llm.LanguageModel
import app.dream2gether.core.llm.LlmRequest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import java.net.ServerSocket
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import kotlin.io.path.readLines

/** Where llama-server gets its model: a local GGUF file, or a Hugging Face repo it downloads once. */
sealed interface ModelSource {
    val label: String

    data class File(val path: Path) : ModelSource {
        override val label: String get() = path.fileName.toString().removeSuffix(".gguf")
    }

    data class HuggingFace(val repo: String) : ModelSource {
        override val label: String get() = repo.substringAfter('/')
    }
}

/**
 * llama.cpp's `llama-server` (OpenAI-style API on localhost). Either one we start for this run, or
 * one that's already running at a URL. Nothing leaves the machine: it listens on 127.0.0.1 only.
 */
class LlamaServer private constructor(
    private val baseUrl: URI,
    override val name: String,
    private val process: Process?,
) : LanguageModel, AutoCloseable {

    private val http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()

    override fun complete(request: LlmRequest): String {
        val req = HttpRequest.newBuilder(baseUrl.resolve("/v1/chat/completions"))
            .timeout(Duration.ofMinutes(COMPLETION_TIMEOUT_MIN))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(LlamaApi.chatBody(request).toString()))
            .build()
        val res = http.send(req, HttpResponse.BodyHandlers.ofString())
        check(res.statusCode() == 200) { "llama-server answered ${res.statusCode()}: ${res.body().take(500)}" }
        return LlamaApi.content(res.body())
    }

    override fun close() {
        val p = process ?: return
        p.descendants().forEach { it.destroy() }
        p.destroy()
        if (!p.waitFor(STOP_WAIT_S, java.util.concurrent.TimeUnit.SECONDS)) p.destroyForcibly()
    }

    companion object {
        private const val COMPLETION_TIMEOUT_MIN = 10L
        private const val STOP_WAIT_S = 5L
        private const val START_TIMEOUT_MIN = 20L
        private const val CONTEXT_TOKENS = "8192"

        /** Uses a server you already run, e.g. `llama-server -hf … --port 8080`. */
        fun connect(url: String, name: String = "llama-server"): LlamaServer {
            val base = URI.create(url.trimEnd('/') + "/")
            check(health(base) == 200) { "No ready llama-server at $url" }
            return LlamaServer(base, name, null)
        }

        /** Starts `llama-server` for [model] on a free local port and waits until it's loaded. */
        fun start(binary: String, model: ModelSource, onStatus: (String) -> Unit): LlamaServer {
            val port = ServerSocket(0).use { it.localPort }
            val log = Files.createTempFile("d2g-llama-server", ".log")
            val modelArgs = when (model) {
                is ModelSource.File -> listOf("-m", model.path.toString())
                is ModelSource.HuggingFace -> listOf("-hf", model.repo)
            }
            val cmd = listOf(binary) + modelArgs + listOf("--host", "127.0.0.1", "--port", "$port", "-c", CONTEXT_TOKENS)
            val process = try {
                ProcessBuilder(cmd).redirectErrorStream(true).redirectOutput(log.toFile()).start()
            } catch (e: java.io.IOException) {
                throw IllegalStateException("Can't run `$binary`. Install it with `brew install llama.cpp`.", e)
            }
            val server = LlamaServer(URI.create("http://127.0.0.1:$port/"), model.label, process)
            onStatus("Loading ${model.label}… (the first run with --hf downloads it)")
            val deadline = System.nanoTime() + Duration.ofMinutes(START_TIMEOUT_MIN).toNanos()
            while (true) {
                if (!process.isAlive) {
                    val tail = log.readLines().takeLast(15).joinToString("\n")
                    error("llama-server stopped while loading (log: $log):\n$tail")
                }
                if (runCatching { health(server.baseUrl) }.getOrNull() == 200) return server
                if (System.nanoTime() > deadline) {
                    server.close()
                    error("llama-server didn't get ready in $START_TIMEOUT_MIN min (log: $log)")
                }
                Thread.sleep(500)
            }
        }

        private fun health(base: URI): Int {
            val http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build()
            val req = HttpRequest.newBuilder(base.resolve("/health")).timeout(Duration.ofSeconds(5)).GET().build()
            return http.send(req, HttpResponse.BodyHandlers.discarding()).statusCode()
        }
    }
}

/** Request and response shapes of llama-server's `/v1/chat/completions`. */
object LlamaApi {
    fun chatBody(r: LlmRequest): JsonObject = buildJsonObject {
        putJsonArray("messages") {
            add(buildJsonObject { put("role", "system"); put("content", r.system) })
            add(buildJsonObject { put("role", "user"); put("content", r.user) })
        }
        put("temperature", r.temperature)
        put("max_tokens", r.maxTokens)
        // Answer directly: thinking only slows a small model down on an extraction task.
        put("reasoning_effort", "none")
        putJsonObject("chat_template_kwargs") { put("enable_thinking", false) }
        r.jsonSchema?.let { schema ->
            putJsonObject("response_format") {
                put("type", "json_schema")
                put("schema", schema)
            }
        }
    }

    fun content(responseBody: String): String =
        Json.parseToJsonElement(responseBody).jsonObject["choices"]!!.jsonArray[0]
            .jsonObject["message"]!!.jsonObject["content"]!!.jsonPrimitive.content
}
