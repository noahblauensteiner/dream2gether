package app.dream2gether.core.notes

import app.dream2gether.core.model.YamlValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FrontMatterTest {
    @Test
    fun `reads flow lists, Obsidian block lists, quotes and comments`() {
        val fm = FrontMatter.parse(
            """
            title: Ramen from scratch
            categories: [food, "cozy"]
            tags:
              - moment
              - favourite
            places:
            - home
            kind: album   # album | project
            audio_from: "01:02"
            night: "[[s-2026-10-07-2250-sam]]"
            quote: 'It''s fine'
            empty:
            # a comment line
            """.trimIndent(),
        )
        assertEquals("Ramen from scratch", fm.string("title"))
        assertEquals(listOf("food", "cozy"), fm.list("categories"))
        assertEquals(listOf("moment", "favourite"), fm.list("tags"))
        assertEquals(listOf("home"), fm.list("places"))
        assertEquals("album", fm.string("kind"))
        assertEquals("01:02", fm.string("audio_from"))
        assertEquals("s-2026-10-07-2250-sam", fm.link("night"))
        assertEquals("It's fine", fm.string("quote"))
        assertNull(fm.string("empty"))
        assertEquals(emptyList(), fm.list("empty"))
    }

    @Test
    fun `tricky strings survive a write and read`() {
        val tricky = listOf(
            "Grandma's \"Rosi\"", "01:02", "true", "[[x]]", "a: b", "# hash", "- dash", "line\nbreak",
            "2026-10-07", "plain words", "ends with colon:", " padded ",
        )
        val w = FrontMatterWriter()
        tricky.forEachIndexed { i, s -> w.string("k$i", s) }
        w.list("list", tricky)
        val fm = FrontMatter.parse(w.build())
        tricky.forEachIndexed { i, s -> assertEquals(s, fm.string("k$i"), "k$i") }
        assertEquals(tricky, fm.list("list"))
    }

    @Test
    fun `quotes only what YAML would misread`() {
        assertEquals("Ramen from scratch", FrontMatterWriter.quoteIfNeeded("Ramen from scratch"))
        assertEquals("🍜", FrontMatterWriter.quoteIfNeeded("🍜"))
        assertEquals("\"01:02\"", FrontMatterWriter.quoteIfNeeded("01:02"))
        assertEquals("\"[[x]]\"", FrontMatterWriter.quoteIfNeeded("[[x]]"))
        assertEquals("\"yes\"", FrontMatterWriter.quoteIfNeeded("yes"))
    }

    @Test
    fun `unknown keys are reported as extras in order`() {
        val fm = FrontMatter.parse("id: x\naliases:\n  - Soup night\ncssclasses: wide\n")
        assertEquals(
            listOf("aliases" to YamlValue.Items(listOf("Soup night")), "cssclasses" to YamlValue.Scalar("wide")),
            fm.extras(setOf("id")),
        )
    }
}
