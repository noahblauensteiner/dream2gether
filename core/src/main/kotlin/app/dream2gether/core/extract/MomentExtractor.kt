package app.dream2gether.core.extract

import app.dream2gether.core.llm.LanguageModel
import app.dream2gether.core.llm.LlmRequest
import app.dream2gether.core.model.Category
import app.dream2gether.core.model.DreamMention
import app.dream2gether.core.model.Moment
import app.dream2gether.core.model.MomentId
import app.dream2gether.core.model.NightId
import app.dream2gether.core.model.Segment
import app.dream2gether.core.model.Space
import app.dream2gether.core.model.slugify
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlin.random.Random

/** Picks favourite moments out of a night's transcript with a local model, then cleans them up in code. */
class MomentExtractor(private val llm: LanguageModel, private val random: Random = Random.Default) {
    class ExtractionFailed(message: String) : RuntimeException(message)

    data class Input(
        val space: Space,
        val nightId: NightId,
        val evening: LocalDate,
        val question: String?,
        val transcript: List<Segment>,
        val recent: List<Moment> = emptyList(),
        /** False for typed chats: their segment times don't point into any audio. */
        val hasAudio: Boolean,
    )

    data class Result(val moments: List<Moment>, val dreams: List<DreamMention>)

    private val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

    fun extract(input: Input): Result {
        if (input.transcript.isEmpty()) return Result(emptyList(), emptyList())
        val system = ExtractionPrompt.system(input.space)
        val schema = ExtractionSchema.build(input.space.members.map { it.id })
        val recentTitles = input.recent.map { "${it.emoji} ${it.title}" }

        val moments = mutableListOf<Found>()
        val dreams = mutableListOf<DreamMention>()
        for (chunk in chunks(input.transcript)) {
            val user = ExtractionPrompt.user(input.space, input.evening, input.question, chunk, recentTitles)
            val output = ask(LlmRequest(system, user, schema))
            val valid = chunk.map { it.index }.toSet()
            output.moments.mapNotNull { toMoment(it, input, valid) }.forEach { merge(moments, it) }
            output.dreams.mapNotNull { toDream(it, input, valid) }
                .filter { d -> dreams.none { it.text.equals(d.text, ignoreCase = true) } }
                .forEach { dreams += it }
        }
        return Result(moments.map { it.moment }, dreams)
    }

    /** A moment plus the transcript lines it came from, used to spot duplicates across chunks. */
    private data class Found(val moment: Moment, val segments: Set<Int>)

    /** One retry: constrained decoding makes bad JSON rare, but a cut-off answer can still happen. */
    private fun ask(request: LlmRequest): ExtractionOutput {
        var last: String? = null
        repeat(2) {
            val raw = llm.complete(request)
            last = raw
            parse(raw)?.let { return it }
        }
        throw ExtractionFailed("The model didn't return valid JSON. It said: ${last?.take(300)}")
    }

    internal fun parse(raw: String): ExtractionOutput? {
        val start = raw.indexOf('{')
        val end = raw.lastIndexOf('}')
        if (start < 0 || end <= start) return null
        return runCatching { json.decodeFromString<ExtractionOutput>(raw.substring(start, end + 1)) }.getOrNull()
    }

    private fun toMoment(e: ExtractedMoment, input: Input, valid: Set<Int>): Found? {
        val title = e.title.trim().take(MAX_TITLE)
        if (title.isEmpty()) return null
        val segments = e.segments.filter { it in valid }.distinct().sorted()
        val bySegment = input.transcript.associateBy { it.index }
        val transcriptText = normalize(input.transcript.joinToString(" ") { it.text })
        val date = DateRef.resolve(e.dayRef, e.dayRefEnd, e.approx, input.evening)
        val memberIds = input.space.members.map { it.id }.toSet()
        val glossary = input.space.glossaryPeople + input.space.glossaryPlaces

        val moment = Moment(
            id = MomentId.of(date.date ?: input.evening, title, random),
            title = title,
            emoji = cleanEmoji(e.emoji),
            date = date.date,
            dateEnd = date.dateEnd,
            dateConfidence = date.confidence,
            who = e.who.takeIf { it in memberIds } ?: Moment.WHO_BOTH,
            categories = e.categories.mapNotNull(Category::fromKey).distinct().take(MAX_CATEGORIES),
            places = cleanNames(e.places, glossary),
            people = cleanNames(e.people, glossary),
            firstTime = e.firstTime,
            night = input.nightId,
            audioFromMs = if (input.hasAudio) segments.mapNotNull { bySegment[it]?.startMs }.minOrNull() else null,
            audioToMs = if (input.hasAudio) segments.mapNotNull { bySegment[it]?.endMs }.maxOrNull() else null,
            photoHint = e.photoHint.trim().ifEmpty { null },
            story = e.story.trim().ifEmpty { title },
            // Quotes must be real: drop anything the model paraphrased.
            quote = e.quote.trim().trim('"', '“', '”').takeIf { it.isNotEmpty() && normalize(it) in transcriptText },
        )
        return Found(moment.copy(generatedHash = MomentHash.of(moment)), segments.toSet())
    }

    private fun toDream(e: ExtractedDream, input: Input, valid: Set<Int>): DreamMention? {
        val text = e.text.trim()
        if (text.isEmpty()) return null
        val first = e.segments.filter { it in valid }.minOrNull()
        val at = if (input.hasAudio) input.transcript.firstOrNull { it.index == first }?.startMs else null
        return DreamMention(text, at)
    }

    /** Chunks overlap, so the same moment can come back twice: same segments or same title = same moment. */
    private fun merge(into: MutableList<Found>, f: Found) {
        val i = into.indexOfFirst { other ->
            slugify(other.moment.title) == slugify(f.moment.title) || other.segments.any { it in f.segments }
        }
        when {
            i < 0 -> into += f
            f.moment.story.length > into[i].moment.story.length -> into[i] = f.copy(segments = f.segments + into[i].segments)
            else -> into[i] = into[i].copy(segments = into[i].segments + f.segments)
        }
    }

    private fun chunks(segments: List<Segment>): List<List<Segment>> {
        val out = mutableListOf<List<Segment>>()
        var start = 0
        while (start < segments.size) {
            var end = start
            var chars = 0
            while (end < segments.size && (end == start || chars + segments[end].text.length <= MAX_CHUNK_CHARS)) {
                chars += segments[end].text.length
                end++
            }
            out += segments.subList(start, end)
            if (end >= segments.size) break
            start = maxOf(start + 1, end - CHUNK_OVERLAP)
        }
        return out
    }

    private fun cleanEmoji(e: String): String {
        val t = e.trim().substringBefore(' ')
        return if (t.isEmpty() || t.length > 8 || t.any { it.isLetterOrDigit() }) "✨" else t
    }

    /** Trims, drops duplicates and uses the glossary's spelling when a name matches it. */
    private fun cleanNames(names: List<String>, glossary: List<String>): List<String> =
        names.map { n -> n.trim() }.filter { it.isNotEmpty() }
            .map { n -> glossary.firstOrNull { it.equals(n, ignoreCase = true) } ?: n }
            .distinctBy { it.lowercase() }
            .take(MAX_NAMES)

    private fun normalize(s: String) = s.lowercase().replace(Regex("[^a-z0-9]+"), " ").trim()

    private companion object {
        const val MAX_TITLE = 80
        const val MAX_CATEGORIES = 3
        const val MAX_NAMES = 5
        const val MAX_CHUNK_CHARS = 6_000
        const val CHUNK_OVERLAP = 2
    }
}
