package app.dream2gether.core.model

/** A property value from a note's front matter. */
sealed interface YamlValue {
    data class Scalar(val text: String) : YamlValue
    data class Items(val items: List<String>) : YamlValue
}

/** A `## Heading` section of a note body; [content] is trimmed of blank lines at both ends. */
data class Section(val heading: String, val content: String)

/**
 * What a note holds beyond what we model: properties and sections added in Obsidian, and free
 * text where a note allows it. Carried through so rewriting a note never loses someone's edits.
 */
data class NoteExtras(
    val properties: List<Pair<String, YamlValue>> = emptyList(),
    val sections: List<Section> = emptyList(),
    val text: String = "",
) {
    companion object {
        val NONE = NoteExtras()
    }
}
