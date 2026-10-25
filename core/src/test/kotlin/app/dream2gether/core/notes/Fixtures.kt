package app.dream2gether.core.notes

import app.dream2gether.core.model.Category
import app.dream2gether.core.model.Comment
import app.dream2gether.core.model.DateConfidence
import app.dream2gether.core.model.DreamMention
import app.dream2gether.core.model.Moment
import app.dream2gether.core.model.MomentId
import app.dream2gether.core.model.MomentLink
import app.dream2gether.core.model.Night
import app.dream2gether.core.model.NightId
import app.dream2gether.core.model.NightStatus
import app.dream2gether.core.model.Processing
import app.dream2gether.core.model.Segment
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.UtcOffset

object Fixtures {
    val nightId = NightId("s-2026-10-07-2250-sam")
    val ramenId = MomentId("m-2026-10-07-ramen-n9qe")

    val ramen = Moment(
        id = ramenId,
        title = "Ramen from scratch",
        emoji = "🍜",
        date = LocalDate(2026, 10, 7),
        dateConfidence = DateConfidence.EXACT,
        categories = listOf(Category.FOOD, Category.COZY),
        places = listOf("home"),
        firstTime = true,
        star = true,
        night = nightId,
        audioFromMs = 62_000,
        audioToMs = 134_000,
        generatedHash = "9f2c41d0",
        story = "Six hours of broth, two burnt eggs, one perfect bowl each. We ate on the living room floor\n" +
            "because the table was still covered in flour.",
        quote = "Six hours for one bowl of soup and I'd do it again tomorrow.",
        photos = listOf("m-2026-10-07-ramen-n9qe-1.jpg"),
        comments = listOf(Comment("Alex", "8 Oct", "==Best soup of my life.== Next time: less salt.")),
    )

    val night = Night(
        id = nightId,
        recordedBy = "sam",
        device = "sams-phone",
        recordedAt = LocalDateTime(2026, 10, 7, 22, 50, 12),
        utcOffset = UtcOffset(hours = 2),
        durationS = 612,
        language = "en",
        question = "What was your favourite moment today?",
        audioFile = "s-2026-10-07-2250-sam.m4a",
        status = NightStatus.EXTRACTED,
        processing = Processing("whisper-small.en", "gemma-4-e2b", "extract@1", "2026-10-08T03:12:40+02:00"),
        moments = listOf(MomentLink(ramenId, "🍜 Ramen from scratch", 62_000)),
        dreams = listOf(DreamMention("Learn to make proper pho", 451_000)),
        transcript = listOf(
            Segment(0, 0, 3_000, "Okay. Favourite moment today, go."),
            Segment(1, 3_000, 5_000, "Easy. The ramen."),
            Segment(2, 5_000, 612_000, "Six hours for one bowl of soup and I'd do it again tomorrow."),
        ),
    )
}
