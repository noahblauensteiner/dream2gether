package app.dream2gether.cli

import com.github.ajalt.clikt.testing.test
import java.nio.file.Files
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class CliTest {
    @Test
    fun `init-space creates a vault`() {
        val dir = Files.createTempDirectory("d2g").resolve("space")
        val r = d2g().test(listOf("init-space", dir.toString(), "--name", "Noah & Kim", "--member", "noah:Noah", "--member", "kim:Kim", "--timezone", "Europe/Vienna"))
        assertEquals(0, r.statusCode, r.output)
        assertContains(dir.resolve("space.md").readText(), "member_names:\n  - Noah\n  - Kim")
        assertTrue(dir.resolve("Moments.base").exists())
    }

    @Test
    fun `init-space refuses an existing space and bad member ids`() {
        val dir = Files.createTempDirectory("d2g")
        val args = listOf("init-space", dir.toString(), "--name", "X", "--member", "noah:Noah")
        assertEquals(0, d2g().test(args).statusCode)
        val again = d2g().test(args)
        assertNotEquals(0, again.statusCode)
        assertContains(again.output, "already a space")
        val bad = d2g().test(listOf("init-space", Files.createTempDirectory("d2g").toString(), "--name", "X", "--member", "Noah Smith"))
        assertNotEquals(0, bad.statusCode)
    }

    @Test
    fun `demo fills an empty folder and refuses a non-empty one`() {
        val dir = Files.createTempDirectory("d2g").resolve("demo")
        assertEquals(0, d2g().test(listOf("demo", dir.toString())).statusCode)
        assertTrue(dir.resolve("moments/2026-10/m-2026-10-07-ramen-n9qe.md").exists())
        assertTrue(dir.resolve("audio/2026-10/s-2026-10-07-2250-sam.wav").exists())
        assertNotEquals(0, d2g().test(listOf("demo", dir.toString())).statusCode)
    }

    @Test
    fun `file store can't escape the space`() {
        val store = FileSpaceStore(Files.createTempDirectory("d2g"))
        val e = runCatching { store.writeText("../evil.md", "x") }.exceptionOrNull()
        assertTrue(e is IllegalArgumentException)
    }
}

class ProcessTranscriptTest {
    private val answer = """{"moments":[{"title":"Ramen from scratch","emoji":"🍜","day_ref":"weekday:wednesday",
        "day_ref_end":"","approx":false,"who":"noah","categories":["food","cozy"],"places":["home"],"people":[],
        "first_time":true,"story":"We made ramen for six hours.","quote":"","photo_hint":"","segments":[1]}],
        "dreams":[{"text":"Learn to make pho","segments":[2]}]}"""

    private val fake: ModelOpener = { _, _ ->
        object : app.dream2gether.core.llm.LanguageModel {
            override val name = "fake"
            override fun complete(request: app.dream2gether.core.llm.LlmRequest) = answer
        }
    }

    @Test
    fun `typed chat becomes a night and moments`() {
        val dir = Files.createTempDirectory("d2g").resolve("space")
        d2g().test(listOf("init-space", dir.toString(), "--name", "Noah & Kim", "--member", "noah:Noah", "--member", "kim:Kim", "--timezone", "Europe/Vienna"))
        val chat = Files.createTempFile("chat", ".txt")
        Files.writeString(chat, "Kim: Favourite moment?\nNoah: The ramen on Wednesday!\nKim: We should learn pho.\n")

        val r = d2g(fake).test(listOf("process-transcript", chat.toString(), "--space", dir.toString(), "--by", "noah", "--at", "2026-10-24 22:41"))
        assertEquals(0, r.statusCode, r.output)
        assertContains(r.output, "🍜 Ramen from scratch  (Wed 21 Oct)")
        assertContains(r.output, "💭 Learn to make pho")
        val night = dir.resolve("sessions/2026-10/s-2026-10-24-2241-noah.md").readText()
        assertContains(night, "recorded_at: 2026-10-24T22:41:00+02:00")
        assertContains(night, "llm_model: fake")
        val moments = Files.list(dir.resolve("moments/2026-10")).use { it.toList() }
        assertEquals(1, moments.size)
        assertContains(moments[0].readText(), "date: 2026-10-21")

        val again = d2g(fake).test(listOf("process-transcript", chat.toString(), "--space", dir.toString(), "--by", "noah", "--at", "2026-10-24 22:41"))
        assertNotEquals(0, again.statusCode)
        assertContains(again.output, "already exists")
    }

    @Test
    fun `explains how to pick a model`() {
        val dir = Files.createTempDirectory("d2g").resolve("space")
        d2g().test(listOf("init-space", dir.toString(), "--name", "N", "--member", "noah:Noah"))
        val chat = Files.createTempFile("chat", ".txt")
        Files.writeString(chat, "Noah: hi\n")
        val r = d2g().test(listOf("process-transcript", chat.toString(), "--space", dir.toString(), "--by", "noah"), envvars = emptyMap())
        assertNotEquals(0, r.statusCode)
        assertContains(r.output, "--hf ggml-org/gemma-3-12b-it-GGUF")
    }
}
