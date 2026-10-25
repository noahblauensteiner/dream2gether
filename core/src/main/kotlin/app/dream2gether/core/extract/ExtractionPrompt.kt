package app.dream2gether.core.extract

import app.dream2gether.core.model.Category
import app.dream2gether.core.model.Segment
import app.dream2gether.core.model.Space
import app.dream2gether.core.notes.shortDay
import kotlinx.datetime.LocalDate

/** Prompt v1 for picking favourite moments out of a bedtime chat. Written for small local models. */
object ExtractionPrompt {
    const val VERSION = "extract@1"

    fun system(space: Space): String {
        val people = space.members.joinToString(", ") { "${it.id} (${it.name})" }
        val categories = Category.entries.joinToString("\n") { "- ${it.key}: ${it.label}" }
        return """
            You help a couple keep a journal of their favourite moments. You read the transcript of their
            bedtime chat and pick out the moments they enjoyed. Answer with JSON only.

            Rules:
            - A moment is something that happened and that they liked, loved or found funny. Skip complaints,
              chores and small talk. If nothing qualifies, return an empty "moments" list.
            - One moment per event. If they come back to the same event later, it's still one moment.
            - title: 2 to 6 warm words, no emoji. emoji: one emoji that fits.
            - story: 1 to 3 short sentences in "we" voice, past tense. Only facts from the transcript.
            - quote: one short memorable line copied word for word from the transcript, or "".
            - day_ref: when it happened, relative to the chat date: "today", "yesterday", "weekday:saturday",
              "date:YYYY-MM-DD", "this-week", "weekend" or "unknown". Don't calculate dates yourself.
              day_ref_end: only for multi-day moments, same format, else "". approx: true if they're vague.
            - who: whose favourite it was, using these ids: $people. Use "both" if it was shared or unclear.
            - categories: 1 or 2 that fit best, from this list:
            $categories
            - places, people: names as they said them. first_time: true only if they say it was the first time.
            - photo_hint: 2 to 5 words describing a photo that would fit, or "".
            - segments: the line numbers [n] where they talk about the moment.
            - dreams: things they say they want to do in the future ("we should…", "let's…").

            Example. People: noah (Noah), kim (Kim). Transcript:
            [0] Kim: Okay, favourite moment today?
            [1] Noah: The pancakes. You flipped one onto the ceiling.
            [2] Kim: Ha! And on Saturday the lake was so calm.
            [3] Noah: We should go camping there in spring.
            Answer:
            {"moments":[{"title":"Pancake on the ceiling","emoji":"🥞","day_ref":"today","day_ref_end":"","approx":false,"who":"noah","categories":["food","silly"],"places":["home"],"people":[],"first_time":false,"story":"We made pancakes and Kim flipped one right onto the ceiling.","quote":"You flipped one onto the ceiling.","photo_hint":"pancakes in the kitchen","segments":[1]},{"title":"Calm lake on Saturday","emoji":"🌊","day_ref":"weekday:saturday","day_ref_end":"","approx":false,"who":"kim","categories":["outdoors"],"places":["the lake"],"people":[],"first_time":false,"story":"On Saturday we were at the lake and the water was perfectly calm.","quote":"","photo_hint":"calm lake","segments":[2]}],"dreams":[{"text":"Go camping at the lake in spring","segments":[3]}]}
        """.trimIndent()
    }

    fun user(
        space: Space,
        evening: LocalDate,
        question: String?,
        segments: List<Segment>,
        recentTitles: List<String>,
    ): String = buildString {
        append("Chat date: ").append(evening.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() })
            .append(' ').append(evening).append(" (").append(shortDay(evening)).append(", evening)\n")
        question?.let { append("Question they answered: ").append(it).append('\n') }
        append("People: ").append(space.members.joinToString(", ") { "${it.id} (${it.name})" }).append('\n')
        if (space.glossaryPeople.isNotEmpty()) append("Names they use for others: ").append(space.glossaryPeople.joinToString(", ")).append('\n')
        if (space.glossaryPlaces.isNotEmpty()) append("Places they know: ").append(space.glossaryPlaces.joinToString(", ")).append('\n')
        if (recentTitles.isNotEmpty()) {
            append("Moments already in the journal (only add them again if something new happened):\n")
            recentTitles.forEach { append("- ").append(it).append('\n') }
        }
        append("Transcript:\n")
        segments.forEach { append('[').append(it.index).append("] ").append(it.text).append('\n') }
    }
}
