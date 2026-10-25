package app.dream2gether.core.notes

import app.dream2gether.core.model.Category
import app.dream2gether.core.model.Comment
import app.dream2gether.core.model.DateConfidence
import app.dream2gether.core.model.Moment
import app.dream2gether.core.model.MomentId
import app.dream2gether.core.model.NightId
import app.dream2gether.core.model.NoteExtras
import app.dream2gether.core.model.Section
import kotlinx.datetime.LocalDate

/** Reads and writes moment notes (docs/session-file-format.md#moment-note). */
object MomentNote {
    const val SCHEMA = "dream2gether/moment@1"
    private const val COMMENTS = "Comments"
    private val KNOWN = setOf(
        "type", "schema", "id", "title", "emoji", "date", "date_end", "date_confidence", "who",
        "categories", "places", "people", "first_time", "star", "private", "night", "audio_from",
        "audio_to", "photo_hint", "generated_hash", "cover", "tags",
    )
    private val PHOTO = Regex("""^!\[\[([^\]|#]+)(?:\|[^\]]*)?\]\]$""")
    private val COMMENT_HEAD = Regex("""^>\s*\[!comment\][+-]?\s*(.*)$""", RegexOption.IGNORE_CASE)

    fun write(m: Moment): String {
        val fm = FrontMatterWriter()
            .string("type", "moment")
            .string("schema", SCHEMA)
            .string("id", m.id.value)
            .string("title", m.title)
            .string("emoji", m.emoji)
            .raw("date", m.date)
            .raw("date_end", m.dateEnd)
            .string("date_confidence", m.dateConfidence.key)
            .string("who", m.who)
            .list("categories", m.categories.map { it.key })
            .list("places", m.places)
            .list("people", m.people)
            .raw("first_time", m.firstTime)
            .raw("star", m.star)
            .raw("private", m.private)
            .link("night", m.night.value)
            .string("audio_from", m.audioFromMs?.let(::formatClock))
            .string("audio_to", m.audioToMs?.let(::formatClock))
            .string("photo_hint", m.photoHint)
            .string("generated_hash", m.generatedHash)
            .string("cover", cover(m))
            .list("tags", listOf("moment"))
        m.extras.properties.forEach { (k, v) -> fm.value(k, v) }

        val body = StringBuilder()
        body.append("# ").append(m.emoji).append(' ').append(m.title).append("\n\n")
        if (m.story.isNotBlank()) body.append(m.story.trim()).append("\n\n")
        m.quote?.let { body.append("> \"").append(it).append("\"\n\n") }
        if (m.photos.isNotEmpty()) {
            m.photos.forEach { body.append("![[").append(it).append("]]\n") }
            body.append('\n')
        }
        val commentExtras = m.extras.sections.filter { it.heading == COMMENTS }
        if (m.comments.isNotEmpty() || commentExtras.isNotEmpty()) {
            body.append("## ").append(COMMENTS).append("\n\n")
            m.comments.forEach { c ->
                body.append("> [!comment] ").append(c.author)
                if (c.date.isNotEmpty()) body.append(" · ").append(c.date)
                body.append('\n')
                c.text.lines().forEach { body.append("> ").append(it).append('\n') }
                body.append('\n')
            }
            commentExtras.forEach { body.append(it.content).append("\n\n") }
        }
        body.appendSections(m.extras.sections.filter { it.heading != COMMENTS })
        return NoteFile.render(fm.build(), body.toString())
    }

    fun read(text: String): Moment {
        val note = NoteFile.parse(text)
        val fm = note.frontMatter
        val (preamble, sections) = splitSections(note.body)

        val story = mutableListOf<String>()
        var quote: String? = null
        val photos = mutableListOf<String>()
        var heading: String? = null
        for (block in blocks(preamble)) {
            if (heading == null && block.size == 1 && block[0].startsWith("# ")) {
                heading = block[0].removePrefix("# ").trim()
                continue
            }
            val photoLines = block.mapNotNull { PHOTO.matchEntire(it.trim())?.groupValues?.get(1) }
            when {
                photoLines.size == block.size -> photos += photoLines
                quote == null && block.all { it.startsWith(">") } && !block[0].contains("[!") ->
                    quote = block.joinToString(" ") { it.removePrefix(">").trim() }.trim().trimQuotes()
                else -> story += block.joinToString("\n")
            }
        }

        val comments = mutableListOf<Comment>()
        val extraSections = mutableListOf<Section>()
        for (s in sections) {
            if (s.heading != COMMENTS) { extraSections += s; continue }
            val leftover = mutableListOf<String>()
            for (block in blocks(s.content)) {
                val head = COMMENT_HEAD.matchEntire(block[0])
                if (head == null) { leftover += block.joinToString("\n"); continue }
                val parts = head.groupValues[1].split(" · ", limit = 2)
                val lines = block.drop(1).map { it.removePrefix(">").removePrefix(" ") }
                comments += Comment(parts[0].trim(), parts.getOrElse(1) { "" }.trim(), lines.joinToString("\n"))
            }
            if (leftover.isNotEmpty()) extraSections += Section(COMMENTS, leftover.joinToString("\n\n"))
        }

        val title = fm.string("title") ?: heading.orEmpty()
        val emoji = fm.string("emoji").orEmpty()
        return Moment(
            id = MomentId(requireNotNull(fm.string("id")) { "Moment note without id" }),
            title = title,
            emoji = emoji,
            date = fm.string("date")?.let(LocalDate::parse),
            dateEnd = fm.string("date_end")?.let(LocalDate::parse),
            dateConfidence = fm.string("date_confidence")?.let(DateConfidence::fromKey) ?: DateConfidence.UNKNOWN,
            who = fm.string("who") ?: Moment.WHO_BOTH,
            categories = fm.list("categories").mapNotNull(Category::fromKey),
            places = fm.list("places"),
            people = fm.list("people"),
            firstTime = fm.bool("first_time"),
            star = fm.bool("star"),
            private = fm.bool("private"),
            night = NightId(requireNotNull(fm.link("night")) { "Moment note without night" }),
            audioFromMs = fm.string("audio_from")?.let(::parseClock),
            audioToMs = fm.string("audio_to")?.let(::parseClock),
            photoHint = fm.string("photo_hint"),
            generatedHash = fm.string("generated_hash"),
            story = story.joinToString("\n\n"),
            quote = quote,
            photos = photos,
            comments = comments,
            extras = NoteExtras(properties = fm.extras(KNOWN), sections = extraSections),
        )
    }

    /** Dashboard card image: the first photo, else the first category's colour. Derived, never read. */
    private fun cover(m: Moment): String = m.photos.firstOrNull()?.let(::wrapLink)
        ?: m.categories.firstOrNull()?.color
        ?: Category.NO_CATEGORY_COLOR

    private fun String.trimQuotes(): String =
        removeSurrounding("\"").removeSurrounding("“", "”")
}
