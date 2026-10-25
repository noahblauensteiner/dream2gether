package app.dream2gether.core.space

import app.dream2gether.core.model.Category
import app.dream2gether.core.model.Category.ADVENTURE
import app.dream2gether.core.model.Category.COZY
import app.dream2gether.core.model.Category.CULTURE
import app.dream2gether.core.model.Category.FOOD
import app.dream2gether.core.model.Category.LITTLE_THINGS
import app.dream2gether.core.model.Category.OUTDOORS
import app.dream2gether.core.model.Category.PEOPLE
import app.dream2gether.core.model.Category.SILLY
import app.dream2gether.core.model.Category.SPORT
import app.dream2gether.core.model.Category.US
import app.dream2gether.core.model.Collection
import app.dream2gether.core.model.CollectionKind
import app.dream2gether.core.model.DateConfidence
import app.dream2gether.core.model.Member
import app.dream2gether.core.model.Moment
import app.dream2gether.core.model.MomentId
import app.dream2gether.core.model.MomentLink
import app.dream2gether.core.model.Night
import app.dream2gether.core.model.NightId
import app.dream2gether.core.model.NightStatus
import app.dream2gether.core.model.Processing
import app.dream2gether.core.model.Segment
import app.dream2gether.core.model.Space
import app.dream2gether.core.notes.CollectionNote
import app.dream2gether.core.notes.MomentNote
import app.dream2gether.core.notes.NightNote
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.UtcOffset

/**
 * A fictional October 2026 for Alex & Sam (same story as examples/fav-moments-2026-10.md), so the
 * Obsidian dashboard has something to show before any real night is recorded.
 * Audio files are one second of silence: placeholders, not recordings.
 */
object DemoSpace {
    val space = Space(
        name = "Alex & Sam (demo)",
        members = listOf(Member("alex", "Alex"), Member("sam", "Sam")),
        timezone = "Europe/Vienna",
        glossaryPeople = listOf("Mila", "Jonas", "Grandma Rosi", "Pepper"),
        glossaryPlaces = listOf("farmers' market", "boulder gym", "old cinema"),
    )

    private data class Demo(
        val id: String,
        val recordedAt: LocalDateTime,
        val by: String,
        val emoji: String,
        val title: String,
        val categories: List<Category>,
        val places: List<String>,
        val people: List<String>,
        val story: String,
        val quote: String? = null,
        val firstTime: Boolean = false,
        val star: Boolean = false,
        val dateEnd: LocalDate? = null,
    )

    private fun at(day: Int, hour: Int, minute: Int) = LocalDateTime(2026, 10, day, hour, minute, 0)

    private val demos = listOf(
        Demo("m-2026-10-01-autumn-walk-q7rx", at(1, 22, 41), "sam", "🍂", "First proper autumn walk",
            listOf(OUTDOORS, US), listOf("riverside path"), emptyList(),
            "Straight after work, still in office shoes, we took the long way home along the river. " +
                "It was the first evening that really smelled like autumn.",
            "Can we do this every Thursday?"),
        Demo("m-2026-10-03-chestnuts-p8nc", at(3, 23, 5), "sam", "🌰", "Hot chestnuts at the farmers' market",
            listOf(FOOD, OUTDOORS), listOf("farmers' market"), emptyList(),
            "The chestnut stand is back. We burnt our fingers on the first bag and bought a second one anyway."),
        Demo("m-2026-10-04-board-game-h9mf", at(4, 22, 15), "alex", "🎲", "Board game rematch at Mila & Jonas'",
            listOf(PEOPLE, SILLY), listOf("Mila & Jonas' place"), listOf("Mila", "Jonas"),
            "After four lost games in a row, Alex finally won, by one point. Jonas demanded a recount.",
            "One point. ONE. I'm framing the scoresheet."),
        Demo("m-2026-10-07-ramen-n9qe", at(7, 22, 50), "sam", "🍜", "Ramen from scratch",
            listOf(FOOD, COZY), listOf("home"), emptyList(),
            "Six hours of broth, two burnt eggs, one perfect bowl each. We ate on the living room floor.",
            "Six hours for one bowl of soup and I'd do it again tomorrow.", firstTime = true, star = true),
        Demo("m-2026-10-10-bouldering-b2kd", at(10, 23, 20), "alex", "🧗", "First bouldering session",
            listOf(SPORT, ADVENTURE), listOf("boulder gym"), emptyList(),
            "Shaky arms and chalk everywhere. Alex topped a yellow route on the fourth try.",
            firstTime = true),
        Demo("m-2026-10-14-rosi-call-w4tz", at(14, 22, 30), "sam", "📞", "Long call with Grandma Rosi",
            listOf(PEOPLE), emptyList(), listOf("Grandma Rosi"),
            "An hour and a half on the phone. Rosi told how she met Grandpa at a dance, with a stolen bicycle in it."),
        Demo("m-2026-10-17-storm-t3ob", at(17, 23, 10), "alex", "⛈️", "Storm watching with tea",
            listOf(COZY, US), listOf("home"), emptyList(),
            "The power flickered twice, so we switched all the lights off on purpose and counted the thunder.",
            "Eleven seconds! It's moving away.", star = true),
        Demo("m-2026-10-18-cinema-r2ye", at(18, 22, 45), "sam", "🎬", "Double feature at the old cinema",
            listOf(CULTURE, US), listOf("old cinema"), emptyList(),
            "Two black-and-white films back to back. The popcorn was gone before the first film started."),
        Demo("m-2026-10-20-pepper-g7ls", at(20, 22, 20), "alex", "🐕", "Pepper finally likes Alex",
            listOf(SILLY, LITTLE_THINGS), listOf("stairwell"), listOf("Pepper"),
            "After two years of growling, the neighbours' dog Pepper rolled over for a belly rub.",
            "She CHOSE me."),
        Demo("m-2026-10-24-pumpkins-x5ja", at(24, 22, 55), "sam", "🎃", "Pumpkin carving",
            listOf(COZY, SILLY), listOf("home"), emptyList(),
            "Alex carved a classic grin. Sam carved a spider with eight legs, for accuracy. It collapsed by Sunday."),
        Demo("m-2026-10-26-sunrise-d6vu", at(28, 22, 35), "alex", "🌅", "Sunrise on the way to work",
            listOf(LITTLE_THINGS, US), listOf("tram stop"), emptyList(),
            "For once we were both up early and walked to the tram together. The whole sky went pink.",
            dateEnd = LocalDate(2026, 10, 28)),
        Demo("m-2026-10-31-halloween-k3f9", at(31, 23, 50), "sam", "🕷️", "Halloween at Mila & Jonas'",
            listOf(PEOPLE, SILLY), listOf("Mila & Jonas' place"), listOf("Mila", "Jonas"),
            "Sam went as the pumpkin spider, now with sturdier legs. Jonas gave the costume award to the spider.",
            star = true),
    )

