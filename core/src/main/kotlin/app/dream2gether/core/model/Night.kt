package app.dream2gether.core.model

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.UtcOffset

/** One recording session. Stored as a night note in `sessions/YYYY-MM/`. */
data class Night(
    val id: NightId,
    val recordedBy: String,
    val device: String,
    val recordedAt: LocalDateTime,
    val utcOffset: UtcOffset,
    val durationS: Int,
    val language: String,
    val question: String?,
    val audioFile: String,
    val status: NightStatus,
    val processing: Processing? = null,
    val moments: List<MomentLink> = emptyList(),
    val dreams: List<DreamMention> = emptyList(),
    val transcript: List<Segment> = emptyList(),
)

enum class NightStatus(val key: String) {
    RECORDED("recorded"),
    TRANSCRIBED("transcribed"),
    EXTRACTED("extracted"),
    FAILED("failed");

    companion object {
        fun fromKey(key: String): NightStatus? = entries.firstOrNull { it.key == key.trim() }
    }
}

/** Which models and prompt produced a night's results, so we know what to re-run later. */
data class Processing(
    val sttModel: String,
    val llmModel: String?,
    val promptVersion: String?,
    val processedAt: LocalDateTime?,
)

/** A transcript line. [language] is set only when it differs from the night's language. */
data class Segment(
    val index: Int,
    val startMs: Long,
    val endMs: Long,
    val text: String,
    val language: String? = null,
)

/** Entry in a night note's "Moments" list. */
data class MomentLink(val id: MomentId, val label: String, val atMs: Long?)

/** Entry in a night note's "Dreams" list. */
data class DreamMention(val text: String, val atMs: Long?)
