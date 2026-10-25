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
