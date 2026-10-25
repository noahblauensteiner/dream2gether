package app.dream2gether.core.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class IdsTest {
    @Test
    fun `night id uses date, hour-minute and member`() {
        val id = NightId.of(LocalDateTime(2026, 10, 7, 22, 50, 12), "sam")
        assertEquals("s-2026-10-07-2250-sam", id.value)
    }

    @Test
    fun `moment id has date, short slug and four-char suffix`() {
        val id = MomentId.of(LocalDate(2026, 10, 7), "Ramen from scratch, again!", Random(1))
        assertTrue(Regex("""m-2026-10-07-ramen-from-scratch-[a-z0-9]{4}""").matches(id.value), id.value)
    }

    @Test
    fun `two moments with the same title get different ids`() {
        val date = LocalDate(2026, 10, 7)
        val random = Random(42)
        assertNotEquals(MomentId.of(date, "Ramen", random), MomentId.of(date, "Ramen", random))
    }

    @Test
    fun `slug drops accents, emoji and punctuation`() {
        assertEquals("caf-de-flore", slugify("Café de Flore ☕"))
        assertEquals("moment", slugify("🎉🎉"))
        assertEquals("storm-watching-with", slugify("  Storm   watching with tea  "))
    }

    @Test
    fun `malformed ids are rejected`() {
        assertFailsWith<IllegalArgumentException> { NightId("2026-10-07-sam") }
        assertFailsWith<IllegalArgumentException> { MomentId("m-2026-10-07-ramen") }
    }
}
