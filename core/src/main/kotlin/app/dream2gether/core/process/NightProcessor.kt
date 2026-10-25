package app.dream2gether.core.process

import app.dream2gether.core.extract.DateRef
import app.dream2gether.core.extract.ExtractionPrompt
import app.dream2gether.core.extract.MomentExtractor
import app.dream2gether.core.llm.LanguageModel
import app.dream2gether.core.model.Moment
import app.dream2gether.core.model.MomentLink
import app.dream2gether.core.model.Night
import app.dream2gether.core.model.NightId
import app.dream2gether.core.model.NightStatus
import app.dream2gether.core.model.Processing
import app.dream2gether.core.model.Segment
import app.dream2gether.core.notes.MomentNote
import app.dream2gether.core.notes.NightNote
import app.dream2gether.core.space.SpaceLayout
import app.dream2gether.core.space.SpaceReader
import app.dream2gether.core.space.SpaceStore
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.UtcOffset
import kotlin.random.Random

/** Turns one recorded or typed night into a night note plus moment notes in the space. */
class NightProcessor(
    private val store: SpaceStore,
    private val llm: LanguageModel,
    private val now: () -> String,
    private val random: Random = Random.Default,
) {
    class AlreadyProcessed(path: String) : IllegalStateException("This night already exists: $path")

    data class NightInput(
        val recordedBy: String,
        val device: String,
        val recordedAt: LocalDateTime,
        val utcOffset: UtcOffset,
        val question: String?,
        val transcript: List<Segment>,
        val durationS: Int,
        /** File name in the audio folder; null for typed chats. */
        val audioFile: String?,
        val sttModel: String,
    )

    data class Processed(val night: Night, val moments: List<Moment>, val nightPath: String, val momentPaths: List<String>)

    fun process(input: NightInput): Processed {
        val space = SpaceReader.space(store)
        val member = requireNotNull(space.member(input.recordedBy)) {
            "${input.recordedBy} isn't in this space (members: ${space.members.joinToString { it.id }})"
        }
        val id = NightId.of(input.recordedAt, member.id)
        val nightPath = SpaceLayout.nightNote(id)
        if (store.exists(nightPath)) throw AlreadyProcessed(nightPath)

        val evening = DateRef.eveningOf(input.recordedAt)
        val result = MomentExtractor(llm, random).extract(
            MomentExtractor.Input(
                space = space,
                nightId = id,
                evening = evening,
                question = input.question,
                transcript = input.transcript,
                recent = SpaceReader.recentMoments(store, evening),
                hasAudio = input.audioFile != null,
            ),
        )

        val momentPaths = result.moments.map { m ->
            SpaceLayout.momentNote(m.id).also { store.writeText(it, MomentNote.write(m)) }
        }
        val night = Night(
            id = id,
            recordedBy = member.id,
            device = input.device,
            recordedAt = input.recordedAt,
            utcOffset = input.utcOffset,
            durationS = input.durationS,
            language = space.language,
            question = input.question,
            audioFile = input.audioFile,
            status = NightStatus.EXTRACTED,
            processing = Processing(input.sttModel, llm.name, ExtractionPrompt.VERSION, now()),
            moments = result.moments.map { MomentLink(it.id, "${it.emoji} ${it.title}", it.audioFromMs) },
            dreams = result.dreams,
            transcript = input.transcript,
        )
        store.writeText(nightPath, NightNote.write(night, member.name))
        return Processed(night, result.moments, nightPath, momentPaths)
    }
}
