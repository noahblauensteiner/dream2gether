package app.dream2gether.core.notes

import app.dream2gether.core.model.DreamMention
import app.dream2gether.core.model.MomentId
import app.dream2gether.core.model.MomentLink
import app.dream2gether.core.model.Night
import app.dream2gether.core.model.NightId
import app.dream2gether.core.model.NightStatus
import app.dream2gether.core.model.NoteExtras
import app.dream2gether.core.model.Processing
import app.dream2gether.core.model.Section
import app.dream2gether.core.model.Segment
import kotlin.math.roundToInt

/** Reads and writes night notes (docs/session-file-format.md#night-note). */
object NightNote {
    const val SCHEMA = "dream2gether/night@1"
    private const val MOMENTS = "Moments"
    private const val DREAMS = "Dreams"
    private const val TRANSCRIPT = "Transcript"
    private val KNOWN = setOf(
        "type", "schema", "id", "recorded_by", "device", "recorded_at", "duration_s", "language",
        "question", "audio", "status", "stt_model", "llm_model", "prompt_version", "processed_at",
    )
    private val MOMENT_ITEM = Regex("""^- \[\[([^\]|]+)(?:\|([^\]]*))?\]\](?:\s*·\s*([\d:]+))?\s*$""")
    private val DREAM_ITEM = Regex("""^- 💭\s*(.*?)(?:\s*·\s*([\d:]+))?\s*$""")
    private val LINE = Regex("""^\*\*\[([\d:]+)(?:\s*·\s*([a-z]{2,3}))?\]\*\*\s?(.*)$""")

    /** [recorderName] is the display name of `recordedBy`, for the heading. */
    fun write(n: Night, recorderName: String): String {
        val fm = FrontMatterWriter()
            .string("type", "night")
            .string("schema", SCHEMA)
            .string("id", n.id.value)
            .string("recorded_by", n.recordedBy)
            .string("device", n.device)
            .raw("recorded_at", formatOffsetDateTime(n.recordedAt, n.utcOffset))
            .raw("duration_s", n.durationS)
            .string("language", n.language)
            .string("question", n.question)
            .link("audio", n.audioFile)
            .string("status", n.status.key)
            .string("stt_model", n.processing?.sttModel)
            .string("llm_model", n.processing?.llmModel)
            .string("prompt_version", n.processing?.promptVersion)
            .raw("processed_at", n.processing?.processedAt)
        n.extras.properties.forEach { (k, v) -> fm.value(k, v) }

        val minutes = maxOf(1, (n.durationS / 60.0).roundToInt())
        val body = StringBuilder()
        body.append("# 🌙 ").append(shortDay(n.recordedAt.date)).append(" · ").append(minutes)
            .append(" min · recorded by ").append(recorderName).append("\n\n")
        n.question?.let { body.append("> *").append(it).append("*\n\n") }
        n.audioFile?.let { body.append("![[").append(it).append("]]\n") }
        if (n.moments.isNotEmpty()) {
            body.append("\n## ").append(MOMENTS).append("\n\n")
            n.moments.forEach { m ->
                body.append("- [[").append(m.id.value).append('|').append(m.label).append("]]")
                m.atMs?.let { body.append(" · ").append(formatClock(it)) }
                body.append('\n')
            }
        }
        if (n.dreams.isNotEmpty()) {
            body.append("\n## ").append(DREAMS).append("\n\n")
            n.dreams.forEach { d ->
                body.append("- 💭 ").append(d.text)
                d.atMs?.let { body.append(" · ").append(formatClock(it)) }
                body.append('\n')
            }
        }
        if (n.transcript.isNotEmpty()) {
            body.append("\n## ").append(TRANSCRIPT).append("\n\n")
            n.transcript.forEach { s ->
                body.append("**[").append(formatClock(s.startMs))
                s.language?.let { body.append(" · ").append(it) }
                body.append("]** ").append(s.text).append('\n')
            }
        }
        body.appendSections(n.extras.sections)
        return NoteFile.render(fm.build(), body.toString())
    }

    fun read(text: String): Night {
        val note = NoteFile.parse(text)
        val fm = note.frontMatter
        val (_, sections) = splitSections(note.body)
        val (recordedAt, offset) = parseOffsetDateTime(requireNotNull(fm.string("recorded_at")) { "Night note without recorded_at" })
        val durationS = fm.int("duration_s") ?: 0

        var moments = emptyList<MomentLink>()
        var dreams = emptyList<DreamMention>()
        var transcript = emptyList<Segment>()
        val extras = mutableListOf<Section>()
        for (s in sections) {
            when (s.heading) {
                MOMENTS -> moments = s.content.lines().mapNotNull { line ->
                    MOMENT_ITEM.matchEntire(line.trim())?.let {
                        MomentLink(MomentId(it.groupValues[1]), it.groupValues[2], parseClock(it.groupValues[3]))
                    }
                }
                DREAMS -> dreams = s.content.lines().mapNotNull { line ->
                    DREAM_ITEM.matchEntire(line.trim())?.let { DreamMention(it.groupValues[1], parseClock(it.groupValues[2])) }
                }
                TRANSCRIPT -> transcript = readTranscript(s.content, durationS * 1000L)
                else -> extras += s
            }
        }

        val stt = fm.string("stt_model")
        return Night(
            id = NightId(requireNotNull(fm.string("id")) { "Night note without id" }),
            recordedBy = fm.string("recorded_by").orEmpty(),
            device = fm.string("device").orEmpty(),
            recordedAt = recordedAt,
            utcOffset = offset,
            durationS = durationS,
            language = fm.string("language") ?: "en",
            question = fm.string("question"),
            audioFile = fm.link("audio"),
            status = fm.string("status")?.let(NightStatus::fromKey) ?: NightStatus.RECORDED,
            processing = stt?.let {
                Processing(it, fm.string("llm_model"), fm.string("prompt_version"), fm.string("processed_at"))
            },
            moments = moments,
            dreams = dreams,
            transcript = transcript,
            extras = NoteExtras(properties = fm.extras(KNOWN), sections = extras),
        )
    }

    /** Lines that don't start with a timestamp belong to the line before (hand-wrapped text). */
    private fun readTranscript(content: String, durationMs: Long): List<Segment> {
        data class Raw(val startMs: Long, val language: String?, val text: StringBuilder)
        val raws = mutableListOf<Raw>()
        for (line in content.lines()) {
            val m = LINE.matchEntire(line.trim())
            val start = m?.let { parseClock(it.groupValues[1]) }
            if (m != null && start != null) {
                raws += Raw(start, m.groupValues[2].ifEmpty { null }, StringBuilder(m.groupValues[3]))
            } else if (line.isNotBlank() && raws.isNotEmpty()) {
                raws.last().text.append(' ').append(line.trim())
            }
        }
        return raws.mapIndexed { i, r ->
            val end = raws.getOrNull(i + 1)?.startMs ?: maxOf(durationMs, r.startMs)
            Segment(i, r.startMs, end, r.text.toString(), r.language)
        }
    }
}
