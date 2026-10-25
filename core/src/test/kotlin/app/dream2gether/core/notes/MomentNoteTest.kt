package app.dream2gether.core.notes

import app.dream2gether.core.model.Category
import app.dream2gether.core.model.Comment
import app.dream2gether.core.model.NoteExtras
import app.dream2gether.core.model.Section
import app.dream2gether.core.model.YamlValue
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class MomentNoteTest {
    @Test
    fun `writes the documented layout`() {
        val expected = """
            ---
            type: moment
            schema: dream2gether/moment@1
            id: m-2026-10-07-ramen-n9qe
            title: Ramen from scratch
            emoji: 🍜
            date: 2026-10-07
            date_end:
            date_confidence: exact
            who: both
            categories:
              - food
              - cozy
            places:
              - home
            people: []
            first_time: true
            star: true
            private: false
            night: "[[s-2026-10-07-2250-sam]]"
            audio_from: "01:02"
            audio_to: "02:14"
            photo_hint:
            generated_hash: 9f2c41d0
            tags:
              - moment
            ---

            # 🍜 Ramen from scratch

            Six hours of broth, two burnt eggs, one perfect bowl each. We ate on the living room floor
            because the table was still covered in flour.

            > "Six hours for one bowl of soup and I'd do it again tomorrow."

            ![[m-2026-10-07-ramen-n9qe-1.jpg]]

            ## Comments

            > [!comment] Alex · 8 Oct
            > ==Best soup of my life.== Next time: less salt.

        """.trimIndent()
        assertEquals(expected, MomentNote.write(Fixtures.ramen))
    }

    @Test
    fun `round-trips`() {
        val m = Fixtures.ramen.copy(
            comments = Fixtures.ramen.comments + Comment("Sam", "", "Two lines\nof comment"),
            extras = NoteExtras(
                properties = listOf("aliases" to YamlValue.Items(listOf("Soup night"))),
                sections = listOf(Section("Notes", "Recipe: [[ramen recipe]]")),
            ),
        )
        assertEquals(m, MomentNote.read(MomentNote.write(m)))
    }

    @Test
    fun `reads a note edited in Obsidian and keeps the edits when rewritten`() {
        val edited = """
            ---
            id: m-2026-10-07-ramen-n9qe
            title: Ramen from scratch (best ever)
            emoji: 🍜
            date: 2026-10-07
            date_confidence: exact
            categories: [food, cozy, us]
            star: true
            night: "[[s-2026-10-07-2250-sam]]"
            aliases:
              - Soup night
            ---

            # 🍜 Ramen from scratch

            Six hours of broth. ==Worth it.==

            Second paragraph we added later.

            > “Six hours for one bowl of soup”

            ![[m-2026-10-07-ramen-n9qe-1.jpg]]
            ![[m-2026-10-07-ramen-n9qe-2.jpg|300]]

            ## Comments

            > [!comment] Sam · 9 Oct
            > Agreed.
            > Next: pho.

            Some loose note under comments.

            ## Recipe

            - pork bones
            - 6 hours
        """.trimIndent().replace("\n", "\r\n")

        val m = MomentNote.read(edited)
        assertEquals("Ramen from scratch (best ever)", m.title)
        assertEquals(listOf(Category.FOOD, Category.COZY, Category.US), m.categories)
        assertEquals("Six hours of broth. ==Worth it.==\n\nSecond paragraph we added later.", m.story)
        assertEquals("Six hours for one bowl of soup", m.quote)
        assertEquals(listOf("m-2026-10-07-ramen-n9qe-1.jpg", "m-2026-10-07-ramen-n9qe-2.jpg"), m.photos)
        assertEquals(listOf(Comment("Sam", "9 Oct", "Agreed.\nNext: pho.")), m.comments)

        val rewritten = MomentNote.write(m)
        assertContains(rewritten, "aliases:\n  - Soup night")
        assertContains(rewritten, "Some loose note under comments.")
        assertContains(rewritten, "## Recipe\n\n- pork bones\n- 6 hours")
        assertEquals(m, MomentNote.read(rewritten))
    }
}
