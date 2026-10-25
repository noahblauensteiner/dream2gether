package app.dream2gether.core.process

import app.dream2gether.core.model.Segment

/**
 * A typed (pretend) bedtime chat: one line per turn, optionally `Name: text`. Blank lines and
 * `#` comments are skipped. There's no audio, so lines get evenly spaced placeholder times.
 */
object TypedChat {
    const val SECONDS_PER_LINE = 5

    fun parse(text: String): List<Segment> =
        text.lines().map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("#") }
            .mapIndexed { i, line ->
                Segment(i, i * SECONDS_PER_LINE * 1000L, (i + 1) * SECONDS_PER_LINE * 1000L, line)
            }
}