    /** Creates the demo space in an empty [store]. */
    fun create(store: SpaceStore) {
        SpaceInit.create(store, space)
        val wav = silentWav()
        for (d in demos) {
            val momentId = MomentId(d.id)
            val nightId = NightId.of(d.recordedAt, d.by)
            val sentences = listOfNotNull(d.story, d.quote)
            val transcript = (listOf("So. Favourite moment today?") + sentences).mapIndexed { i, text ->
                Segment(i, i * 8_000L, (i + 1) * 8_000L, text)
            }
            val night = Night(
                id = nightId,
                recordedBy = d.by,
                device = "demo",
                recordedAt = d.recordedAt,
                utcOffset = if (d.recordedAt.day >= 25) UtcOffset(hours = 1) else UtcOffset(hours = 2),
                durationS = 300 + d.recordedAt.day * 10,
                language = "en",
                question = "What was your favourite moment today?",
                audioFile = "${nightId.value}.wav",
                status = NightStatus.EXTRACTED,
                processing = Processing("demo", "demo", "demo", null),
                moments = listOf(MomentLink(momentId, "${d.emoji} ${d.title}", 8_000)),
                transcript = transcript,
            )
            val moment = Moment(
                id = momentId,
                title = d.title,
                emoji = d.emoji,
                date = LocalDate.parse(d.id.substring(2, 12)),
                dateEnd = d.dateEnd,
                dateConfidence = if (d.dateEnd != null) DateConfidence.APPROX else DateConfidence.EXACT,
                categories = d.categories,
                places = d.places,
                people = d.people,
                firstTime = d.firstTime,
                star = d.star,
                night = nightId,
                audioFromMs = 8_000,
                audioToMs = transcript.last().endMs,
                story = d.story,
                quote = d.quote,
            )
            val recorder = space.member(d.by)?.name ?: d.by
            store.writeText(SpaceLayout.nightNote(nightId), NightNote.write(night, recorder))
            store.writeBytes(SpaceLayout.audio(nightId, "wav"), wav)
            store.writeText(SpaceLayout.momentNote(momentId), MomentNote.write(moment))
        }
        val food = Collection(
            fileName = "best-food-2026",
            kind = CollectionKind.ALBUM,
            title = "🍝 Best food of 2026",
            intro = "Everything we loved eating this year.",
            moments = listOf(MomentId("m-2026-10-03-chestnuts-p8nc"), MomentId("m-2026-10-07-ramen-n9qe")),
        )
        store.writeText(SpaceLayout.collection(food.fileName), CollectionNote.write(food))
    }

    /** One second of 8 kHz, 16-bit mono silence as a WAV file. */
    fun silentWav(seconds: Int = 1, sampleRate: Int = 8_000): ByteArray {
        val dataSize = seconds * sampleRate * 2
        val out = ByteArray(44 + dataSize)
        fun ascii(at: Int, s: String) = s.forEachIndexed { i, c -> out[at + i] = c.code.toByte() }
        fun int32(at: Int, v: Int) = (0..3).forEach { out[at + it] = (v shr (8 * it)).toByte() }
        fun int16(at: Int, v: Int) = (0..1).forEach { out[at + it] = (v shr (8 * it)).toByte() }
        ascii(0, "RIFF"); int32(4, 36 + dataSize); ascii(8, "WAVE")
        ascii(12, "fmt "); int32(16, 16); int16(20, 1); int16(22, 1)
        int32(24, sampleRate); int32(28, sampleRate * 2); int16(32, 2); int16(34, 16)
        ascii(36, "data"); int32(40, dataSize)
        return out
    }
}
