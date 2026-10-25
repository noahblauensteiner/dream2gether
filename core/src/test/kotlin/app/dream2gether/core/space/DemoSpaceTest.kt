package app.dream2gether.core.space

import app.dream2gether.core.notes.CollectionNote
import app.dream2gether.core.notes.MomentNote
import app.dream2gether.core.notes.NightNote
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DemoSpaceTest {
    @Test
    fun `demo space has twelve nights and moments that read back and link up`() {
        val store = InMemorySpaceStore()
        DemoSpace.create(store)
        val nights = store.listFiles("sessions").map { NightNote.read(store.readText(it)!!) }
        val moments = store.listFiles("moments").map { MomentNote.read(store.readText(it)!!) }
        assertEquals(12, nights.size)
        assertEquals(12, moments.size)
        moments.forEach { m ->
            assertTrue(store.exists(SpaceLayout.nightNote(m.night)), "night for ${m.id}")
            assertTrue(m.categories.isNotEmpty(), "categories for ${m.id}")
        }
        nights.forEach { n -> assertTrue(store.exists(SpaceLayout.audio(n.id, "wav")), "audio for ${n.id}") }
        assertEquals(3, moments.count { it.star })
        val album = CollectionNote.read("best-food-2026", store.readText("collections/best-food-2026.md")!!)
        album.moments.forEach { assertTrue(store.exists(SpaceLayout.momentNote(it))) }
    }

    @Test
    fun `cards without photos get their category colour`() {
        val store = InMemorySpaceStore()
        DemoSpace.create(store)
        val ramen = store.readText("moments/2026-10/m-2026-10-07-ramen-n9qe.md")!!
        assertTrue(ramen.contains("cover: \"#F4A261\""), ramen)
    }

    @Test
    fun `silent wav has a valid header`() {
        val wav = DemoSpace.silentWav()
        assertEquals(44 + 16_000, wav.size)
        assertEquals("RIFF", wav.copyOfRange(0, 4).decodeToString())
        assertEquals("WAVE", wav.copyOfRange(8, 12).decodeToString())
    }
}
