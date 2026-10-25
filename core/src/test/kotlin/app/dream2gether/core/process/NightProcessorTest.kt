package app.dream2gether.core.process

import app.dream2gether.core.extract.FakeLlm
import app.dream2gether.core.model.Member
import app.dream2gether.core.model.Space
import app.dream2gether.core.notes.MomentNote
import app.dream2gether.core.notes.NightNote
import app.dream2gether.core.space.InMemorySpaceStore
import app.dream2gether.core.space.SpaceInit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.UtcOffset
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class NightProcessorTest {
    private val answer = """{"moments":[{"title":"Ramen from scratch","emoji":"🍜","day_ref":"today","day_ref_end":"",
        "approx":false,"who":"noah","categories":["food"],"places":["home"],"people":[],"first_time":true,
        "story":"We made ramen.","quote":"","photo_hint":"","segments":[1]}],"dreams":[]}"""

    private fun store() = InMemorySpaceStore().also {
        SpaceInit.create(it, Space("Noah & Kim", listOf(Member("noah", "Noah"), Member("kim", "Kim")), timezone = "Europe/Vienna"))
    }

    private fun input(by: String = "noah") = NightProcessor.NightInput(
        recordedBy = by,
        device = "test",
        recordedAt = LocalDateTime(2026, 10, 24, 22, 41),
        utcOffset = UtcOffset(hours = 2),
        question = "What was your favourite moment today?",
        transcript = TypedChat.parse("Kim: Favourite moment?\nNoah: The ramen!\n"),
        durationS = 10,
        audioFile = null,
        sttModel = "typed",
    )

    @Test
    fun `writes a night note and its moment notes`() {
        val store = store()
        val r = NightProcessor(store, FakeLlm(answer), { "2026-10-24T23:00:00+02:00" }).process(input())
        assertEquals("sessions/2026-10/s-2026-10-24-2241-noah.md", r.nightPath)
        val night = NightNote.read(store.readText(r.nightPath)!!)
        assertEquals("fake-llm", night.processing?.llmModel)
        assertEquals("typed", night.processing?.sttModel)
        assertEquals(1, night.moments.size)
        assertEquals(2, night.transcript.size)
        val text = store.readText(r.nightPath)!!
        assertFalse(text.contains("![["), "typed chats have no audio embed")
        val moment = MomentNote.read(store.readText(r.momentPaths.single())!!)
        assertEquals("Ramen from scratch", moment.title)
        assertEquals(night.id, moment.night)
    }

    @Test
    fun `refuses to overwrite a night and unknown members`() {
        val store = store()
        val p = NightProcessor(store, FakeLlm(answer), { "now" })
        p.process(input())
        assertFailsWith<NightProcessor.AlreadyProcessed> { p.process(input()) }
        val e = assertFailsWith<IllegalArgumentException> { p.process(input(by = "alex")) }
        assertContains(e.message!!, "noah, kim")
    }

    @Test
    fun `typed chat skips blanks and comments`() {
        val segs = TypedChat.parse("# pretend chat\n\nKim: hi\n  \nNoah: hey\n")
        assertEquals(listOf("Kim: hi", "Noah: hey"), segs.map { it.text })
        assertEquals(5_000L, segs[1].startMs)
    }
}
