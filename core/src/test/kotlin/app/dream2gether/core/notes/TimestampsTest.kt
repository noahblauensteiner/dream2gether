package app.dream2gether.core.notes

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.UtcOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TimestampsTest {
    @Test
    fun `clock times`() {
        assertEquals("01:02", formatClock(62_000))
        assertEquals("1:02:03", formatClock(3_723_000))
        assertEquals(62_000, parseClock("01:02"))
        assertEquals(62_000, parseClock("1:02"))
        assertEquals(3_723_000, parseClock("1:02:03"))
        assertNull(parseClock("soon"))
        assertNull(parseClock(""))
    }

    @Test
    fun `offset date-times keep seconds and offset`() {
        val text = formatOffsetDateTime(LocalDateTime(2026, 10, 7, 22, 50, 0), UtcOffset(hours = 2))
        assertEquals("2026-10-07T22:50:00+02:00", text)
        assertEquals(LocalDateTime(2026, 10, 7, 22, 50, 0) to UtcOffset(hours = 2), parseOffsetDateTime(text))
    }

    @Test
    fun `short day`() {
        assertEquals("Wed 7 Oct", shortDay(LocalDate(2026, 10, 7)))
        assertEquals("Sat 31 Oct", shortDay(LocalDate(2026, 10, 31)))
    }
}
