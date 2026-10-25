package app.dream2gether.core.extract

import app.dream2gether.core.model.Category
import app.dream2gether.core.model.DateConfidence
import app.dream2gether.core.model.Member
import app.dream2gether.core.model.NightId
import app.dream2gether.core.model.Segment
import app.dream2gether.core.model.Space
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MomentExtractorTest {
    private val space = Space(
        "Noah & Kim", listOf(Member("noah", "Noah"), Member("kim", "Kim")), timezone = "Europe/Vienna",
        glossaryPeople = listOf("Grandma Rosi"),
    )
    private val evening = LocalDate(2026, 10, 24)
    private val transcript = listOf(
        "Kim: Okay, favourite moment today?",
        "Noah: The ramen. Six hours for one bowl of soup and I'd do it again tomorrow.",
        "Kim: And on Wednesday grandma rosi called, that was lovely.",
        "Noah: We should learn to make pho.",
    ).mapIndexed { i, t -> Segment(i, i * 10_000L, (i + 1) * 10_000L, t) }

    private fun input(hasAudio: Boolean = true, segments: List<Segment> = transcript) = MomentExtractor.Input(
        space, NightId("s-2026-10-24-2241-noah"), evening, "What was your favourite moment today?", segments, hasAudio = hasAudio,
    )

    private val answer = """
        Sure! Here it is:
        {"moments":[
          {"title":"Ramen from scratch","emoji":"🍜","day_ref":"today","day_ref_end":"","approx":false,"who":"noah",
           "categories":["food","cozy","made-up"],"places":["home"],"people":[],"first_time":true,
           "story":"We spent six hours making ramen.","quote":"Six hours for one bowl of soup and I'd do it again tomorrow",
           "photo_hint":"two bowls","segments":[1, 99]},
          {"title":"Call with Grandma Rosi","emoji":"call","day_ref":"weekday:wednesday","day_ref_end":"","approx":false,
           "who":"grandma","categories":["people"],"places":[],"people":["grandma rosi"],"first_time":false,
           "story":"","quote":"She said we're her favourite","photo_hint":"","segments":[2]}
        ],
        "dreams":[{"text":"Learn to make pho","segments":[3]}]}
    """.trimIndent()

    @Test
    fun `turns model output into clean moments`() {
        val llm = FakeLlm(answer)
        val r = MomentExtractor(llm, Random(7)).extract(input())
        assertEquals(2, r.moments.size)
        val (ramen, call) = r.moments

        assertEquals(evening, ramen.date)
        assertEquals(DateConfidence.EXACT, ramen.dateConfidence)
        assertEquals(listOf(Category.FOOD, Category.COZY), ramen.categories)
        assertEquals("noah", ramen.who)
        assertEquals("Six hours for one bowl of soup and I'd do it again tomorrow", ramen.quote)
        assertEquals(10_000L to 20_000L, ramen.audioFromMs to ramen.audioToMs)
        assertEquals(MomentHash.of(ramen.copy(generatedHash = null)), ramen.generatedHash)
        assertTrue(ramen.id.value.startsWith("m-2026-10-24-ramen-from-scratch-"))

        assertEquals(LocalDate(2026, 10, 21), call.date)
        assertEquals("both", call.who, "unknown member falls back to both")
        assertEquals("✨", call.emoji, "words aren't emoji")
        assertNull(call.quote, "a quote that's not in the transcript is dropped")
        assertEquals("Call with Grandma Rosi", call.story, "empty story falls back to the title")
        assertEquals(listOf("Grandma Rosi"), call.people, "glossary spelling wins")

        assertEquals(listOf("Learn to make pho"), r.dreams.map { it.text })
        assertEquals(30_000L, r.dreams[0].atMs)
    }

    @Test
    fun `sends schema, members and numbered transcript`() {
        val llm = FakeLlm(answer)
        MomentExtractor(llm).extract(input())
        val req = llm.requests.single()
        assertContains(req.user, "[1] Noah: The ramen.")
        assertContains(req.user, "Chat date: Saturday 2026-10-24")
        assertContains(req.system, "noah (Noah), kim (Kim)")
        val who = req.jsonSchema!!["properties"]!!.jsonObject["moments"]!!.jsonObject["items"]!!
            .jsonObject["properties"]!!.jsonObject["who"]!!.jsonObject["enum"]!!.jsonArray.map { it.toString().trim('"') }
        assertEquals(listOf("noah", "kim", "both"), who)
    }

    @Test
    fun `typed chats get no audio times`() {
        val r = MomentExtractor(FakeLlm(answer)).extract(input(hasAudio = false))
        assertNull(r.moments[0].audioFromMs)
        assertNull(r.dreams[0].atMs)
    }

    @Test
    fun `retries once on broken JSON, then gives up clearly`() {
        val ok = MomentExtractor(FakeLlm("{\"moments\": [", answer)).extract(input())
        assertEquals(2, ok.moments.size)
        val e = assertFailsWith<MomentExtractor.ExtractionFailed> { MomentExtractor(FakeLlm("no json here")).extract(input()) }
        assertContains(e.message!!, "no json here")
    }

    @Test
    fun `long nights are chunked and duplicates merged`() {
        val long = (0 until 400).map { Segment(it, it * 5_000L, (it + 1) * 5_000L, "Line $it " + "blah ".repeat(10)) }
        val same = """{"moments":[{"title":"Same moment","emoji":"🌟","day_ref":"today","day_ref_end":"","approx":false,
            "who":"both","categories":["us"],"places":[],"people":[],"first_time":false,"story":"Short.","quote":"",
            "photo_hint":"","segments":[100]}],"dreams":[{"text":"Same dream","segments":[]}]}"""
        val llm = FakeLlm(same)
        val r = MomentExtractor(llm).extract(input(segments = long))
        assertTrue(llm.requests.size > 1, "should take several chunks")
        assertEquals(1, r.moments.size)
        assertEquals(1, r.dreams.size)
    }

    @Test
    fun `nothing said, nothing asked`() {
        val llm = FakeLlm(answer)
        assertEquals(0, MomentExtractor(llm).extract(input(segments = emptyList())).moments.size)
        assertEquals(0, llm.requests.size)
    }
}
