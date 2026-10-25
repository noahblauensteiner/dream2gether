package app.dream2gether.core.notes

import app.dream2gether.core.model.Section

/** A Markdown note: YAML front matter between `---` lines, then the body. */
data class NoteFile(val frontMatter: FrontMatter, val body: String) {
    companion object {
        fun parse(text: String): NoteFile {
            val normalized = text.replace("\r\n", "\n")
            if (!normalized.startsWith("---\n")) return NoteFile(FrontMatter(emptyList()), normalized)
            val end = normalized.indexOf("\n---", startIndex = 3)
            if (end < 0) return NoteFile(FrontMatter(emptyList()), normalized)
            val yaml = normalized.substring(4, end + 1)
            val body = normalized.substring(end + 4).removePrefix("\n").trimStart('\n')
            return NoteFile(FrontMatter.parse(yaml), body)
        }

        fun render(frontMatter: String, body: String): String =
            "---\n$frontMatter---\n\n${body.trimEnd()}\n"
    }
}

/** Splits a body at level-2 headings. Returns the text before the first one, and the sections. */
fun splitSections(body: String): Pair<String, List<Section>> {
    val preamble = StringBuilder()
    val sections = mutableListOf<Section>()
    var heading: String? = null
    val content = StringBuilder()
    for (line in body.lines()) {
        if (line.startsWith("## ")) {
            heading?.let { sections += Section(it, content.toString().trim('\n')) }
            heading = line.removePrefix("## ").trim()
            content.clear()
        } else if (heading == null) {
            preamble.append(line).append('\n')
        } else {
            content.append(line).append('\n')
        }
    }
    heading?.let { sections += Section(it, content.toString().trim('\n')) }
    return preamble.toString().trim('\n') to sections
}

/** Splits text into blocks separated by blank lines. */
fun blocks(text: String): List<List<String>> {
    val out = mutableListOf<List<String>>()
    var cur = mutableListOf<String>()
    for (line in text.lines()) {
        if (line.isBlank()) {
            if (cur.isNotEmpty()) { out += cur; cur = mutableListOf() }
        } else {
            cur += line
        }
    }
    if (cur.isNotEmpty()) out += cur
    return out
}

internal fun StringBuilder.appendSections(sections: List<Section>) {
    sections.forEach { append("\n## ").append(it.heading).append("\n\n").append(it.content).append('\n') }
}
