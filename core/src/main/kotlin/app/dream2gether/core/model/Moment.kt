package app.dream2gether.core.model

import kotlinx.datetime.LocalDate

/** One favourite moment. Stored as a moment note in `moments/YYYY-MM/`. */
data class Moment(
    val id: MomentId,
    val title: String,
    val emoji: String,
    val date: LocalDate?,
    val dateEnd: LocalDate? = null,
    val dateConfidence: DateConfidence,
    val who: String = WHO_BOTH,
    val categories: List<Category> = emptyList(),
    val places: List<String> = emptyList(),
    val people: List<String> = emptyList(),
    val firstTime: Boolean = false,
    val star: Boolean = false,
    val private: Boolean = false,
    val night: NightId,
    val audioFromMs: Long? = null,
    val audioToMs: Long? = null,
    val photoHint: String? = null,
    val generatedHash: String? = null,
    val story: String,
    val quote: String? = null,
    val photos: List<String> = emptyList(),
    val comments: List<Comment> = emptyList(),
) {
    companion object {
        const val WHO_BOTH = "both"
    }
}

enum class DateConfidence(val key: String) {
    EXACT("exact"),
    APPROX("approx"),
    UNKNOWN("unknown");

    companion object {
        fun fromKey(key: String): DateConfidence? = entries.firstOrNull { it.key == key.trim() }
    }
}

/** A `> [!comment] {author} · {date}` callout under a moment's "Comments" heading. */
data class Comment(val author: String, val date: String, val text: String)
