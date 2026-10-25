package app.dream2gether.core.notes

import app.dream2gether.core.model.NoteExtras
import app.dream2gether.core.model.Section
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class NightNoteTest {
    @Test
    fun `round-trips, with segment ends derived from the next start`() {
        val n = Fixtures.night.copy(extras = NoteExtras(sections = listOf(Section("Notes", "Rain all night."))))
        val text = NightNote.write(n, "Sam")
        assertContains(text, "recorded_at: 2026-10-07T22:50:12+02:00\n")
        assertContains(text, "# 🌙 Wed 7 Oct · 10 min · recorded by Sam")
        assertContains(text, "- [[m-2026-10-07-ramen-n9qe|🍜 Ramen from scratch]] · 01:02")
        assertContains(text, "- 💭 Learn to make proper pho · 07:31")
        assertContains(text, "**[00:03]** Easy. The ramen.")
        assertEquals(n, NightNote.read(text))
    }

    @Test
    fun `reads language tags and hand-wrapped transcript lines`() {
        val text = NightNote.write(Fixtures.night.copy(transcript = emptyList()), "Sam") + """

            ## Transcript

            **[00:00]** We went to the markt
            and it was so nice.
            **[00:04 · nl]** Zo gezellig.
        """.trimIndent()
        val segments = NightNote.read(text).transcript
        assertEquals("We went to the markt and it was so nice.", segments[0].text)
        assertEquals(4_000, segments[0].endMs)
        assertEquals("nl", segments[1].language)
        assertEquals(612_000, segments[1].endMs)
    }

    @Test
    fun `a fresh recording without processing round-trips`() {
        val bare = Fixtures.night.copy(
            status = app.dream2gether.core.model.NightStatus.RECORDED,
            processing = null, moments = emptyList(), dreams = emptyList(), transcript = emptyList(),
        )
        assertEquals(bare, NightNote.read(NightNote.write(bare, "Sam")))
    }
}
