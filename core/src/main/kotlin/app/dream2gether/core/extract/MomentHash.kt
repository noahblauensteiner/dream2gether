package app.dream2gether.core.extract

import app.dream2gether.core.model.Moment

/**
 * Fingerprint of the fields the model wrote. Stored as `generated_hash` when a moment is created;
 * if a later hash differs, someone edited the moment and re-processing must leave it alone.
 */
object MomentHash {
    fun of(m: Moment): String {
        val canonical = listOf(
            m.title, m.emoji, m.date, m.dateEnd, m.dateConfidence.key, m.who,
            m.categories.joinToString(",") { it.key }, m.places.joinToString(","), m.people.joinToString(","),
            m.firstTime, m.story, m.quote, m.photoHint,
        ).joinToString("\u0000") { it?.toString().orEmpty() }
        return fnv1a32(canonical.encodeToByteArray()).toUInt().toString(16).padStart(8, '0')
    }

    private fun fnv1a32(bytes: ByteArray): Int {
        var hash = 0x811c9dc5.toInt()
        for (b in bytes) {
            hash = hash xor (b.toInt() and 0xff)
            hash *= 0x01000193
        }
        return hash
    }
}
