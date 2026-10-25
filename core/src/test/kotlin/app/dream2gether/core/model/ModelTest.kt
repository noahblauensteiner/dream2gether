package app.dream2gether.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ModelTest {
    @Test
    fun `category keys round-trip and unknown keys give null`() {
        Category.entries.forEach { assertEquals(it, Category.fromKey(it.key)) }
        assertEquals(Category.LITTLE_THINGS, Category.fromKey(" Little-Things "))
        assertNull(Category.fromKey("work"))
    }

    @Test
    fun `space rejects duplicate or malformed member ids`() {
        assertFailsWith<IllegalArgumentException> {
            Space("A & B", listOf(Member("alex", "Alex"), Member("alex", "Alex 2")), timezone = "Europe/Vienna")
        }
        assertFailsWith<IllegalArgumentException> { Member("Alex", "Alex") }
    }

    @Test
    fun `unknown collection kind falls back to other`() {
        assertEquals(CollectionKind.PHOTO_BOOK, CollectionKind.fromKey("photo-book"))
        assertEquals(CollectionKind.OTHER, CollectionKind.fromKey("scrapbook"))
    }
}
