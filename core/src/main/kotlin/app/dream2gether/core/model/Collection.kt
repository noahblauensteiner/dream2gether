package app.dream2gether.core.model

/** An album, project or artwork list: a note in `collections/` that embeds moments in order. */
data class Collection(
    val fileName: String,
    val kind: CollectionKind,
    val title: String,
    val cover: String? = null,
    val intro: String = "",
    val moments: List<MomentId> = emptyList(),
    val extras: NoteExtras = NoteExtras.NONE,
)

enum class CollectionKind(val key: String) {
    ALBUM("album"),
    PROJECT("project"),
    ARTWORK("artwork"),
    PHOTO_BOOK("photo-book"),
    OTHER("other");

    companion object {
        fun fromKey(key: String): CollectionKind = entries.firstOrNull { it.key == key.trim() } ?: OTHER
    }
}
