package app.dream2gether.core.notes

import app.dream2gether.core.model.Collection
import app.dream2gether.core.model.CollectionKind
import app.dream2gether.core.model.Member
import app.dream2gether.core.model.MomentId
import app.dream2gether.core.model.NoteExtras
import app.dream2gether.core.model.Space
import kotlin.test.Test
import kotlin.test.assertEquals

class SpaceAndCollectionNoteTest {
    @Test
    fun `space round-trips, keeping free text`() {
        val s = Space(
            name = "Alex & Sam",
            members = listOf(Member("alex", "Alex"), Member("sam", "Sam")),
            timezone = "Europe/Vienna",
            glossaryPeople = listOf("Mila", "Grandma Rosi"),
            glossaryPlaces = listOf("farmers' market"),
            extras = NoteExtras(text = "Our little archive."),
        )
        assertEquals(s, SpaceNote.read(SpaceNote.write(s)))
    }

    @Test
    fun `collection round-trips and keeps moment order`() {
        val c = Collection(
            fileName = "best-food-2026",
            kind = CollectionKind.ALBUM,
            title = "🍝 Best food of 2026",
            cover = "m-2026-10-07-ramen-n9qe-1.jpg",
            intro = "Everything we loved eating this year.",
            moments = listOf(MomentId("m-2026-10-07-ramen-n9qe"), MomentId("m-2026-10-03-chestnuts-p8nc")),
            extras = NoteExtras(text = "Maybe print this one."),
        )
        assertEquals(c, CollectionNote.read("best-food-2026", CollectionNote.write(c)))
    }

    @Test
    fun `collection reads embeds with aliases and block refs`() {
        val text = "---\nkind: project\ntitle: Speech\n---\n\n# Speech\n\n![[m-2026-10-07-ramen-n9qe|small]]\n![[m-2026-10-03-chestnuts-p8nc#^quote]]\n"
        val c = CollectionNote.read("speech", text)
        assertEquals(listOf("m-2026-10-07-ramen-n9qe", "m-2026-10-03-chestnuts-p8nc"), c.moments.map { it.value })
        assertEquals(CollectionKind.PROJECT, c.kind)
    }
}
