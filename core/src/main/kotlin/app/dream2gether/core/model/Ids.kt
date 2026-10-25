package app.dream2gether.core.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.random.Random

/**
 * Night note id and file name stem: `s-{date}-{HHmm}-{member}`.
 * Unique per member and minute, so two phones never write the same file.
 */
@JvmInline
value class NightId(val value: String) {
    init {
        require(PATTERN.matches(value)) { "Not a night id: $value" }
    }

    override fun toString() = value

    companion object {
        private val PATTERN = Regex("""s-\d{4}-\d{2}-\d{2}-\d{4}-[a-z0-9]+(-[a-z0-9]+)*""")

        fun of(recordedAt: LocalDateTime, memberId: String): NightId {
            val hhmm = "%02d%02d".format(recordedAt.hour, recordedAt.minute)
            return NightId("s-${recordedAt.date}-$hhmm-$memberId")
        }
    }
}

/**
 * Moment note id and file name stem: `m-{date}-{slug}-{4 random chars}`.
 * The random part keeps ids unique when two phones create moments offline.
 * [date] is the moment's own date, or the recording date when it's unknown.
 */
@JvmInline
value class MomentId(val value: String) {
    init {
        require(PATTERN.matches(value)) { "Not a moment id: $value" }
    }

    override fun toString() = value

    companion object {
        private val PATTERN = Regex("""m-\d{4}-\d{2}-\d{2}-[a-z0-9]+(-[a-z0-9]+)*-[a-z0-9]{4}""")
        private const val SUFFIX_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789"

        fun of(date: LocalDate, title: String, random: Random = Random.Default): MomentId {
            val suffix = (1..4).map { SUFFIX_CHARS[random.nextInt(SUFFIX_CHARS.length)] }.joinToString("")
            return MomentId("m-$date-${slugify(title)}-$suffix")
        }
    }
}

private const val MAX_SLUG_WORDS = 3

/** `"Ramen from scratch!"` → `ramen-from-scratch`. Keeps file names short and ASCII. */
fun slugify(title: String): String {
    val words = title.lowercase()
        .replace(Regex("[^a-z0-9]+"), " ")
        .trim()
        .split(" ")
        .filter { it.isNotEmpty() }
        .take(MAX_SLUG_WORDS)
    return words.joinToString("-").ifEmpty { "moment" }
}
