package app.dream2gether.cli

import app.dream2gether.core.llm.LlmRequest
import com.sun.net.httpserver.HttpServer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.net.InetSocketAddress
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class LlamaServerTest {
    @Test
    fun `request asks for schema-constrained JSON without thinking`() {
        val schema = buildJsonObject { put("type", "object") }
        val body = LlamaApi.chatBody(LlmRequest("sys", "usr", schema))
        assertEquals("json_schema", body["response_format"]!!.jsonObject["type"]!!.jsonPrimitive.content)
        assertEquals(schema, body["response_format"]!!.jsonObject["schema"])
        assertEquals("none", body["reasoning_effort"]!!.jsonPrimitive.content)
        assertContains(body["messages"].toString(), "\"role\":\"system\"")
    }

    @Test
    fun `talks to a running server`() {
        var seenBody = ""
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/health") { ex -> ex.sendResponseHeaders(200, -1); ex.close() }
        server.createContext("/v1/chat/completions") { ex ->
            seenBody = ex.requestBody.readAllBytes().decodeToString()
            val out = """{"choices":[{"message":{"role":"assistant","content":"{\"moments\":[]}"}}]}""".toByteArray()
            ex.sendResponseHeaders(200, out.size.toLong())
            ex.responseBody.use { it.write(out) }
        }
        server.start()
        try {
            val llm = LlamaServer.connect("http://127.0.0.1:${server.address.port}")
            assertEquals("{\"moments\":[]}", llm.complete(LlmRequest("sys", "usr")))
            assertContains(Json.parseToJsonElement(seenBody).jsonObject["messages"].toString(), "\"content\":\"usr\"")
        } finally {
            server.stop(0)
        }
    }
}
