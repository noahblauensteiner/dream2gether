package app.dream2gether.core.space

import app.dream2gether.core.model.Member
import app.dream2gether.core.model.Space
import app.dream2gether.core.notes.SpaceNote
import org.yaml.snakeyaml.Yaml
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SpaceInitTest {
    private val space = Space("Noah & Kim", listOf(Member("noah", "Noah"), Member("kim", "Kim")), timezone = "Europe/Vienna")

    @Test
    fun `creates space note, folders and starter kit`() {
        val store = InMemorySpaceStore()
        SpaceInit.create(store, space)
        assertEquals(space, SpaceNote.read(store.readText("space.md")!!))
        SpaceLayout.FOLDERS.forEach { assertTrue(store.exists(it), it) }
        listOf(StarterKit.START_HERE, StarterKit.MOMENTS_BASE, StarterKit.NIGHTS_BASE, StarterKit.CSS_SNIPPET, StarterKit.APPEARANCE)
            .forEach { assertTrue(store.exists(it), it) }
        assertTrue(store.readText(StarterKit.START_HERE)!!.contains("Noah & Kim"))
    }

    @Test
    fun `refuses to overwrite an existing space`() {
        val store = InMemorySpaceStore()
        SpaceInit.create(store, space)
        assertFailsWith<SpaceInit.AlreadyASpace> { SpaceInit.create(store, space) }
    }

    @Test
    fun `keeps existing Obsidian appearance settings`() {
        val store = InMemorySpaceStore()
        store.writeText(StarterKit.APPEARANCE, "{\"theme\":\"moonstone\"}")
        SpaceInit.create(store, space)
        assertEquals("{\"theme\":\"moonstone\"}", store.readText(StarterKit.APPEARANCE))
    }

    @Test
    fun `base files are valid YAML with the expected views`() {
        val moments = Yaml().load<Map<String, Any>>(StarterKit.momentsBase)
        @Suppress("UNCHECKED_CAST")
        val views = moments["views"] as List<Map<String, Any>>
        assertEquals(listOf("All moments", "By month", "Starred ⭐", "Table"), views.map { it["name"] })
        assertEquals("note.cover", views[0]["image"])
        val nights = Yaml().load<Map<String, Any>>(StarterKit.nightsBase)
        assertTrue("views" in nights && "filters" in nights)
    }

    @Test
    fun `layout puts files in month folders`() {
        assertEquals(
            "moments/2026-10/m-2026-10-07-ramen-n9qe.md",
            SpaceLayout.momentNote(app.dream2gether.core.model.MomentId("m-2026-10-07-ramen-n9qe")),
        )
        assertEquals(
            "audio/2026-10/s-2026-10-07-2250-sam.m4a",
            SpaceLayout.audio(app.dream2gether.core.model.NightId("s-2026-10-07-2250-sam"), "m4a"),
        )
    }
}
