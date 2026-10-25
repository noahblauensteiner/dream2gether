package app.dream2gether.core.extract

import app.dream2gether.core.model.DateConfidence
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class DateRefTest {
    private val saturday = LocalDate(2026, 10, 24)

    private fun r(ref: String, end: String? = null, approx: Boolean = false, evening: LocalDate = saturday) =
        DateRef.resolve(ref, end, approx, evening)

    @Test
    fun `simple references`() {
        assertEquals(DateRef.Resolved(saturday, null, DateConfidence.EXACT), r("today"))
        assertEquals(LocalDate(2026, 10, 23), r("yesterday").date)
        assertEquals(DateRef.Resolved(null, null, DateConfidence.UNKNOWN), r("unknown"))
        assertEquals(DateRef.Resolved(null, null, DateConfidence.UNKNOWN), r("next tuesday"))
    }

    @Test
    fun `weekdays look back, and the same weekday means today`() {
        assertEquals(saturday, r("weekday:saturday").date)
        assertEquals(LocalDate(2026, 10, 18), r("weekday:sunday").date)
        assertEquals(LocalDate(2026, 10, 19), r("weekday:Monday").date)
        // Across a month boundary.
        assertEquals(LocalDate(2026, 9, 30), r("weekday:wednesday", evening = LocalDate(2026, 10, 3)).date)
    }

    @Test
    fun `explicit dates, future dates are unknown`() {
        assertEquals(LocalDate(2026, 10, 17), r("date:2026-10-17").date)
        assertEquals(DateConfidence.UNKNOWN, r("date:2026-11-02").confidence)
        assertEquals(DateConfidence.UNKNOWN, r("date:2026-13-40").confidence)
    }

    @Test
    fun `ranges and vague references are approx`() {
        assertEquals(DateRef.Resolved(LocalDate(2026, 10, 19), saturday, DateConfidence.APPROX), r("this-week"))
        assertEquals(DateRef.Resolved(saturday, null, DateConfidence.EXACT), r("weekend"))
        val sunday = LocalDate(2026, 10, 25)
        assertEquals(DateRef.Resolved(saturday, sunday, DateConfidence.APPROX), r("weekend", evening = sunday))
        assertEquals(DateConfidence.APPROX, r("today", approx = true).confidence)
        assertEquals(
            DateRef.Resolved(LocalDate(2026, 10, 22), LocalDate(2026, 10, 23), DateConfidence.APPROX),
            r("yesterday", end = "weekday:thursday"),
        )
    }

    @Test
    fun `after midnight still counts as the evening before`() {
        assertEquals(LocalDate(2026, 10, 24), DateRef.eveningOf(LocalDateTime(2026, 10, 25, 0, 40)))
        assertEquals(LocalDate(2026, 10, 24), DateRef.eveningOf(LocalDateTime(2026, 10, 24, 22, 40)))
    }

    @Test
    fun `pattern accepts exactly the documented forms`() {
        val p = Regex(DateRef.PATTERN)
        listOf("today", "yesterday", "this-week", "weekend", "unknown", "weekday:friday", "date:2026-10-17")
            .forEach { assertEquals(true, p.matches(it), it) }
        listOf("Today", "weekday:fri", "date:17.10.2026", "last week").forEach { assertEquals(false, p.matches(it), it) }
    }
}
