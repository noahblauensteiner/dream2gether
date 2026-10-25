package app.dream2gether.core.notes

import app.dream2gether.core.model.Collection
import app.dream2gether.core.model.CollectionKind
import app.dream2gether.core.model.MomentId
import app.dream2gether.core.model.NoteExtras

/** Reads and writes collection notes (docs/session-file-format.md#collections-albums-projects-artworks). */
object CollectionNote {
    const val SCHEMA = "dream2gether/collection@1"
    private val KNOWN = setOf("type", "schema", "kind", "title", "cover")
    private val EMBED = Regex("""^!\[\[(m-[^\]|#]+)(?:[#|][^\]]*)?\]\]$""")

    fun write(c: Collection): String {
        val fm = FrontMatterWriter()
            .string("type", "collection")
            .string("schema", SCHEMA)
            .string("kind", c.kind.key)
            .string("title", c.title)
            .link("cover", c.cover)
        c.extras.properties.forEach { (k, v) -> fm.value(k, v) }
        val body = StringBuilder("# ").append(c.title).append("\n\n")
        if (c.intro.isNotBlank()) body.append(c.intro.trim()).append("\n\n")
        c.moments.forEach { body.append("![[").append(it.value).append("]]\n") }
        if (c.extras.text.isNotBlank()) body.append('\n').append(c.extras.text.trim()).append('\n')
        body.appendSections(c.extras.sections)
        return NoteFile.render(fm.build(), body.toString())
    }

    /** [fileName] is the note's file name without `.md`. */
    fun read(fileName: String, text: String): Collection {
        val note = NoteFile.parse(text)
        val fm = note.frontMatter
        val (preamble, sections) = splitSections(note.body)
        val intro = StringBuilder()
        val after = StringBuilder()
        val moments = mutableListOf<MomentId>()
        for (line in preamble.lines()) {
            val embed = EMBED.matchEntire(line.trim())
            when {
                embed != null -> moments += MomentId(embed.groupValues[1])
                line.startsWith("# ") && intro.isBlank() && moments.isEmpty() -> Unit
                moments.isEmpty() -> intro.append(line).append('\n')
                else -> after.append(line).append('\n')
            }
        }
        return Collection(
            fileName = fileName,
            kind = CollectionKind.fromKey(fm.string("kind") ?: "other"),
            title = fm.string("title") ?: fileName,
            cover = fm.link("cover"),
            intro = intro.toString().trim(),
            moments = moments,
            extras = NoteExtras(properties = fm.extras(KNOWN), sections = sections, text = after.toString().trim()),
        )
    }
}
