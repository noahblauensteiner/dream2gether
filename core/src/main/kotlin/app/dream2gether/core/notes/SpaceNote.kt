package app.dream2gether.core.notes

import app.dream2gether.core.model.Member
import app.dream2gether.core.model.NoteExtras
import app.dream2gether.core.model.Space

/** Reads and writes `space.md` (docs/session-file-format.md#spacemd). */
object SpaceNote {
    const val SCHEMA = "dream2gether/space@1"
    const val FILE_NAME = "space.md"
    private val KNOWN = setOf(
        "type", "schema", "name", "members", "member_names", "language", "timezone",
        "glossary_people", "glossary_places",
    )

    fun write(s: Space): String {
        val fm = FrontMatterWriter()
            .string("type", "space")
            .string("schema", SCHEMA)
            .string("name", s.name)
            .list("members", s.members.map { it.id })
            .list("member_names", s.members.map { it.name })
            .string("language", s.language)
            .string("timezone", s.timezone)
            .list("glossary_people", s.glossaryPeople)
            .list("glossary_places", s.glossaryPlaces)
        s.extras.properties.forEach { (k, v) -> fm.value(k, v) }
        val body = StringBuilder("# ").append(s.name).append(" 🌙\n")
        if (s.extras.text.isNotBlank()) body.append('\n').append(s.extras.text).append('\n')
        body.appendSections(s.extras.sections)
        return NoteFile.render(fm.build(), body.toString())
    }

    fun read(text: String): Space {
        val note = NoteFile.parse(text)
        val fm = note.frontMatter
        val (preamble, sections) = splitSections(note.body)
        val ids = fm.list("members")
        val names = fm.list("member_names")
        val freeText = preamble.lines().dropWhile { it.isBlank() || it.startsWith("# ") }.joinToString("\n").trim()
        return Space(
            name = fm.string("name").orEmpty(),
            members = ids.mapIndexed { i, id -> Member(id, names.getOrElse(i) { id }) },
            language = fm.string("language") ?: "en",
            timezone = fm.string("timezone") ?: "UTC",
            glossaryPeople = fm.list("glossary_people"),
            glossaryPlaces = fm.list("glossary_places"),
            extras = NoteExtras(properties = fm.extras(KNOWN), sections = sections, text = freeText),
        )
    }
}
